package com.haulmont.cuba.web.app.embedded;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;
import com.haulmont.cuba.web.app.embedded.window.GuestWindowHolder;
import com.haulmont.cuba.web.app.embedded.window.RemoteWindowManager;
import com.haulmont.cuba.web.app.embedded.window.RemoteWindowManager.RemoteLookupHandler;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.mainwindow.WebAppWorkArea;
import com.haulmont.cuba.web.sys.WindowBreadCrumbs;
import com.haulmont.cuba.web.toolkit.ui.ContentSwitchMode;
import com.haulmont.cuba.web.toolkit.ui.CubaWindow;
import com.haulmont.cuba.web.toolkit.ui.TabSheetBehaviour;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HostAppWindowManager extends WebWindowManager {

    @Inject
    private EmbedAppConfig embedAppConfig;

    private Map<Window, GuestWindowHandlerImpl> remoteApps = new HashMap<>();

    private int guestIdCounter = 0;

    @Override
    public Window openWindow(WindowInfo windowInfo, OpenType openType, Map<String, Object> params) {
        if (windowInfo instanceof RemoteWindowInfo) {
            RemoteWindowInfo remoteWindowInfo = (RemoteWindowInfo) windowInfo;
            String appName = remoteWindowInfo.getAppName();

            String appOrigin = getAppUrl(appName);

            String appId = Integer.toString(guestIdCounter++);
            params = ParamsMap.of(
                    "screenAlias", remoteWindowInfo.getRemoteScreenId(),
                    "appUrl", appOrigin,
                    "paramsMap", params,
                    "item", remoteWindowInfo.getRemoteItem(),
                    "appId", appId,
                    "remoteWindowMode", remoteWindowInfo.getRemoteWindowMode());

            Window window = super.openWindow(windowInfo, openType, params);
            RemoteApp app = new RemoteApp(appId);
            GuestWindowHandlerImpl windowHandler = new GuestWindowHandlerImpl(app, window);
            remoteApps.put(window, windowHandler);
            return window;
        }
        return super.openWindow(windowInfo, openType, params);
    }

    @Override
    protected void showWindow(Window window, String caption, String description, OpenType type, boolean multipleOpen) {
        super.showWindow(window, caption, description, type, multipleOpen);
    }

    @Override
    protected Component showWindowThisTab(com.haulmont.cuba.gui.components.Window window, String caption, String description) {
        getDialogParams().reset();

        WebAppWorkArea workArea = getConfiguredWorkArea(createWorkAreaContext(window));

        Layout layout;
        if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
            TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
            layout = (Layout) tabSheet.getSelectedTab();
        } else {
            layout = (Layout) workArea.getSingleWindowContainer().getComponent(0);
        }

        final WindowBreadCrumbs breadCrumbs = tabs.get(layout);
        if (breadCrumbs == null) {
            throw new IllegalStateException("BreadCrumbs not found");
        }

        final Window currentWindow = breadCrumbs.getCurrentWindow();

        Set<Map.Entry<Window, Integer>> set = windows.entrySet();
        boolean pushed = false;
        for (Map.Entry<Window, Integer> entry : set) {
            if (entry.getKey().equals(currentWindow)) {
                windows.remove(currentWindow);
                getStack(breadCrumbs).push(new Pair<>(entry.getKey(), entry.getValue()));
                pushed = true;
                break;
            }
        }
        if (!pushed) {
            getStack(breadCrumbs).push(new Pair<>(currentWindow, null));
        }

        removeFromWindowMap(currentWindow);
        WebComponentsHelper.getComposition(currentWindow).addStyleName("hidden-tab");

        final Component component = WebComponentsHelper.getComposition(window);
        component.setSizeFull();
        layout.addComponent(component);

        breadCrumbs.addWindow(window);

        if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
            TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
            String tabId = tabSheet.getTab(layout);
            String formattedCaption = formatTabCaption(caption, description);
            tabSheet.setTabCaption(tabId, formattedCaption);
            String formattedDescription = formatTabDescription(caption, description);

            if (!Objects.equals(formattedCaption, formattedDescription)) {
                tabSheet.setTabDescription(tabId, formattedDescription);
            } else {
                tabSheet.setTabDescription(tabId, null);
            }

            tabSheet.setTabIcon(tabId, WebComponentsHelper.getIcon(window.getIcon()));

            ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(window.getContentSwitchMode().name());
            tabSheet.setContentSwitchMode(tabId, contentSwitchMode);
        } else {
            layout.markAsDirtyRecursive();
        }

        return layout;
    }

    @Override
    protected void closeWindow(Window window, WindowOpenInfo openInfo) {
        if (!disableSavingScreenHistory) {
            screenHistorySupport.saveScreenHistory(window, openInfo.getOpenMode());
        }

        GuestWindowHandlerImpl windowHandler = remoteApps.get(window);
        if (windowHandler != null) {
            windowHandler.destroy();
            remoteApps.remove(window);
        }

        WebWindow webWindow = (WebWindow) window;
        webWindow.stopTimers();

        switch (openInfo.getOpenMode()) {
            case DIALOG: {
                final CubaWindow cubaDialogWindow = (CubaWindow) openInfo.getData();
                cubaDialogWindow.forceClose();
                fireListeners(window, tabs.size() != 0);
                break;
            }

            case NEW_WINDOW:
            case NEW_TAB: {
                final Layout layout = (Layout) openInfo.getData();
                layout.removeComponent(WebComponentsHelper.getComposition(window));

                WebAppWorkArea workArea = getConfiguredWorkArea(createWorkAreaContext(window));

                if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
                    TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
                    tabSheet.silentCloseTabAndSelectPrevious(layout);
                    tabSheet.removeComponent(layout);
                } else {
                    VerticalLayout singleLayout = workArea.getSingleWindowContainer();
                    singleLayout.removeComponent(layout);
                }

                WindowBreadCrumbs windowBreadCrumbs = tabs.get(layout);
                if (windowBreadCrumbs != null) {
                    windowBreadCrumbs.clearListeners();
                    windowBreadCrumbs.removeWindow();
                }

                tabs.remove(layout);
                stacks.remove(windowBreadCrumbs);
                fireListeners(window, !tabs.isEmpty());
                if (tabs.isEmpty() && app.getConnection().isConnected()) {
                    workArea.switchTo(AppWorkArea.State.INITIAL_LAYOUT);
                }
                break;
            }
            case THIS_TAB: {
                final Layout layout = (Layout) openInfo.getData();

                final WindowBreadCrumbs breadCrumbs = tabs.get(layout);
                if (breadCrumbs == null) {
                    throw new IllegalStateException("Unable to closeWindow screen: breadCrumbs not found");
                }

                breadCrumbs.removeWindow();
                Window currentWindow = breadCrumbs.getCurrentWindow();
                if (!getStack(breadCrumbs).empty()) {
                    Pair<Window, Integer> entry = getStack(breadCrumbs).pop();
                    putToWindowMap(entry.getFirst(), entry.getSecond());
                }

                Component component = WebComponentsHelper.getComposition(currentWindow);
                component.setSizeFull();

                WebAppWorkArea workArea = getConfiguredWorkArea(createWorkAreaContext(window));

                layout.removeComponent(WebComponentsHelper.getComposition(window));
                if (app.getConnection().isConnected()) {
                    component.removeStyleName("hidden-tab");

                    if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
                        TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
                        String tabId = tabSheet.getTab(layout);
                        String formattedCaption = formatTabCaption(currentWindow.getCaption(), currentWindow.getDescription());
                        tabSheet.setTabCaption(tabId, formattedCaption);
                        String formattedDescription = formatTabDescription(currentWindow.getCaption(), currentWindow.getDescription());

                        if (!Objects.equals(formattedCaption, formattedDescription)) {
                            tabSheet.setTabDescription(tabId, formattedDescription);
                        } else {
                            tabSheet.setTabDescription(tabId, null);
                        }

                        tabSheet.setTabIcon(tabId, WebComponentsHelper.getIcon(currentWindow.getIcon()));

                        ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(currentWindow.getContentSwitchMode().name());
                        tabSheet.setContentSwitchMode(tabId, contentSwitchMode);
                    }
                }
                fireListeners(window, !tabs.isEmpty());
                if (tabs.isEmpty() && app.getConnection().isConnected()) {
                    workArea.switchTo(AppWorkArea.State.INITIAL_LAYOUT);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }

    protected Layout createNewTabLayout(final Window window, final boolean multipleOpen, WindowBreadCrumbs breadCrumbs,
                                        Component... additionalComponents) {
        Layout layout = super.createNewTabLayout(window, multipleOpen, breadCrumbs, additionalComponents);
        layout.removeComponent(breadCrumbs);

        CssLayout breadcrumbsWrapper = new CssLayout();
        breadcrumbsWrapper.addComponent(breadCrumbs);
        ((CssLayout) layout).addComponent(breadcrumbsWrapper, 0);
        return layout;
    }

    private String getAppUrl(String appName) {
        Map<String, String> apps = embedAppConfig.getApps();
        return apps.get(appName);
    }

    private class ResendingLookupHandler implements RemoteLookupHandler {

        private final RemoteLookupHandler delegate;
        private Window window;

        private ResendingLookupHandler(RemoteLookupHandler delegate, Window window) {
            this.delegate = delegate;
            this.window = window;
        }

        @Override
        public void handleLookup(RemoteEntityInfo[] items) {
            delegate.handleLookup(items);
            window.close(Window.CLOSE_ACTION_ID);
        }
    }

    class GuestWindowHandlerImpl {

        private final RemoteWindowManager windowManager;
        private final HostBreadcrumbListener breadcrumbListener;
        private RemoteApp app;
        private Window window;
        private LinkedList<GuestWindow> guestWindows = new LinkedList<>();

        GuestWindowHandlerImpl(RemoteApp app, Window window) {
            this.app = app;
            this.window = window;
            this.windowManager = new RemoteWindowManagerImpl(this);
            app.register(windowManager, RemoteWindowManager.class);
            breadcrumbListener = app.get(HostBreadcrumbListener.class);
        }

        private WindowBreadCrumbs getBreadcrumbs() {
            Window window = this.window;
            if (window instanceof Window.Wrapper) {
                window = ((Window.Wrapper) window).getWrappedWindow();
            }

            WindowOpenInfo openInfo = windowOpenMode.get(window);
            //noinspection SuspiciousMethodCalls
            return tabs.get(openInfo.getData());
        }

        private void updateCaption() {
            String caption = getBreadcrumbs().getCurrentWindow().getCaption();

            Window window = this.window;
            if (window instanceof Window.Wrapper) {
                window = ((Window.Wrapper) window).getWrappedWindow();
            }

            WindowOpenInfo openInfo = windowOpenMode.get(window);
            if (openInfo != null) {
                OpenMode openMode = openInfo.getOpenMode();
                if (openMode == OpenMode.NEW_TAB
                        || openMode == OpenMode.NEW_WINDOW
                        || openMode == OpenMode.THIS_TAB) {
                    Layout layout = (Layout) openInfo.getData();
                    TabSheetBehaviour webTabsheet = getConfiguredWorkArea(createWorkAreaContext(window))
                            .getTabbedWindowContainer().getTabSheetBehaviour();
                    String tabId = webTabsheet.getTab(layout);
                    webTabsheet.setTabCaption(tabId, caption);
                }
            }
        }

        public void closeWindow(String actionId) {
            breadcrumbListener.closeWindow();
            popStack(actionId);
        }

        public void destroy() {
            app.destroy();
        }

        public void addToStack(String caption, String description) {
            window.setCaption(caption);

            //noinspection IncorrectCreateGuiComponent
            GuestWindow guestWindow = new GuestWindow(((GuestWindowHolder) window), this, caption, description);
            WindowBreadCrumbs breadCrumbs = getBreadcrumbs();
            if (guestWindows.isEmpty()) {
                breadCrumbs.removeWindow();
            }
            guestWindows.add(guestWindow);
            breadCrumbs.addWindow(guestWindow);

            updateCaption();
        }

        public void popStack(String actionId) {
            if (!guestWindows.isEmpty()) {
                getBreadcrumbs().removeWindow();
                guestWindows.removeLast();
            }
            if (guestWindows.isEmpty()) {
                getBreadcrumbs().addWindow(window);
                HostAppWindowManager.this.close(window);
            } else {
                updateCaption();
            }
        }
    }

    private class RemoteWindowManagerImpl implements RemoteWindowManager {

        private GuestWindowHandlerImpl guestWindowHandler;

        RemoteWindowManagerImpl(GuestWindowHandlerImpl guestWindowHandler) {
            this.guestWindowHandler = guestWindowHandler;
        }

        public void openLookup(String appName, String screenAlias, RemoteLookupHandler lookupHandler, OpenMode openType, Map<String, Object> remoteScreenParams) {
            RemoteWindowInfo windowInfo = (RemoteWindowInfo) windowConfig.getWindowInfo(appName + "/" + screenAlias);
            windowInfo.setRemoteWindowMode(RemoteWindowInfo.RemoteWindowMode.LOOKUP);

            GuestWindowHolder lookup = (GuestWindowHolder) openWindow(windowInfo, new OpenType(openType), remoteScreenParams);
            RemoteApp remoteApp = remoteApps.get(lookup).app;
            remoteApp.register(new ResendingLookupHandler(lookupHandler, lookup), RemoteLookupHandler.class);
        }

        public void openEditor(String appName, String screenAlias, String item, RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> remoteScreenParams) {
            RemoteWindowInfo windowInfo = (RemoteWindowInfo) windowConfig.getWindowInfo(appName + "/" + screenAlias);
            windowInfo.setRemoteWindowMode(RemoteWindowInfo.RemoteWindowMode.EDITOR);
            windowInfo.setRemoteItem(item);

            GuestWindowHolder editor = (GuestWindowHolder) openWindow(windowInfo, new OpenType(openType), remoteScreenParams);
            RemoteApp remoteApp = remoteApps.get(editor).app;
            remoteApp.register(new ResendingLookupHandler(handler, editor), RemoteLookupHandler.class);
        }

        public void showOptionsDialog(String title, String message, Frame.MessageType messageType, RemoteDialogAction[] actions, RemoteDialogHandler handler) {
            Action[] localActions = Arrays.stream(actions)
                    .map(remoteDialogAction -> remoteDialogAction.createDialogAction(handler))
                    .collect(Collectors.toList()).toArray(new Action[actions.length]);

            showOptionDialog(title, message, messageType, localActions);
        }

        @Override
        public void showNotification(String caption, String description, Frame.NotificationType type) {
        }

        @Override
        public void guestWindowOpened(String caption, String description) {
            guestWindowHandler.addToStack(caption, description);
        }

        public void remoteWindowClosed(String actionId) {
            guestWindowHandler.popStack(actionId);
        }
    }
}

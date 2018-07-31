/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.haulmont.cuba.web.sys;

import com.google.common.base.Strings;
import com.haulmont.bali.events.EventHub;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.DialogWindow;
import com.haulmont.cuba.gui.components.RootWindow;
import com.haulmont.cuba.gui.components.TabWindow;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.Window.BeforeCloseWithCloseButtonEvent;
import com.haulmont.cuba.gui.components.Window.BeforeCloseWithShortcutEvent;
import com.haulmont.cuba.gui.components.Window.HasWorkArea;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import com.haulmont.cuba.gui.components.mainwindow.UserIndicator;
import com.haulmont.cuba.gui.components.sys.WindowImplementation;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.sys.ScreenDependencyInjector;
import com.haulmont.cuba.gui.sys.ScreenViewsLoader;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.gui.xml.layout.LayoutLoader;
import com.haulmont.cuba.gui.xml.layout.ScreenXmlLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import com.haulmont.cuba.security.app.UserSettingService;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.gui.components.mainwindow.WebAppWorkArea;
import com.haulmont.cuba.web.gui.icons.IconResolver;
import com.haulmont.cuba.web.widgets.ContentSwitchMode;
import com.haulmont.cuba.web.widgets.HasTabSheetBehaviour;
import com.haulmont.cuba.web.widgets.TabSheetBehaviour;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import static com.haulmont.cuba.gui.ComponentsHelper.walkComponents;
import static com.haulmont.cuba.gui.components.Window.CLOSE_ACTION_ID;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(WindowManager.NAME)
public class WebWindowManager implements WindowManager {

    /**
     * Constant that is passed to {@link Window#close(String)} and {@link Window#close(String, boolean)} methods when
     * the screen is closed by window manager. Propagated to {@link Window.CloseListener#windowClosed}.
     */
    public static final String MAIN_MENU_ACTION_ID = "mainMenu";

    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected Security security;
    @Inject
    protected UuidSource uuidSource;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected BeanLocator beanLocator;
    @Inject
    protected ScreenXmlLoader screenXmlLoader;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected UserSettingService userSettingService;
    @Inject
    protected ScreenViewsLoader screenViewsLoader;
    @Inject
    protected IconResolver iconResolver;

    @Inject
    protected WebConfig webConfig;

    protected AppUI ui;

    public WebWindowManager(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public <T extends Screen> T create(Class<T> requiredScreenClass, LaunchMode launchMode, ScreenOptions options) {
        WindowInfo windowInfo = getScreenInfo(requiredScreenClass);

        checkPermissions(launchMode, windowInfo);

        // todo perf4j stop watches for lifecycle

        @SuppressWarnings("unchecked")
        Class<T> resolvedScreenClass = (Class<T>) windowInfo.getScreenClass();

        Window window = createWindow(windowInfo, resolvedScreenClass, launchMode);

        T controller = createController(windowInfo, window, resolvedScreenClass, launchMode);

        loadScreenXml(windowInfo, window, controller, options);

        ScreenUtils.setWindow(controller, window);
        ScreenUtils.setWindowInfo(controller, windowInfo);
        ScreenUtils.setScreenOptions(controller, options);

        WindowImplementation windowImpl = (WindowImplementation) window;
        windowImpl.setController(controller);
        windowImpl.setLaunchMode(launchMode);

        // todo legacy datasource layer

        ScreenDependencyInjector dependencyInjector =
                beanLocator.getPrototype(ScreenDependencyInjector.NAME, controller, options, this);
        dependencyInjector.inject();

        InitEvent initEvent = new InitEvent(controller, options);
        ScreenUtils.fireEvent(controller, InitEvent.class, initEvent);

        AfterInitEvent afterInitEvent = new AfterInitEvent(controller, options);
        ScreenUtils.fireEvent(controller, AfterInitEvent.class, afterInitEvent);

        return controller;
    }

    protected <T extends Screen> void loadScreenXml(WindowInfo windowInfo, Window window, T controller,
                                                    ScreenOptions options) {
        String templatePath = windowInfo.getTemplate();

        if (StringUtils.isNotEmpty(templatePath)) {
            // todo support relative design path

            Element element = screenXmlLoader.load(templatePath, windowInfo.getId(),
                    Collections.emptyMap()); // todo support legacy params map

            // todo load XML markup if annotation present, or screen is legacy screen

            ComponentLoaderContext componentLoaderContext =
                    new ComponentLoaderContext(Collections.emptyMap()); // todo support legacy parameters map
            componentLoaderContext.setFullFrameId(windowInfo.getId());
            componentLoaderContext.setCurrentFrameId(windowInfo.getId());
            componentLoaderContext.setFrame(window);

            ComponentLoader windowLoader = createLayout(windowInfo, window, element, componentLoaderContext);

            screenViewsLoader.deployViews(element); // todo will be removed from new screens

            // todo load datasources here

            windowLoader.loadComponent();

            EventHub eventHub = ScreenUtils.getEventHub(controller);
            eventHub.subscribe(AfterInitEvent.class, event ->
                    componentLoaderContext.executePostInitTasks()
            );
        }
    }

    protected ComponentLoader createLayout(WindowInfo windowInfo, Window window, Element rootElement,
                                           ComponentLoader.Context context) {
        String descriptorPath = windowInfo.getTemplate();

        LayoutLoader layoutLoader = beanLocator.getPrototype(LayoutLoader.NAME, context);
        layoutLoader.setLocale(getLocale());

        // todo should we load messages depending on Class ?
        if (StringUtils.isNotEmpty(descriptorPath)) {
            if (descriptorPath.contains("/")) {
                descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
            }

            String path = descriptorPath.replaceAll("/", ".");
            int start = path.startsWith(".") ? 1 : 0;
            path = path.substring(start);

            layoutLoader.setMessagesPack(path);
        }
        //noinspection UnnecessaryLocalVariable
        ComponentLoader windowLoader = layoutLoader.createWindowContent(window, rootElement, windowInfo.getId());
        return windowLoader;
    }

    protected Locale getLocale() {
        return userSessionSource.getUserSession().getLocale();
    }

    @Override
    public void show(Screen screen) {
        checkMultiOpen(screen);

        // todo load and apply settings

        // todo UI security

        BeforeShowEvent beforeShowEvent = new BeforeShowEvent(screen);
        ScreenUtils.fireEvent(screen, BeforeShowEvent.class, beforeShowEvent);

        WindowImplementation windowImpl = (WindowImplementation) screen.getWindow();

        if (windowImpl.getLaunchMode() instanceof OpenMode) {
            OpenMode openMode = (OpenMode) windowImpl.getLaunchMode();

            switch (openMode) {
                case ROOT:
                    showRootWindow(screen);
                    break;

                case THIS_TAB:
                    showThisTabWindow(screen);
                    break;

                case NEW_WINDOW:
                case NEW_TAB:
                    showNewTabWindow(screen);
                    break;

                case DIALOG:
                    showDialogWindow(screen);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported OpenMode " + openMode);
            }
        }

        AfterShowEvent afterShowEvent = new AfterShowEvent(screen);
        ScreenUtils.fireEvent(screen, AfterShowEvent.class, afterShowEvent);
    }

    @Override
    public void remove(Screen screen) {
        // todo remove event
    }

    @Override
    public void removeAll() {
        // todo implement
    }

    protected <T extends Screen> T createController(WindowInfo windowInfo, Window window,
                                                    Class<T> screenClass, LaunchMode launchMode) {
        Constructor<T> constructor;
        try {
            constructor = screenClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new DevelopmentException("No accessible constructor for screen class " + screenClass);
        }

        T controller;
        try {
            controller = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of screen class " + screenClass);
        }

        return controller;
    }

    protected Window createWindow(WindowInfo windowInfo, Class<? extends Screen> screenClass, LaunchMode launchMode) {
        Window window;
        if (launchMode instanceof OpenMode) {
            OpenMode openMode = (OpenMode) launchMode;
            switch (openMode) {
                case ROOT:
                    window = componentsFactory.createComponent(RootWindow.NAME);
                    break;

                case THIS_TAB:
                case NEW_TAB:
                    window = componentsFactory.createComponent(TabWindow.NAME);
                    break;

                case DIALOG:
                    window = componentsFactory.createComponent(DialogWindow.NAME);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported launch mode");
            }
        } else {
            throw new UnsupportedOperationException("Unsupported launch mode");
        }

        return window;
    }

    protected void checkMultiOpen(Screen screen) {
        // todo check if already opened, replace buggy int hash code
    }

    protected void checkPermissions(LaunchMode launchMode, WindowInfo windowInfo) {
        // ROOT windows are always permitted
        if (launchMode != OpenMode.ROOT) {
            boolean permitted = security.isScreenPermitted(windowInfo.getId());
            if (!permitted) {
                throw new AccessDeniedException(PermissionType.SCREEN, windowInfo.getId());
            }
        }
    }

    protected WindowInfo getScreenInfo(Class<? extends Screen> screenClass) {
        UiController uiController = screenClass.getAnnotation(UiController.class);
        // todo legacy screens
        if (uiController == null) {
            throw new IllegalArgumentException("No @UiController annotation for class " + screenClass);
        }

        String screenId = com.haulmont.cuba.gui.sys.ScreenUtils.getInferredScreenId(uiController, screenClass);

        return windowConfig.getWindowInfo(screenId);
    }

    protected void showRootWindow(Screen screen) {
        if (screen instanceof MainScreen) {
            MainScreen mainScreen = (MainScreen) screen;

            // bind system UI components to AbstractMainWindow
            walkComponents(screen.getWindow(), component -> {
                if (component instanceof AppWorkArea) {
                    mainScreen.setWorkArea((AppWorkArea) component);
                } else if (component instanceof UserIndicator) {
                    mainScreen.setUserIndicator((UserIndicator) component);
                } else if (component instanceof FoldersPane) {
                    mainScreen.setFoldersPane((FoldersPane) component);
                }

                return false;
            });
        }

        ui.setTopLevelWindow((RootWindow) screen.getWindow());

        if (screen instanceof Window.HasWorkArea) {
            AppWorkArea workArea = ((Window.HasWorkArea) screen).getWorkArea();
            if (workArea != null) {
                workArea.addStateChangeListener(new AppWorkArea.StateChangeListener() {
                    @Override
                    public void stateChanged(AppWorkArea.State newState) {
                        if (newState == AppWorkArea.State.WINDOW_CONTAINER) {
//                            todo implement
//                            initTabShortcuts();

                            // listener used only once
                            getConfiguredWorkArea().removeStateChangeListener(this);
                        }
                    }
                });
            }
        }
    }

    protected void showNewTabWindow(Screen screen) {
        WebAppWorkArea workArea = getConfiguredWorkArea();
        workArea.switchTo(AppWorkArea.State.WINDOW_CONTAINER);

        // close previous windows
        if (workArea.getMode() == AppWorkArea.Mode.SINGLE) {
            VerticalLayout mainLayout = workArea.getSingleWindowContainer();
            if (mainLayout.getComponentCount() > 0) {
                WindowContainer oldLayout = (WindowContainer) mainLayout.getComponent(0);
                WindowBreadCrumbs oldBreadCrumbs = oldLayout.getBreadCrumbs();
                if (oldBreadCrumbs != null) {
                    Window oldWindow = oldBreadCrumbs.getCurrentWindow();
                    oldWindow.closeAndRun(MAIN_MENU_ACTION_ID, () -> {
                        // todo implement
//                            showWindow(window, caption, description, WindowManagerImpl.OpenType.NEW_TAB, false)
                    });
                    return;
                }
            }
        } else {
            /* todo
            Integer hashCode = getWindowHashCode(window);
            com.vaadin.ui.ComponentContainer tab = null;
            if (hashCode != null && !multipleOpen) {
                tab = findTab(hashCode);
            }

            com.vaadin.ui.ComponentContainer oldLayout = tab;
            final WindowBreadCrumbs oldBreadCrumbs = tabs.get(oldLayout);

            if (oldBreadCrumbs != null
                    && windowOpenMode.containsKey(oldBreadCrumbs.getCurrentWindow().getFrame())
                    && !multipleOpen) {
                Window oldWindow = oldBreadCrumbs.getCurrentWindow();
                selectWindowTab(((Window.Wrapper) oldBreadCrumbs.getCurrentWindow()).getWrappedWindow());

                int tabPosition = -1;
                final TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
                String tabId = tabSheet.getTab(tab);
                if (tabId != null) {
                    tabPosition = tabSheet.getTabPosition(tabId);
                }

                final int finalTabPosition = tabPosition;
                oldWindow.closeAndRun(MAIN_MENU_ACTION_ID, () -> {
                    showWindow(window, caption, description, WindowManagerImpl.OpenType.NEW_TAB, false);

                    Window wrappedWindow = window;
                    if (window instanceof Window.Wrapper) {
                        wrappedWindow = ((Window.Wrapper) window).getWrappedWindow();
                    }

                    if (finalTabPosition >= 0 && finalTabPosition < tabSheet.getComponentCount() - 1) {
                        moveWindowTab(workArea, wrappedWindow, finalTabPosition);
                    }
                });
                return;
            }
            */
        }

        // work with new window
        createNewTabLayout(screen);
    }

    protected WindowBreadCrumbs createWindowBreadCrumbs(@SuppressWarnings("unused") Screen screen) {
        WebAppWorkArea appWorkArea = getConfiguredWorkArea();
        WindowBreadCrumbs windowBreadCrumbs = new WindowBreadCrumbs(appWorkArea);

        boolean showBreadCrumbs = webConfig.getShowBreadCrumbs() || appWorkArea.getMode() == AppWorkArea.Mode.SINGLE;
        windowBreadCrumbs.setVisible(showBreadCrumbs);

        return windowBreadCrumbs;
    }

    protected void createNewTabLayout(Screen screen) {
        WindowBreadCrumbs breadCrumbs = createWindowBreadCrumbs(screen);
        breadCrumbs.setWindowNavigateHandler(this::handleWindowBreadCrumbsNavigate);
        breadCrumbs.addWindow(screen.getWindow());

        WindowContainer windowContainer = new WindowContainer();
        windowContainer.setPrimaryStyleName("c-app-window-wrap");
        windowContainer.setSizeFull();

        windowContainer.setBreadCrumbs(breadCrumbs);
        windowContainer.addComponent(breadCrumbs);

        Window window = screen.getWindow();

        com.vaadin.ui.Component windowComposition = window.unwrapComposition(com.vaadin.ui.Component.class);
        windowContainer.addComponent(windowComposition);

        WebAppWorkArea workArea = getConfiguredWorkArea();

        if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
            windowContainer.addStyleName("c-app-tabbed-window");

            TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();

            String tabId;

            ScreenOptions options = ScreenUtils.getScreenOptions(screen);
            WindowInfo windowInfo = ScreenUtils.getWindowInfo(screen);

            com.vaadin.ui.ComponentContainer tab = findSameWindowTab(window, options);

            if (tab != null && !windowInfo.getMultipleOpen()) {
                tabSheet.replaceComponent(tab, windowContainer);
                tabSheet.removeComponent(tab);
                tabId = tabSheet.getTab(windowContainer);
            } else {
                tabId = "tab_" + uuidSource.createUuid();

                tabSheet.addTab(windowContainer, tabId);

                if (ui.isTestMode()) {
                    String id = "tab_" + window.getId();

                    tabSheet.setTabTestId(tabId, ui.getTestIdManager().getTestId(id));
                    tabSheet.setTabCubaId(tabId, id);
                }
            }
            String windowContentSwitchMode = window.getContentSwitchMode().name();
            ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(windowContentSwitchMode);
            tabSheet.setContentSwitchMode(tabId, contentSwitchMode);

            String formattedCaption = formatTabCaption(window.getCaption(), window.getDescription());
            tabSheet.setTabCaption(tabId, formattedCaption);
            String formattedDescription = formatTabDescription(window.getCaption(), window.getDescription());
            if (!Objects.equals(formattedCaption, formattedDescription)) {
                tabSheet.setTabDescription(tabId, formattedDescription);
            } else {
                tabSheet.setTabDescription(tabId, null);
            }

            tabSheet.setTabIcon(tabId, iconResolver.getIconResource(window.getIcon()));
            tabSheet.setTabClosable(tabId, true);
            tabSheet.setTabCloseHandler(windowContainer, this::handleTabWindowClose);
            tabSheet.setSelectedTab(windowContainer);
        } else {
            windowContainer.addStyleName("c-app-single-window");

            Layout mainLayout = workArea.getSingleWindowContainer();
            mainLayout.removeAllComponents();
            mainLayout.addComponent(windowContainer);
        }
    }

    protected void showThisTabWindow(Screen screen) {
        WebAppWorkArea workArea = getConfiguredWorkArea();
        workArea.switchTo(AppWorkArea.State.WINDOW_CONTAINER);

        WindowContainer windowContainer;
        if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
            TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
            windowContainer = (WindowContainer) tabSheet.getSelectedTab();
        } else {
            windowContainer = (WindowContainer) workArea.getSingleWindowContainer().getComponent(0);
        }

        WindowBreadCrumbs breadCrumbs = windowContainer.getBreadCrumbs();
        if (breadCrumbs == null) {
            throw new IllegalStateException("BreadCrumbs not found");
        }

        Window currentWindow = breadCrumbs.getCurrentWindow();

        windowContainer.removeComponent(currentWindow.unwrapComposition(com.vaadin.ui.Layout.class));

        Window newWindow = screen.getWindow();
        com.vaadin.ui.Component newWindowComposition = newWindow.unwrapComposition(com.vaadin.ui.Component.class);

        windowContainer.addComponent(newWindowComposition);

        breadCrumbs.addWindow(newWindow);

        if (workArea.getMode() == AppWorkArea.Mode.TABBED) {
            TabSheetBehaviour tabSheet = workArea.getTabbedWindowContainer().getTabSheetBehaviour();
            String tabId = tabSheet.getTab(windowContainer);

            String formattedCaption = formatTabCaption(newWindow.getCaption(), newWindow.getDescription());
            tabSheet.setTabCaption(tabId, formattedCaption);
            String formattedDescription = formatTabDescription(newWindow.getCaption(), newWindow.getDescription());

            if (!Objects.equals(formattedCaption, formattedDescription)) {
                tabSheet.setTabDescription(tabId, formattedDescription);
            } else {
                tabSheet.setTabDescription(tabId, null);
            }

            tabSheet.setTabIcon(tabId, iconResolver.getIconResource(newWindow.getIcon()));

            ContentSwitchMode contentSwitchMode = ContentSwitchMode.valueOf(newWindow.getContentSwitchMode().name());
            tabSheet.setContentSwitchMode(tabId, contentSwitchMode);
        } else {
            windowContainer.markAsDirtyRecursive();
        }
    }

    protected void showDialogWindow(Screen screen) {
        // todo
    }

    protected WebAppWorkArea getConfiguredWorkArea() {
        RootWindow topLevelWindow = ui.getTopLevelWindow();

        Screen controller = ((WindowImplementation) topLevelWindow).getController();

        if (controller instanceof HasWorkArea) {
            AppWorkArea workArea = ((HasWorkArea) controller).getWorkArea();
            if (workArea != null) {
                return (WebAppWorkArea) workArea;
            }
        }

        throw new IllegalStateException("RootWindow does not have any configured work area");
    }

    protected String formatTabCaption(String caption, String description) {
        String s = formatTabDescription(caption, description);
        int maxLength = webConfig.getMainTabCaptionLength();
        if (s.length() > maxLength) {
            return s.substring(0, maxLength) + "...";
        } else {
            return s;
        }
    }

    protected String formatTabDescription(String caption, String description) {
        if (!StringUtils.isEmpty(description)) {
            return String.format("%s: %s", caption, description);
        } else {
            return Strings.nullToEmpty(caption);
        }
    }

    protected void handleWindowBreadCrumbsNavigate(WindowBreadCrumbs breadCrumbs, Window window) {
        Runnable op = new Runnable() {
            @Override
            public void run() {
                Window currentWindow = breadCrumbs.getCurrentWindow();

                if (currentWindow != null && window != currentWindow) {
                    if (!isCloseWithCloseButtonPrevented(currentWindow)) {
                        currentWindow.closeAndRun(CLOSE_ACTION_ID, this);
                    }
                }
            }
        };
        op.run();
    }

    protected void handleTabWindowClose(HasTabSheetBehaviour targetTabSheet, com.vaadin.ui.Component tabContent) {
        WindowBreadCrumbs tabBreadCrumbs = ((WindowContainer) tabContent).getBreadCrumbs();

        if (!canWindowBeClosed(tabBreadCrumbs.getCurrentWindow())) {
            return;
        }

        Runnable closeTask = new TabCloseTask(tabBreadCrumbs);
        closeTask.run();

        // it is needed to force redraw tabsheet if it has a lot of tabs and part of them are hidden
        targetTabSheet.markAsDirty();
    }

    public class TabCloseTask implements Runnable {
        protected WindowBreadCrumbs breadCrumbs;

        public TabCloseTask(WindowBreadCrumbs breadCrumbs) {
            this.breadCrumbs = breadCrumbs;
        }

        @Override
        public void run() {
            Window windowToClose = breadCrumbs.getCurrentWindow();
            if (windowToClose != null) {
                if (!isCloseWithCloseButtonPrevented(windowToClose)) {
                    windowToClose.closeAndRun(CLOSE_ACTION_ID, new TabCloseTask(breadCrumbs));
                }
            }
        }
    }

    protected boolean isCloseWithShortcutPrevented(Window window) {
        BeforeCloseWithShortcutEvent event = new BeforeCloseWithShortcutEvent(window);
//        todo implement
//        webWindow.fireBeforeCloseWithShortcut(event);
        return event.isClosePrevented();
    }

    protected boolean isCloseWithCloseButtonPrevented(Window window) {
        BeforeCloseWithCloseButtonEvent event = new BeforeCloseWithCloseButtonEvent(window);
//        todo implement
//        webWindow.fireBeforeCloseWithCloseButton(event);
        return event.isClosePrevented();
    }

    protected boolean canWindowBeClosed(Window window) {
        if (webConfig.getDefaultScreenCanBeClosed()) {
            return true;
        }

        String defaultScreenId = webConfig.getDefaultScreenId();

        if (webConfig.getUserCanChooseDefaultScreen()) {
            String userDefaultScreen = userSettingService.loadSetting(ClientType.WEB, "userDefaultScreen");
            defaultScreenId = StringUtils.isEmpty(userDefaultScreen) ? defaultScreenId : userDefaultScreen;
        }

        return !window.getId().equals(defaultScreenId);
    }

    protected com.vaadin.ui.ComponentContainer findSameWindowTab(Window window, ScreenOptions options) {
        WebAppWorkArea workArea = getConfiguredWorkArea();

        TabSheetBehaviour tabSheetBehaviour = workArea.getTabbedWindowContainer().getTabSheetBehaviour();

        Iterator<com.vaadin.ui.Component> componentIterator = tabSheetBehaviour.getTabComponents();
        while (componentIterator.hasNext()) {
            WindowContainer component = (WindowContainer) componentIterator.next();
            Window currentWindow = component.getBreadCrumbs().getCurrentWindow();

//            todo include options hash into Window instance
//            if (hashCode.equals(getWindowHashCode(currentWindow))) {
//                return entry.getKey();
//            }
        }
        return null;
    }

    /**
     * Content of each tab of AppWorkArea TabSheet.
     */
    protected static class WindowContainer extends CssLayout {
        protected WindowBreadCrumbs breadCrumbs;

        public WindowBreadCrumbs getBreadCrumbs() {
            return breadCrumbs;
        }

        public void setBreadCrumbs(WindowBreadCrumbs breadCrumbs) {
            this.breadCrumbs = breadCrumbs;
        }
    }
}
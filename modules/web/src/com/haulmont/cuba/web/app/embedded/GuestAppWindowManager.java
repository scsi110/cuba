package com.haulmont.cuba.web.app.embedded;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.annotation.RemoteEntity;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;
import com.haulmont.cuba.web.app.embedded.window.RemoteEditor;
import com.haulmont.cuba.web.app.embedded.window.RemoteLookup;
import com.haulmont.cuba.web.app.embedded.window.RemoteWindowManager;
import com.haulmont.cuba.web.app.embedded.window.RemoteWindowManager.RemoteDialogAction;
import com.haulmont.cuba.web.app.embedded.window.RemoteWindowManager.RemoteDialogHandler;
import com.haulmont.cuba.web.sys.WindowBreadCrumbs;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component(GuestAppWindowManager.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GuestAppWindowManager extends WebWindowManager implements RemoteWindowStack {
    public static final String NAME = "cuba_GuestWindowManager";

    private RemoteWindowManager remoteWindowManager;

    private RemoteWindowStackListener stackListener;

    private RemoteApp hostApp;

    @PostConstruct
    public void init() {
        hostApp = new RemoteApp(RemoteApp.HOST_APP_NAME);
        hostApp.register(this, RemoteWindowStack.class);

        remoteWindowManager = hostApp.get(RemoteWindowManager.class);
        stackListener = hostApp.get(RemoteWindowStackListener.class);
    }

    @Override
    public void showOptionDialog(String title, String message, Frame.MessageType messageType, Action[] actions) {
        Preconditions.checkArgument(Arrays.stream(actions).allMatch(action -> action instanceof BaseAction));
        RemoteDialogAction[] remoteActions = new RemoteDialogAction[actions.length];
        for (int i = 0; i < actions.length; i++) {
            RemoteDialogAction action = remoteActions[i] = new RemoteDialogAction();
            action.caption = actions[i].getCaption();
            if (actions[i] instanceof DialogAction) {
                action.type = ((DialogAction) actions[i]).getType();
            }
            action.id = Integer.toString(i);
        }

        RemoteDialogHandler handler = id -> actions[Integer.parseInt(id)].actionPerform(null);

        remoteWindowManager.showOptionsDialog(title, message, messageType, remoteActions, handler);
    }

    @Override
    public void showNotification(String caption, String description, Frame.NotificationType type) {
        remoteWindowManager.showNotification(caption, description, type);
    }

    @Override
    protected void showWindow(Window window, String caption, String description, OpenType type, boolean multipleOpen) {
        super.showWindow(window, caption, description, type, multipleOpen);
        stackListener.onWindowOpened(window.getCaption(), window.getDescription());
    }

    @Override
    protected void closeWindow(Window window, WindowOpenInfo openInfo) {
        super.closeWindow(window, openInfo);
        stackListener.onWindowClosed(Window.CLOSE_ACTION_ID);
    }

    @Override
    protected WindowBreadCrumbs createWindowBreadCrumbs(Window window) {
        WindowBreadCrumbs breadCrumbs = super.createWindowBreadCrumbs(window);
        breadCrumbs.setVisible(false);
        return breadCrumbs;
    }

    public RemoteLookup openRemoteLookup(Class<? extends Entity> entityClass, OpenMode openMode, Map<String, Object> screenParams) {
        RemoteEntity remoteEntityAnnotation = entityClass.getAnnotation(RemoteEntity.class);
        remoteWindowManager.openLookup(remoteEntityAnnotation.app(), remoteEntityAnnotation.lookup(), openMode, screenParams);
        return new RemoteLookup(hostApp);
    }

    public RemoteEditor openRemoteEditor(Class<? extends Entity> entityClass, String item, OpenMode openMode, Map<String, Object> screenParams) {
        RemoteEntity remoteEntityAnnotation = entityClass.getAnnotation(RemoteEntity.class);
        remoteWindowManager.openEditor(remoteEntityAnnotation.app(), remoteEntityAnnotation.editor(), item, openMode, screenParams);
        return new RemoteEditor(hostApp);
    }

    public void openLookupFromHost(WindowInfo windowInfo, OpenType openType, Map<String, Object> paramsMap) {
        RemoteLookup.RemoteLookupListener listener = hostApp.get(RemoteLookup.RemoteLookupListener.class);
        openLookup(windowInfo, new ConvertingLookup(listener), openType, paramsMap);
    }

    public void openEditorFromHost(WindowInfo windowInfo, Entity entity, OpenType openType, Map<String, Object> paramsMap) {
        RemoteEditor.RemoteEditorListener editorListener = hostApp.get(RemoteEditor.RemoteEditorListener.class);

        Window.Editor editor = openEditor(windowInfo, entity, openType, paramsMap);
        editor.addCloseListener(actionId -> {
            if (actionId.equals(Window.COMMIT_ACTION_ID)) {
                RemoteEntityInfo entityInfo = RemoteEntityInfo.from((BaseUuidEntity) editor.getItem());
                editorListener.onCommit(entityInfo);
            }
            editorListener.onClose(actionId);
        });
    }

    @Override
    public void popStack() {
        WindowBreadCrumbs breadCrumbs = tabs.values().iterator().next();
        Window window = breadCrumbs.getCurrentWindow();
        if (window instanceof Window.Wrapper) {
            window = ((Window.Wrapper) window).getWrappedWindow();
        }

        WindowOpenInfo openInfo = windowOpenMode.get(window);
        super.closeWindow(window, openInfo);
    }

    private class ConvertingLookup implements Window.Lookup.Handler {
        private RemoteLookup.RemoteLookupListener remoteListener;

        ConvertingLookup(RemoteLookup.RemoteLookupListener remoteListener) {
            this.remoteListener = remoteListener;
        }

        @Override
        public void handleLookup(Collection items) {
            RemoteEntityInfo[] infos = ((Collection<BaseUuidEntity>) items).stream()
                    .map(RemoteEntityInfo::from)
                    .collect(Collectors.toList()).toArray(new RemoteEntityInfo[items.size()]);

            remoteListener.handleLookup(infos);
        }
    }
}

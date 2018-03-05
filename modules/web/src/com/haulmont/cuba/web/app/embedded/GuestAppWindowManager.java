package com.haulmont.cuba.web.app.embedded;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.cuba.web.app.embedded.RemoteWindowManager.RemoteDialogAction;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;
import com.haulmont.cuba.web.app.embedded.window.RemoteLookupHandler;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GuestAppWindowManager extends WebWindowManager {

    private RemoteWindowManager remoteWindowManager;

    private RemoteApp hostApp;

    @PostConstruct
    public void init() {
        hostApp = new RemoteApp(RemoteApp.HOST_APP_NAME);
        remoteWindowManager = hostApp.get(RemoteWindowManager.class);
    }

    @Override
    public Window.Lookup openLookup(WindowInfo windowInfo, Window.Lookup.Handler handler, OpenType openType, Map<String, Object> params) {
        //        todo breadcrumbs
        return super.openLookup(windowInfo, handler, openType, params);
    }

    @Override
    public Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType, Map<String, Object> params, Datasource parentDs) {
        //        todo breadcrumbs
        return super.openEditor(windowInfo, item, openType, params, parentDs);
    }

    @Override
    public void showOptionDialog(String title, String message, Frame.MessageType messageType, Action[] actions) {
        Preconditions.checkArgument(Arrays.stream(actions).allMatch(action -> action instanceof DialogAction));
        RemoteDialogAction[] remoteActions = new RemoteDialogAction[actions.length];
        for (int i = 0; i < actions.length; i++) {
            RemoteDialogAction action = remoteActions[i] = new RemoteDialogAction();
            action.caption = actions[i].getCaption();
            action.type = ((DialogAction) actions[i]).getType();
            action.id = Integer.toString(i);
        }

        RemoteWindowManager.RemoteDialogHandler handler = id -> actions[Integer.parseInt(id)].actionPerform(null);

        remoteWindowManager.showOptionsDialog(title, message, messageType, remoteActions, handler);
    }

    @Override
    public void showNotification(String caption, String description, Frame.NotificationType type) {
        remoteWindowManager.showNotification(caption, description, type);
    }

    @Override
    protected Component showWindowNewTab(Window window, boolean multipleOpen) {
        //        todo breadcrumbs
        return super.showWindowNewTab(window, multipleOpen);
    }

    @Override
    protected Component showWindowThisTab(Window window, String caption, String description) {
//        todo breadcrumbs
        return super.showWindowThisTab(window, caption, description);
    }

    @Override
    protected Component showWindowDialog(Window window, OpenType openType, boolean forciblyDialog) {
        return super.showWindowDialog(window, openType, forciblyDialog);
    }

    public void openRemoteLookup(String appName, String screenAlias, RemoteLookupHandler handler, OpenMode openMode, Map<String, Object> screenParams) {
        remoteWindowManager.openLookup(appName, screenAlias, handler, openMode, screenParams);
    }

    public void openRemoteEditor(String appName, String screenAlias, String item, RemoteLookupHandler handler, OpenMode openMode, Map<String, Object> screenParams) {
        remoteWindowManager.openEditor(appName, screenAlias, item, handler, openMode, screenParams);
    }

    public void openLookupFromHost(WindowInfo windowInfo, OpenType openType, Map<String, Object> paramsMap) {
        openLookup(windowInfo, new ConvertingLookup(), openType, paramsMap);
    }

    public void openEditorFromHost(WindowInfo windowInfo, Entity entity, OpenType openType, Map<String, Object> paramsMap) {
        Window.Editor editor = openEditor(windowInfo, entity, openType, paramsMap);
        editor.addCloseWithCommitListener(() -> {
            RemoteEntityInfo entityInfo = RemoteEntityInfo.from((BaseUuidEntity) editor.getItem());
            hostApp.get(RemoteLookupHandler.class).handleLookup(new RemoteEntityInfo[] {entityInfo});
        });
    }

    private class ConvertingLookup implements Window.Lookup.Handler {
        @Override
        public void handleLookup(Collection items) {
            RemoteEntityInfo[] infos = ((Collection<BaseUuidEntity>) items).stream()
                    .map(RemoteEntityInfo::from)
                    .collect(Collectors.toList()).toArray(new RemoteEntityInfo[items.size()]);

            hostApp.get(RemoteLookupHandler.class).handleLookup(infos);
        }
    }
}

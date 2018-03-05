package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.app.embedded.transport.RemoteCallback;
import com.haulmont.cuba.web.app.embedded.window.RemoteLookupHandler;

import java.util.Map;

public interface RemoteWindowManager {

    void openLookup(String appName, String screenAlias, @RemoteCallback RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> remoteScreenParams);

    void openEditor(String appName, String screenAlias, String entityId, @RemoteCallback RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> remoteScreenParams);

    void showOptionsDialog(String title, String message, Frame.MessageType messageType, RemoteDialogAction[] actions, @RemoteCallback RemoteDialogHandler handler);

    void showNotification(String caption, String description, Frame.NotificationType type);

    interface RemoteDialogHandler {
        void onAction(String id);
    }

    class RemoteDialogAction {
        public String id;
        public String caption;
        public DialogAction.Type type;
    }
}

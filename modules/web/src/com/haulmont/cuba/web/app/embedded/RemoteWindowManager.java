package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.app.embedded.lookup.RemoteLookupHandler;
import com.haulmont.cuba.web.app.embedded.transport.RemoteCallback;

import java.util.Map;

public interface RemoteWindowManager {

    void openLookup(String appName, String entityName, @RemoteCallback RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> screenParams);

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

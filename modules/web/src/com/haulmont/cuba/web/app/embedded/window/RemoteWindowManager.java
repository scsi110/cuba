package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;
import com.haulmont.cuba.web.app.embedded.transport.RemoteCallback;

import java.util.Map;

public interface RemoteWindowManager {
    void openLookup(String appName, String screenAlias, @RemoteCallback RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> remoteScreenParams);

    void openEditor(String appName, String screenAlias, String entityId, @RemoteCallback RemoteLookupHandler handler, WindowManager.OpenMode openType, Map<String, Object> remoteScreenParams);

    void showOptionsDialog(String title, String message, Frame.MessageType messageType, RemoteDialogAction[] actions, @RemoteCallback RemoteDialogHandler handler);

    void showNotification(String caption, String description, Frame.NotificationType type);

    void guestWindowOpened(String caption, String description);

    void remoteWindowClosed(String actionId);

    interface RemoteDialogHandler {
        void onAction(String id);
    }

    interface RemoteLookupHandler {
        void handleLookup(RemoteEntityInfo[] items);
    }

    class RemoteDialogAction {
        public String id;
        public String caption;
        public DialogAction.Type type = DialogAction.Type.CANCEL;

        public DialogAction createDialogAction(RemoteDialogHandler handler) {
            return (DialogAction) new DialogAction(type)
                    .withCaption(caption)
                    .withHandler(actionPerformedEvent -> handler.onAction(id));
        }
    }
}

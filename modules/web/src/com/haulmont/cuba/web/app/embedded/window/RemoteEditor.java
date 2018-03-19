package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;

public class RemoteEditor extends RemoteWindow {
    protected RemoteEntityInfo committedInstance;

    public RemoteEditor(RemoteApp remoteApp) {
        super(remoteApp);
    }

    @Override
    protected void registerWindowListener(RemoteApp remoteApp) {
        remoteApp.register(new RemoteEditorListenerImpl(), RemoteEditorListener.class);
    }

    public RemoteEntityInfo getCommittedInstance() {
        return committedInstance;
    }

    public interface RemoteEditorListener extends RemoteWindowListener {

        void onCommit(RemoteEntityInfo entityInfo);

    }

    protected class RemoteEditorListenerImpl extends RemoteWindowListenerImpl implements RemoteEditorListener {

        @Override
        public void onCommit(RemoteEntityInfo entityInfo) {
            committedInstance = entityInfo;
        }
    }
}

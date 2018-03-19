package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;

import java.util.Arrays;

public class RemoteLookup extends RemoteWindow {

    protected Window.Lookup.Handler handler;

    public RemoteLookup(RemoteApp remoteApp) {
        super(remoteApp);
    }

    @Override
    protected void registerWindowListener(RemoteApp remoteApp) {
        remoteApp.register(new RemoteLookupListenerImpl(), RemoteLookupListener.class);
    }

    public void setLookupHandler(Window.Lookup.Handler handler) {
        this.handler = handler;
    }

    public interface RemoteLookupListener extends RemoteWindowListener {
        void handleLookup(RemoteEntityInfo[] items);
    }

    protected class RemoteLookupListenerImpl extends RemoteWindowListenerImpl implements RemoteLookupListener {

        @Override
        public void handleLookup(RemoteEntityInfo[] items) {
            if (handler != null) {
                handler.handleLookup(Arrays.asList(items));
            }
        }
    }
}

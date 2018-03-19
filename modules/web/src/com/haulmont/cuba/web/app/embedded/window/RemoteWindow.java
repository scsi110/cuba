package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.app.embedded.transport.RemoteApp;

import java.util.LinkedList;
import java.util.List;

public class RemoteWindow {

    protected List<Window.CloseListener> listeners = null;

    public RemoteWindow(RemoteApp remoteApp) {
        registerWindowListener(remoteApp);
    }

    protected void registerWindowListener(RemoteApp remoteApp) {
        remoteApp.register(new RemoteWindowListenerImpl(), RemoteWindowListener.class);
    }

    public void addCloseListener(Window.CloseListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeCloseListener(Window.CloseListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected boolean onClose(String actionId) {
        fireWindowClosed(actionId);
        return true;
    }

    protected void fireWindowClosed(String actionId) {
        if (listeners != null) {
            for (Object listener : listeners) {
                if (listener instanceof Window.CloseListener) {
                    ((Window.CloseListener) listener).windowClosed(actionId);
                }
            }
        }
    }

    public interface RemoteWindowListener {
        void onClose(String actionId);
    }

    protected class RemoteWindowListenerImpl implements RemoteWindowListener {

        @Override
        public void onClose(String actionId) {
            RemoteWindow.this.onClose(actionId);
        }
    }
}

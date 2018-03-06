package com.haulmont.cuba.web.app.embedded.window;

public interface RemoteWindow {
    void addCloseListener(CloseListener closeListener);

    interface CloseListener {
        void onClose(String actionId);
    }
}

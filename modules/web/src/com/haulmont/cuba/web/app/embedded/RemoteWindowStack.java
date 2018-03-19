package com.haulmont.cuba.web.app.embedded;

public interface RemoteWindowStack {
    void popStack();

    interface RemoteWindowStackListener {
        void onWindowOpened(String caption, String description);

        void onWindowClosed(String actionId);
    }
}

package com.haulmont.cuba.web.app.embedded.window;

public interface RemoteEditor extends RemoteWindow {
    void addCommitListener(CommitListener commitListener);

    interface CommitListener {
        void onCommit();
    }
}

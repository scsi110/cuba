package com.haulmont.cuba.web.app.embedded.window;

public interface RemoteLookup extends RemoteWindow {
    void addLookupHandler(RemoteWindowManager.RemoteLookupHandler handler);
}

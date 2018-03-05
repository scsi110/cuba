package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;

public interface RemoteLookupHandler {
    void handleLookup(RemoteEntityInfo[] items);
}

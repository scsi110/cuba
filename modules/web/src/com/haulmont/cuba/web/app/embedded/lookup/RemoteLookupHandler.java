package com.haulmont.cuba.web.app.embedded.lookup;

import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;

public interface RemoteLookupHandler {
    void handleLookup(RemoteEntityInfo[] items);
}

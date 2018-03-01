package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.gui.config.WindowInfo;

public class RemoteWindowInfo extends WindowInfo {
    private final String appName;
    private final String remoteScreenId;
    private boolean isLookup = false;

    public RemoteWindowInfo(WindowInfo windowInfo, String appName, String remoteScreenId) {
        super(windowInfo.getId(), windowInfo.getDescriptor(), windowInfo.getScreenAgent());
        this.appName = appName;
        this.remoteScreenId = remoteScreenId;
    }

    public String getAppName() {
        return appName;
    }

    public String getRemoteScreenId() {
        return remoteScreenId;
    }

    public boolean isLookup() {
        return isLookup;
    }

    public void setLookup(boolean lookup) {
        isLookup = lookup;
    }
}

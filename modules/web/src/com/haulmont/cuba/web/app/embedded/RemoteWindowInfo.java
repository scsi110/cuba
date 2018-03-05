package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.gui.config.WindowInfo;

public class RemoteWindowInfo extends WindowInfo {
    private final String appName;
    private final String remoteScreenId;

    private String remoteItem;
    private RemoteWindowMode remoteWindowMode = RemoteWindowMode.DEFAULT;

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

    public String getRemoteItem() {
        return remoteItem;
    }

    public void setRemoteItem(String remoteItem) {
        this.remoteItem = remoteItem;
    }

    public RemoteWindowMode getRemoteWindowMode() {
        return remoteWindowMode;
    }

    public void setRemoteWindowMode(RemoteWindowMode remoteWindowMode) {
        this.remoteWindowMode = remoteWindowMode;
    }

    public enum RemoteWindowMode {
        DEFAULT,
        LOOKUP,
        EDITOR
    }
}

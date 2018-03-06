package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.gui.config.DeviceInfo;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.web.app.embedded.window.GuestWindowHolder;

import javax.annotation.Nullable;

public class HostAppWindowConfig extends WindowConfig {

    @Nullable
    @Override
    public WindowInfo findWindowInfo(String id, @Nullable DeviceInfo deviceInfo) {
        if (id.contains("/")) {
            String[] parts = id.split("/");
            String appName = parts[0];
            String remoteScreenId = parts[1];

            id = GuestWindowHolder.SCREEN_ID;

            WindowInfo windowInfo = super.findWindowInfo(id, deviceInfo);

            assert windowInfo != null;

            return new RemoteWindowInfo(windowInfo, appName, remoteScreenId);
        }
        return super.findWindowInfo(id, deviceInfo);

    }
}

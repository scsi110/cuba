package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.EntityAccessException;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.sys.LinkHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GuestAppLinkHandler extends LinkHandler {
    private final Logger log = LoggerFactory.getLogger(LinkHandler.class);

    public GuestAppLinkHandler(App app, String action, Map<String, String> requestParams) {
        super(app, action, requestParams);
    }

    @Override
    public void handle() {
        String appId = requestParams.get("appId");
        UserSession userSession = app.getConnection().getSession();
        if (appId != null && userSession != null) {
            userSession.setAttribute("appId", appId);
        }

        super.handle();
    }

    @Override
    protected void openWindow(WindowInfo windowInfo, Map<String, String> requestParams) {
        String itemStr = requestParams.get("item");
        String openTypeParam = requestParams.get("openType");
        WindowManager.OpenType openType = WindowManager.OpenType.NEW_TAB;

        String remoteWindowModeName = requestParams.get("remoteWindowMode");
        RemoteWindowInfo.RemoteWindowMode remoteWindowMode = remoteWindowModeName != null ? RemoteWindowInfo.RemoteWindowMode.valueOf(remoteWindowModeName) : null;

        if (StringUtils.isNotEmpty(openTypeParam)) {
            try {
                openType = WindowManager.OpenType.valueOf(openTypeParam);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown open type ({}) in request parameters", openTypeParam);
            }
        }

        Entity entity = null;
        if (itemStr != null) {
            EntityLoadInfo info = EntityLoadInfo.parse(itemStr);
            if (info == null) {
                log.warn("Invalid item definition: {}", itemStr);
            } else {
                entity = loadEntityInstance(info);
            }
            if (entity == null) {
                throw new EntityAccessException();
            }
        }

        if (remoteWindowMode != null) {
            GuestAppWindowManager windowManager = (GuestAppWindowManager) app.getWindowManager();
            switch (remoteWindowMode) {
                case LOOKUP:
                    windowManager.openLookupFromHost(windowInfo, openType, getParamsMap(requestParams));
                    break;
                case EDITOR:
                    windowManager.openEditorFromHost(windowInfo, entity, openType, getParamsMap(requestParams));
                    break;
                case DEFAULT:
                    windowManager.openWindow(windowInfo, openType, getParamsMap(requestParams));
                    break;
            }
        } else {
            if (itemStr == null) {
                app.getWindowManager().openWindow(windowInfo, openType, getParamsMap(requestParams));
            } else {
                EntityLoadInfo info = EntityLoadInfo.parse(itemStr);
                if (info == null) {
                    log.warn("Invalid item definition: {}", itemStr);
                } else {
                    app.getWindowManager().openEditor(windowInfo, entity, openType, getParamsMap(requestParams));
                }
            }
        }
    }
}

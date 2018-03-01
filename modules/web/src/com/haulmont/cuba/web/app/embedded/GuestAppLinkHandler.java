package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.EntityAccessException;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.gui.WindowManager;
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

        boolean asRemoteLookup = requestParams.get("asLookup") != null && Boolean.parseBoolean(requestParams.get("asLookup"));

        if (StringUtils.isNotEmpty(openTypeParam)) {
            try {
                openType = WindowManager.OpenType.valueOf(openTypeParam);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown open type ({}) in request parameters", openTypeParam);
            }
        }

        if (itemStr == null) {
            if (asRemoteLookup) {
                ((GuestAppWindowManager) app.getWindowManager()).openLookupFromHost(windowInfo, openType, getParamsMap(requestParams));
            } else {
                app.getWindowManager().openWindow(windowInfo, openType, getParamsMap(requestParams));
            }
        } else {
            EntityLoadInfo info = EntityLoadInfo.parse(itemStr);
            if (info == null) {
                log.warn("Invalid item definition: {}", itemStr);
            } else {
                Entity entity = loadEntityInstance(info);
                if (entity != null)
                    app.getWindowManager().openEditor(windowInfo, entity, openType, getParamsMap(requestParams));
                else
                    throw new EntityAccessException();
            }
        }
    }
}

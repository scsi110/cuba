package com.haulmont.cuba.web.app.embedded.window;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.BrowserFrame;
import com.haulmont.cuba.gui.components.UrlResource;
import com.haulmont.cuba.web.app.embedded.RemoteWindowInfo;
import com.haulmont.cuba.web.gui.components.WebUrlResource;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class GuestWindowHolder extends AbstractWindow {
    public static final String SCREEN_ID = "remote-window-holder";

    private static final Logger log = LoggerFactory.getLogger(GuestWindowHolder.class);

    @Inject
    private BrowserFrame embeddingTarget;

    @WindowParam(name = "screenAlias")
    private String screenAlias;

    @WindowParam(name = "appUrl")
    private String appUrl;

    @WindowParam(name = "paramsMap")
    private Map<String, String> paramsMap;

    @WindowParam(name = "appId")
    private String appId;

    @WindowParam(name = "item")
    private String item;

    @WindowParam(name = "remoteWindowMode")
    private RemoteWindowInfo.RemoteWindowMode remoteWindowMode;

    @Override
    public void init(Map<String, Object> params) {
        try {
            embeddingTarget.setSource(getResource());
        } catch (MalformedURLException e) {
            log.warn("External resource malformed URL", e);
            showNotification("External resource malformed URL", NotificationType.ERROR);
        }
    }

    private UrlResource getResource() throws MalformedURLException {
        String url = appUrl + "/app/open" +
                "?screen=" + screenAlias +
                "&appId=" + appId +
                "&remoteWindowMode=" + remoteWindowMode.name();

        if (!CollectionUtils.isEmpty(paramsMap)) {
            url += "&params=" + getParams();
        }

        if (item != null) {
            url += "&item=" + item;
        }

        return new WebUrlResource().setUrl(new URL(url));
    }

    private String getParams() {

        return paramsMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ':' + entry.getValue())
                .collect(Collectors.joining(","));
    }

    public enum OpenType {
        STANDARD, LOOKUP, EDITOR
    }
}
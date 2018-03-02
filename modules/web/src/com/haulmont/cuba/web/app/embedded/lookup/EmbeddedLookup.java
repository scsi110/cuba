package com.haulmont.cuba.web.app.embedded.lookup;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.BrowserFrame;
import com.haulmont.cuba.gui.components.UrlResource;
import com.haulmont.cuba.web.gui.components.WebUrlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class EmbeddedLookup extends AbstractLookup {
    public static final String SCREEN_ID = "embeddedLookup";

    private static final Logger log = LoggerFactory.getLogger(EmbeddedLookup.class);

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

    @WindowParam(name = "urlParams")
    private String urlParams = "";

    @Override
    public void init(Map<String, Object> params) {
        embeddingTarget.setSource(getResource());
    }

    private UrlResource getResource() {
        try {
            return new WebUrlResource().setUrl(new URL(appUrl + "/app/open?screen=" + screenAlias + "&appId=" + appId + getParams() + urlParams));
        } catch (MalformedURLException e) {
            log.warn("External resource malformed URL", e);
            showNotification("External resource malformed URL", NotificationType.ERROR);
            return null;
        }
    }

    private String getParams() {
        if (CollectionUtils.isEmpty(paramsMap)) {
            return "";
        }
        return paramsMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ':' + entry.getValue())
                .collect(Collectors.joining(",", "&params=", ""));
    }
}
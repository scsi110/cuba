package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.web.sys.CubaBootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import org.jsoup.nodes.Element;

import javax.inject.Inject;

public class EmbeddedAppBootstrapListener extends CubaBootstrapListener {
    @Inject
    private EmbedAppConfig embedAppConfig;

    @Override
    public void modifyBootstrapPage(BootstrapPageResponse response) {
        super.modifyBootstrapPage(response);

        Element head = response.getDocument().getElementsByTag("head").get(0);

        includeScript("VAADIN/resources/connector.js", response, head);
        if (embedAppConfig.isHostMode()) {
            includeScript("VAADIN/resources/embeddedAppsEventBus.js", response, head);
        }
    }
}

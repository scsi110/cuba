package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.type.Factory;
import com.haulmont.cuba.core.config.type.StringStringMapTypeFactory;

import java.util.Map;

@Source(type = SourceType.APP)
public interface EmbedAppConfig extends Config {
    @Property("cuba.embedded.isGuest")
    @Default("false")
    boolean isGuestMode();

    @Property("cuba.embedded.isHost")
    @Default("false")
    boolean isHostMode();


    @Property("cuba.embedded.appName")
    @Default("")
    String getAppName();

    @Property("cuba.embedded.apps")
    @Factory(factory = StringStringMapTypeFactory.class)
    @Default("")
    Map<String, String> getApps();
}

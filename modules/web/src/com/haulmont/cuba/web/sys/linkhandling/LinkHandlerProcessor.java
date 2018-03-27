package com.haulmont.cuba.web.sys.linkhandling;

import com.haulmont.cuba.web.App;

import java.util.Map;

public interface LinkHandlerProcessor {

    boolean canHandle(Map<String, String> requestParams, String action);

    void handle(Map<String, String> requestParams, String action, App app);
}

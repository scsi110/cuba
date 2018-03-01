package com.haulmont.cuba.web.app.embedded.transport;

public interface CallbackHolder {
    void execute(String callbackId, String jsonArgs);
}

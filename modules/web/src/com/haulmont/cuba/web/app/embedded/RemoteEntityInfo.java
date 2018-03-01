package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.core.entity.BaseUuidEntity;

import java.util.UUID;

public class RemoteEntityInfo {
    private UUID id;

    private String title;

    public RemoteEntityInfo(UUID id, String title) {
        this.id = id;
        this.title = title;
    }

    public static RemoteEntityInfo from(BaseUuidEntity entity) {
        return new RemoteEntityInfo(entity.getId(), entity.getInstanceName());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

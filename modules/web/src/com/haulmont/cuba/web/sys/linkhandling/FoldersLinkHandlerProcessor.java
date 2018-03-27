package com.haulmont.cuba.web.sys.linkhandling;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.app.folders.Folders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@Component(FoldersLinkHandlerProcessor.NAME)
@Order(0)
public class FoldersLinkHandlerProcessor implements LinkHandlerProcessor {

    public static final String NAME = "cuba_FoldersLinkHandlerProcessor";

    private final Logger log = LoggerFactory.getLogger(FoldersLinkHandlerProcessor.class);

    @Inject
    protected DataService dataService;

    @Inject
    protected Folders folders;

    @Override
    public boolean canHandle(Map<String, String> requestParams, String action) {
        return requestParams.containsKey("folder");
    }

    @Override
    public void handle(Map<String, String> requestParams, String action, App app) {
        String folderId = requestParams.get("folder");

        AbstractSearchFolder folder = loadFolder(UUID.fromString(folderId));
        if (folder != null) {
            folders.openFolder(folder);
        } else {
            log.warn("Folder not found: {}", folderId);
        }
    }

    protected AbstractSearchFolder loadFolder(UUID folderId) {
        return dataService.load(new LoadContext<>(AbstractSearchFolder.class).setId(folderId));
    }
}

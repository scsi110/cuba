package com.haulmont.cuba.web.sys.linkhandling;

import com.haulmont.cuba.web.App;

import java.util.Map;

/**
 * Interface that is used by {@link com.haulmont.cuba.web.sys.LinkHandler}
 * to handle links from outside of the application.
 * <br> {@link com.haulmont.cuba.web.sys.LinkHandler} traverses processors to find first able to handle link.
 * <br> To set processor priority use {@link org.springframework.core.annotation.Order @Order},
 * {@link org.springframework.core.Ordered} or {@link javax.annotation.Priority @Priority}.
 */
public interface LinkHandlerProcessor {

    /**
     * @return true if action with such request parameters should be handled by this processor.
     */
    boolean canHandle(Map<String, String> requestParams, String action);

    /**
     * Called to handle action.
     */
    void handle(Map<String, String> requestParams, String action, App app);
}

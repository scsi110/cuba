package com.haulmont.cuba.gui.sys;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.screen.ScreenXml;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.ScreenController;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

public final class ScreenUtils {

    private ScreenUtils() {
    }

    public static String getInferredScreenId(ScreenController screenController, Class<? extends Screen> annotatedScreenClass) {
        checkNotNullArgument(screenController);

        String id = screenController.value();
        if (Strings.isNullOrEmpty(id)) {
            id = screenController.id();

            if (Strings.isNullOrEmpty(id)) {
                throw new DevelopmentException("Screen class annotated with @ScreenController without id " + annotatedScreenClass);
            }
        }

        return id;
    }

    public static String getInferredDesignTemplate(ScreenXml screenXml, Class<? extends Screen> annotatedScreenClass) {
        checkNotNullArgument(screenXml);

        String templateLocation = screenXml.value();
        if (Strings.isNullOrEmpty(templateLocation)) {
            templateLocation = screenXml.path();

            if (Strings.isNullOrEmpty(templateLocation)) {
                throw new DevelopmentException("Screen class annotated with @ScreenXml without template: " + annotatedScreenClass);
            }
        }

        return templateLocation;
    }

    public static String getInferredSubscribeTarget(Subscribe subscribe) {
        checkNotNullArgument(subscribe);

        String target = subscribe.value();
        if (Strings.isNullOrEmpty(target)) {
            target = subscribe.target();
        }

        return target;
    }
}
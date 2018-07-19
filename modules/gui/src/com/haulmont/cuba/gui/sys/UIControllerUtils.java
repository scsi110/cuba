package com.haulmont.cuba.gui.sys;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.screen.Design;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UIController;

import javax.annotation.Nullable;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

public final class UIControllerUtils {

    private UIControllerUtils() {
    }

    public static String getInferredScreenId(UIController uiController, Class<? extends Screen> annotatedScreenClass) {
        checkNotNullArgument(uiController);

        String id = uiController.value();
        if (Strings.isNullOrEmpty(id)) {
            id = uiController.id();

            if (Strings.isNullOrEmpty(id)) {
                throw new DevelopmentException("Screen class annotated with @UIController without id " + annotatedScreenClass);
            }
        }

        return id;
    }

    public static String getInferredDesignTemplate(Design design, Class<? extends Screen> annotatedScreenClass) {
        checkNotNullArgument(design);

        String templateLocation = design.value();
        if (Strings.isNullOrEmpty(templateLocation)) {
            templateLocation = design.path();

            if (Strings.isNullOrEmpty(templateLocation)) {
                throw new DevelopmentException("Screen class annotated with @Design without template: " + annotatedScreenClass);
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
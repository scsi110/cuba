package com.haulmont.cuba.gui.screen;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JavaDoc
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    @AliasFor("value")
    String target() default "";

    @AliasFor("target")
    String value() default "";
}
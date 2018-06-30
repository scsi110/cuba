package com.haulmont.cuba.gui;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JavaDoc
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface UIController {
    @AliasFor("id")
    String value() default "";

    @AliasFor("value")
    String id() default "";

    boolean multipleOpen() default false;
}
package com.haulmont.cuba.gui.screen;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JavaDoc
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ScreenXml {
    @AliasFor("path")
    String value() default "";

    @AliasFor("value")
    String path() default "";
}
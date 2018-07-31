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
public @interface Subscribe {
    Target target() default Target.COMPONENT;

    @AliasFor("id")
    String value() default "";

    @AliasFor("value")
    String id() default "";
}
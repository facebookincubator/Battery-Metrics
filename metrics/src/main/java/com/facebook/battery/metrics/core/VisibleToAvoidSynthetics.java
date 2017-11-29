package com.facebook.battery.metrics.core;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given field, method, class, interface or enum has package private visibility
 * solely to prevent creation of synthetic accessors and that the visibility of members with this
 * annotation shouldn't be changed from package private.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface VisibleToAvoidSynthetics {
}

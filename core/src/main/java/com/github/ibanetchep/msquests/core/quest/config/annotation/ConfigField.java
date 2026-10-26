package com.github.ibanetchep.msquests.core.quest.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to define a configuration field of an objective.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigField {
    /**
     * Name of the key in the configuration.
     */
    String name();

    /**
     * If true, this field is required.
     */
    boolean required() default false;
}
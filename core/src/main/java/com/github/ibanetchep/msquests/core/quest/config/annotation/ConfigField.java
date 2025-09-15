package com.github.ibanetchep.msquests.core.quest.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation pour définir un champ de configuration d’un objectif.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigField {
    /**
     * Nom de la clé dans la configuration.
     */
    String name();

    /**
     * Si vrai, ce champ est obligatoire.
     */
    boolean required() default false;
}
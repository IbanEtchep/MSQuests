package com.github.ibanetchep.msquests.core.quest.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneOfFields {
    String[] value();
    String message() default "At least one of the fields must be provided %s";
}
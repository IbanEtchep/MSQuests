package com.github.ibanetchep.msquests.core.quest.config.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(AtLeastOneOfFields.Container.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneOfFields {
    String[] value();
    String message() default "At least one of the fields must be provided %s";

    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {
        AtLeastOneOfFields[] value();
    }
}

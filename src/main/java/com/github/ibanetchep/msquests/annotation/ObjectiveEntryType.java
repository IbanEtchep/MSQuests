package com.github.ibanetchep.msquests.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectiveEntryType {

    String type();

}

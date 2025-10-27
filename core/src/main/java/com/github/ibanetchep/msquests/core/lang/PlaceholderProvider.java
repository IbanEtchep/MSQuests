package com.github.ibanetchep.msquests.core.lang;

import java.util.Map;

public interface PlaceholderProvider {
    Map<String, String> getPlaceholders(Translator translator);
}

package com.github.ibanetchep.msquests.bukkit.text.placeholder;

@FunctionalInterface
public interface PlaceholderResolver<T> {
    String resolve(String template, T object);
}

package com.github.ibanetchep.artisanattribution.config;

/** config.yml cannot be used; the message says where and why, for the server owner. */
public final class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

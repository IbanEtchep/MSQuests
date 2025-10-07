package com.github.ibanetchep.msquests.bukkit.config;

public record GlobalConfig(String language, DatabaseConfig databaseConfig) {
    public record DatabaseConfig(String type, String host, int port, String name, String user, String password) {}
}

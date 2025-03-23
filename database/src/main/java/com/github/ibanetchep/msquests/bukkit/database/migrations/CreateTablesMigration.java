package com.github.ibanetchep.msquests.bukkit.database.migrations;

import com.github.ibanetchep.msquests.bukkit.database.Migration;
import org.jdbi.v3.core.Jdbi;

public class CreateTablesMigration extends Migration {

    public CreateTablesMigration(Jdbi jdbi) {
        super(jdbi, 1);
    }

    @Override
    public void migrate() {
        jdbi.useHandle(handle -> {
            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_actor (
                id UUID PRIMARY KEY,
                actor_type VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_definition (
                id UUID PRIMARY KEY,
                name VARCHAR(255),
                description VARCHAR(255),
                tags JSON,
                rewards JSON,
                duration INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_objective_definition (
                id UUID PRIMARY KEY,
                objective_type VARCHAR(255),
                config JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                quest_definition_id UUID REFERENCES msquests_definition(id) ON DELETE CASCADE
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_quest (
                id UUID PRIMARY KEY,
                quest_status VARCHAR(255),
                started_at TIMESTAMP,
                expires_at TIMESTAMP,
                completed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                quest_definition_id UUID REFERENCES msquests_definition(id) ON DELETE CASCADE,
                actor_id UUID REFERENCES msquests_actor(id) ON DELETE CASCADE
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_objective (
                id UUID PRIMARY KEY,
                objective_status VARCHAR(255),
                progress INTEGER,
                started_at TIMESTAMP,
                completed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                quest_id UUID REFERENCES msquests_quest(id) ON DELETE CASCADE,
                objective_definition_id UUID REFERENCES msquests_objective_definition(id) ON DELETE CASCADE
            )""");
        });
    }
}
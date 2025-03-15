package com.github.ibanetchep.msquests.database.migrations;

import com.github.ibanetchep.msquests.database.Migration;
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
                unique_id UUID PRIMARY KEY,
                actor_reference_id VARCHAR(255) UNIQUE,
                actor_type VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_quests (
                unique_id UUID PRIMARY KEY,
                name VARCHAR(255),
                description VARCHAR(255),
                tags JSON,
                rewards JSON,
                duration INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_objectives (
                unique_id UUID PRIMARY KEY,
                objective_type VARCHAR(255),
                config JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                quest_id UUID REFERENCES msquests_quests(unique_id) ON DELETE CASCADE
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_quest_entries (
                unique_id UUID PRIMARY KEY,
                quest_id UUID REFERENCES msquests_quests(unique_id) ON DELETE CASCADE,
                quest_status VARCHAR(255),
                started_at TIMESTAMP,
                expires_at TIMESTAMP,
                completed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                actor_id UUID REFERENCES msquests_actor(unique_id) ON DELETE CASCADE
            )""");

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_objective_entries (
                unique_id UUID PRIMARY KEY,
                objective_status VARCHAR(255),
                progress INTEGER,
                started_at TIMESTAMP,
                completed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                quest_entry_id UUID REFERENCES msquests_quest_entries(unique_id) ON DELETE CASCADE,
                objective_id UUID REFERENCES msquests_objectives(unique_id) ON DELETE CASCADE
            )""");
        });
    }
}
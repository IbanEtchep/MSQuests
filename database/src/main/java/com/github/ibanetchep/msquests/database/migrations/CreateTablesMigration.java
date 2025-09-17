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
            CREATE TABLE IF NOT EXISTS msquests_player (
                id CHAR(36) PRIMARY KEY,
                tracked_quest_id CHAR(36) DEFAULT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_actor (
                id CHAR(36) PRIMARY KEY,
                actor_type VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_quest (
                id CHAR(36) PRIMARY KEY,
                quest_key VARCHAR(255) NOT NULL,
                quest_group_key VARCHAR(255) NOT NULL,
                quest_status VARCHAR(50) NOT NULL,
                started_at TIMESTAMP,
                expires_at TIMESTAMP,
                completed_at TIMESTAMP,
                actor_id CHAR(36) NOT NULL REFERENCES msquests_actor(id) ON DELETE CASCADE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_objective (
                objective_key VARCHAR(255) NOT NULL,
                objective_status VARCHAR(50) NOT NULL,
                progress INTEGER DEFAULT 0,
                started_at TIMESTAMP,
                completed_at TIMESTAMP,
                quest_id CHAR(36) NOT NULL REFERENCES msquests_quest(id) ON DELETE CASCADE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (objective_key, quest_id)
            )
            """);
        });
    }
}
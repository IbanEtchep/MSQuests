package com.github.ibanetchep.msquests.database.migrations;

import com.github.ibanetchep.msquests.database.Migration;
import org.jdbi.v3.core.Jdbi;

/**
 * Schema v3: the {@code artisan_quests_*} tables, next to the {@code msquests_*} ones, which stay
 * untouched so the previous jar can still run on them. A quest row references its config by
 * {@code quest_uid} (immutable) instead of {@code (group_key, quest_key)} (renamable in the
 * editor), and an objective row carries its {@code stage_key}.
 *
 * <p>The tables start empty. {@link com.github.ibanetchep.msquests.database.LegacyDataCopier}
 * fills them from {@code msquests_*} once, with the uids chosen by the content conversion, the
 * first time the plugin starts with {@code content_source: artisan}.
 */
public class CreateArtisanTablesMigration extends Migration {

    public CreateArtisanTablesMigration(Jdbi jdbi) {
        super(jdbi, 3);
    }

    @Override
    public void migrate() {
        jdbi.useHandle(handle -> {
            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_profile (
                id CHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                tracked_quest_id CHAR(36) DEFAULT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_actor (
                id CHAR(36) PRIMARY KEY,
                actor_type VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            // quest_key and group_key are the names last saved, for logs and support only:
            // a row is resolved by quest_uid, so a renamed quest keeps its progress.
            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_quest (
                id CHAR(36) PRIMARY KEY,
                quest_uid VARCHAR(64) NOT NULL,
                quest_key VARCHAR(255) NOT NULL,
                group_key VARCHAR(255) NOT NULL,
                quest_status VARCHAR(50) NOT NULL,
                started_at TIMESTAMP NULL DEFAULT NULL,
                expires_at TIMESTAMP NULL DEFAULT NULL,
                completed_at TIMESTAMP NULL DEFAULT NULL,
                actor_id CHAR(36) NOT NULL REFERENCES artisan_quests_actor(id) ON DELETE CASCADE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_objective (
                quest_id CHAR(36) NOT NULL REFERENCES artisan_quests_quest(id) ON DELETE CASCADE,
                stage_key VARCHAR(255) NOT NULL,
                objective_key VARCHAR(255) NOT NULL,
                objective_status VARCHAR(50) NOT NULL,
                objective_type VARCHAR(255) NOT NULL,
                progress INTEGER DEFAULT 0,
                started_at TIMESTAMP NULL DEFAULT NULL,
                completed_at TIMESTAMP NULL DEFAULT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (quest_id, stage_key, objective_key)
            )
            """);

            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_rotation (
                id CHAR(36) PRIMARY KEY,
                actor_id CHAR(36) NOT NULL REFERENCES artisan_quests_actor(id) ON DELETE CASCADE,
                group_uid VARCHAR(64) NOT NULL,
                rotated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """);

            // One row once msquests_* has been copied: the copy never runs twice.
            handle.execute("""
            CREATE TABLE IF NOT EXISTS artisan_quests_data_copy (
                id INT PRIMARY KEY,
                copied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                summary VARCHAR(2000) NOT NULL
            )
            """);
        });
    }
}

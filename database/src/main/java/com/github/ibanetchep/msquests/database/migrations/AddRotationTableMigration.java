package com.github.ibanetchep.msquests.database.migrations;

import com.github.ibanetchep.msquests.database.Migration;
import org.jdbi.v3.core.Jdbi;

public class AddRotationTableMigration extends Migration {

    public AddRotationTableMigration(Jdbi jdbi) {
        super(jdbi, 2);
    }

    @Override
    public void migrate() {
        jdbi.useHandle(handle -> handle.execute("""
            CREATE TABLE IF NOT EXISTS msquests_rotation (
                id CHAR(36) PRIMARY KEY,
                actor_id CHAR(36) NOT NULL REFERENCES msquests_actor(id) ON DELETE CASCADE,
                group_key VARCHAR(255) NOT NULL,
                rotated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """));
    }
}

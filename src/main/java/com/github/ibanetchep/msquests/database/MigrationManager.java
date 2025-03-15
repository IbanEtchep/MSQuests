package com.github.ibanetchep.msquests.database;

import com.github.ibanetchep.msquests.database.migrations.CreateTablesMigration;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MigrationManager {

    private final Logger logger;
    private final Jdbi jdbi;
    private final List<Migration> migrations = new ArrayList<>();

    public MigrationManager(Jdbi jdbi, Logger logger) {
        this.jdbi = jdbi;
        this.logger = logger;
        migrations.add(new CreateTablesMigration(jdbi));
    }

    public void initialize() {
        createVersionTableIfNeeded();
        int currentVersion = getCurrentVersion();

        migrations.stream()
                .filter(m -> m.getVersion() > currentVersion)
                .forEach(this::executeMigration);
    }

    private void executeMigration(Migration migration) {
        try {
            jdbi.useTransaction(handle -> {
                migration.migrate();
                updateVersion(migration.getVersion());
            });
            logger.info("Migration {} completed successfully", migration.getVersion());
        } catch (Exception e) {
            logger.error("Migration {} failed", migration.getVersion(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    private void createVersionTableIfNeeded() {
        jdbi.useHandle(handle -> {
            handle.execute("""
                CREATE TABLE IF NOT EXISTS ms_quests_schema_version (
                    version INT NOT NULL DEFAULT 0
                )""");

            int count = handle.createQuery("SELECT COUNT(*) FROM ms_quests_schema_version")
                    .mapTo(Integer.class)
                    .one();

            if (count == 0) {
                handle.execute("INSERT INTO ms_quests_schema_version (version) VALUES (0)");
            }
        });
    }

    private int getCurrentVersion() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT version FROM ms_quests_schema_version")
                        .mapTo(Integer.class)
                        .one()
        );
    }

    private void updateVersion(int version) {
        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE ms_quests_schema_version SET version = :version")
                        .bind("version", version)
                        .execute()
        );
    }
}
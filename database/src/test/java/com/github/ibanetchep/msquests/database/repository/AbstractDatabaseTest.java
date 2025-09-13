package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Testcontainers
public abstract class AbstractDatabaseTest {

    @TempDir
    protected File tempDir;

    @SuppressWarnings("resource")
    @Container
    protected static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("msquests_test")
            .withUsername("test")
            .withPassword("test");

    protected DbAccess h2DbAccess;
    protected DbAccess mysqlDbAccess;
    protected List<TestFixture> fixtures = new ArrayList<>();

    @BeforeEach
    void setUpDatabases() {
        h2DbAccess = createH2DbAccess();
        mysqlDbAccess = createMysqlDbAccess();
    }

    @AfterEach
    void tearDownDatabases() {
        if (h2DbAccess != null) {
            h2DbAccess.closePool();
        }
        if (mysqlDbAccess != null) {
            mysqlDbAccess.closePool();
        }
    }

    protected DbAccess createH2DbAccess() {
        DbAccess dbAccess = new DbAccess();
        DbCredentials credentials = new DbCredentials(
                "h2",
                null,
                null,
                null,
                null,
                0,
                tempDir
        );
        dbAccess.initPool(credentials);
        return dbAccess;
    }

    protected DbAccess createMysqlDbAccess() {
        DbAccess dbAccess = new DbAccess();
        DbCredentials credentials = new DbCredentials(
                "mysql",
                mySQLContainer.getHost(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword(),
                mySQLContainer.getDatabaseName(),
                mySQLContainer.getFirstMappedPort(),
                tempDir
        );
        dbAccess.initPool(credentials);
        return dbAccess;
    }

    protected void loadFixtures() throws SQLException {
        clearAllTables();

        try (Connection conn = h2DbAccess.getDataSource().getConnection()) {
            for (TestFixture fixture : fixtures) {
                fixture.load(conn);
            }
        }

        try (Connection conn = mysqlDbAccess.getDataSource().getConnection()) {
            for (TestFixture fixture : fixtures) {
                fixture.load(conn);
            }
        }
    }

    protected void clearAllTables() throws SQLException {
        String[] tables = {"msquests_objective", "msquests_quest", "msquests_actor"};

        try (Connection conn = h2DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SET REFERENTIAL_INTEGRITY FALSE")) {
                stmt.execute();
            }

            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table)) {
                    stmt.executeUpdate();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("SET REFERENTIAL_INTEGRITY TRUE")) {
                stmt.execute();
            }
        }

        try (Connection conn = mysqlDbAccess.getDataSource().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0")) {
                stmt.execute();
            }

            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table)) {
                    stmt.executeUpdate();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1")) {
                stmt.execute();
            }
        }
    }

    protected void addFixture(TestFixture fixture) {
        fixtures.add(fixture);
    }

    protected interface TestFixture {
        void load(Connection connection) throws SQLException;
    }

    protected static class ActorFixture implements TestFixture {
        private final List<UUID> actorIds = new ArrayList<>();
        private final List<String> actorTypes = new ArrayList<>();

        public ActorFixture addActor(UUID id, String actorType) {
            actorIds.add(id);
            actorTypes.add(actorType);
            return this;
        }

        @Override
        public void load(Connection connection) throws SQLException {
            String sql = "INSERT INTO msquests_actor (id, actor_type) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < actorIds.size(); i++) {
                    stmt.setString(1, actorIds.get(i).toString());
                    stmt.setString(2, actorTypes.get(i));
                    stmt.executeUpdate();
                }
            }
        }
    }

    protected static class QuestFixture implements TestFixture {
        private final List<UUID> questIds = new ArrayList<>();
        private final List<String> questKeys = new ArrayList<>();
        private final List<String> questGroupKeys = new ArrayList<>();
        private final List<String> questStatuses = new ArrayList<>();
        private final List<UUID> actorIds = new ArrayList<>();

        public QuestFixture addQuest(UUID id, String key, String status, UUID actorId) {
            return addQuest(id, key, "default_group", status, actorId);
        }

        public QuestFixture addQuest(UUID id, String key, String groupKey, String status, UUID actorId) {
            questIds.add(id);
            questKeys.add(key);
            questGroupKeys.add(groupKey);
            questStatuses.add(status);
            actorIds.add(actorId);
            return this;
        }

        @Override
        public void load(Connection connection) throws SQLException {
            String sql = "INSERT INTO msquests_quest (id, quest_key, quest_group_key, quest_status, actor_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < questIds.size(); i++) {
                    stmt.setString(1, questIds.get(i).toString());
                    stmt.setString(2, questKeys.get(i));
                    stmt.setString(3, questGroupKeys.get(i));
                    stmt.setString(4, questStatuses.get(i));
                    stmt.setString(5, actorIds.get(i).toString());
                    stmt.executeUpdate();
                }
            }
        }
    }

    protected static class ObjectiveFixture implements TestFixture {
        private final List<String> objectiveKeys = new ArrayList<>();
        private final List<String> objectiveStatuses = new ArrayList<>();
        private final List<Integer> progresses = new ArrayList<>();
        private final List<UUID> questIds = new ArrayList<>();

        public ObjectiveFixture addObjective(String key, String status, int progress, UUID questId) {
            objectiveKeys.add(key);
            objectiveStatuses.add(status);
            progresses.add(progress);
            questIds.add(questId);
            return this;
        }

        @Override
        public void load(Connection connection) throws SQLException {
            String sql = "INSERT INTO msquests_objective (objective_key, objective_status, progress, quest_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < objectiveKeys.size(); i++) {
                    stmt.setString(1, objectiveKeys.get(i));
                    stmt.setString(2, objectiveStatuses.get(i));
                    stmt.setInt(3, progresses.get(i));
                    stmt.setString(4, questIds.get(i).toString());
                    stmt.executeUpdate();
                }
            }
        }
    }
}
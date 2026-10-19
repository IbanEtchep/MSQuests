package com.github.ibanetchep.artisanattribution.storage;

import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The answers, in the database Artisan lends to the suite (H2, MySQL or MariaDB). Plain JDBC
 * with statements every dialect runs the same: no upsert, an update first and an insert when
 * no row exists, so concurrent servers of a network stay consistent.
 *
 * <p>Every method blocks: call them off the game threads.
 */
public final class AttributionRepository {

    static final String TABLE = "attribution_players";
    static final String VERSION_TABLE = "attribution_schema_version";

    /** Forward only: append, never edit a released step. */
    private static final List<String> MIGRATIONS = List.of(
            "CREATE TABLE " + TABLE + " ("
                    + "player_uuid CHAR(36) NOT NULL PRIMARY KEY, "
                    + "source VARCHAR(64) NULL, "
                    + "answered_at BIGINT NULL, "
                    + "prompted_at BIGINT NULL, "
                    + "prompts INT NOT NULL DEFAULT 0)",
            "CREATE INDEX attribution_players_source ON " + TABLE + " (source)");

    private final DataSource dataSource;

    public AttributionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Creates or upgrades the tables. */
    public void migrate() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS " + VERSION_TABLE + " (version INT NOT NULL)");
            }
            int current;
            try (Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT MAX(version) FROM " + VERSION_TABLE)) {
                current = r.next() ? r.getInt(1) : 0;
            }
            for (int i = current; i < MIGRATIONS.size(); i++) {
                try (Statement s = c.createStatement()) {
                    s.execute(MIGRATIONS.get(i));
                }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO " + VERSION_TABLE + " (version) VALUES (?)")) {
                    p.setInt(1, i + 1);
                    p.executeUpdate();
                }
            }
        }
    }

    public PlayerAttribution find(UUID playerId) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT source, answered_at, prompted_at, prompts FROM " + TABLE + " WHERE player_uuid = ?")) {
            p.setString(1, playerId.toString());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return PlayerAttribution.unknown(playerId);
                return new PlayerAttribution(playerId, r.getString(1), nullableLong(r, 2), nullableLong(r, 3), r.getInt(4), true);
            }
        }
    }

    /** Records that the menu was opened for the player on join. */
    public void markPrompted(UUID playerId, long at) throws SQLException {
        String update = "UPDATE " + TABLE + " SET prompted_at = ?, prompts = prompts + 1 WHERE player_uuid = ?";
        try (Connection c = dataSource.getConnection()) {
            if (update(c, update, at, playerId) > 0) return;
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO " + TABLE + " (player_uuid, prompted_at, prompts) VALUES (?, ?, 1)")) {
                p.setString(1, playerId.toString());
                p.setLong(2, at);
                p.executeUpdate();
            } catch (SQLException e) {
                if (!isDuplicateKey(e)) throw e;
                update(c, update, at, playerId); // inserted meanwhile by another server
            }
        }
    }

    /** Stores the player's source, unless they already have one: the first answer is the one kept. */
    public AnswerResult answer(UUID playerId, String source, long at) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            for (int attempt = 0; attempt < 2; attempt++) {
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE " + TABLE + " SET source = ?, answered_at = ? WHERE player_uuid = ? AND source IS NULL")) {
                    p.setString(1, source);
                    p.setLong(2, at);
                    p.setString(3, playerId.toString());
                    if (p.executeUpdate() > 0) return AnswerResult.SAVED;
                }
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO " + TABLE + " (player_uuid, source, answered_at, prompts) VALUES (?, ?, ?, 0)")) {
                    p.setString(1, playerId.toString());
                    p.setString(2, source);
                    p.setLong(3, at);
                    p.executeUpdate();
                    return AnswerResult.SAVED;
                } catch (SQLException e) {
                    if (!isDuplicateKey(e)) throw e;
                    // The row exists: answered already, or created by markPrompted since the update.
                }
            }
            return AnswerResult.ALREADY_ANSWERED;
        }
    }

    /**
     * Forgets the player's answer and prompts, so they are asked again on their next join; the
     * row stays, which makes them eligible even with {@code new_players_only}. False when they
     * had not answered.
     */
    public boolean reset(UUID playerId) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE " + TABLE + " SET source = ?, answered_at = ?, prompted_at = ?, prompts = 0 "
                             + "WHERE player_uuid = ? AND source IS NOT NULL")) {
            p.setNull(1, Types.VARCHAR);
            p.setNull(2, Types.BIGINT);
            p.setNull(3, Types.BIGINT);
            p.setString(4, playerId.toString());
            return p.executeUpdate() > 0;
        }
    }

    /** Answers per source id, most picked first. */
    public Map<String, Integer> countBySource() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT source, COUNT(*) AS answers FROM " + TABLE
                     + " WHERE source IS NOT NULL GROUP BY source ORDER BY answers DESC, source")) {
            while (r.next()) {
                counts.put(r.getString(1), r.getInt(2));
            }
        }
        return counts;
    }

    private static int update(Connection c, String sql, long at, UUID playerId) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, at);
            p.setString(2, playerId.toString());
            return p.executeUpdate();
        }
    }

    private static @Nullable Long nullableLong(ResultSet r, int column) throws SQLException {
        long value = r.getLong(column);
        return r.wasNull() ? null : value;
    }

    /** SQLState class 23 (integrity constraint violation): H2, MySQL and MariaDB all report a duplicate key so. */
    private static boolean isDuplicateKey(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
}

package com.github.ibanetchep.msquests.database;

import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Copies {@code msquests_*} into {@code artisan_quests_*}, once, in one transaction, filling
 * {@code quest_uid} and {@code stage_key} from the {@link MigrationMap} the content conversion
 * wrote. The {@code msquests_*} tables are only read: the previous jar can still run on them.
 *
 * <p>A quest row whose {@code (group_key, quest_key)} is not in the map belongs to a quest that no
 * longer exists in the configs (its row was already ignored at load); it is left behind, with its
 * objectives, and counted. Same for an objective key the map does not know and a rotation of an
 * unknown group. Before committing, every target table must hold exactly the rows copied, or the
 * whole copy is rolled back.
 */
public class LegacyDataCopier {

    private static final int BATCH = 500;

    private final Jdbi jdbi;

    public LegacyDataCopier(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /** Rows of one table: read from {@code msquests_*}, copied, and left behind (no uid for them). */
    public record TableCount(String table, int source, int copied, int skipped) {
        @Override
        public String toString() {
            return table + ": " + copied + "/" + source + " copied" + (skipped > 0 ? ", " + skipped + " left behind" : "");
        }
    }

    /**
     * @param notes         what was left behind, by name
     * @param alreadyCopied the copy had already run: nothing was read or written
     */
    public record Result(List<TableCount> tables, List<String> notes, boolean alreadyCopied) {
        public String summary() {
            return String.join("; ", tables.stream().map(TableCount::toString).toList());
        }
    }

    /** True once a copy was committed: the {@code artisan_quests_*} tables are the live ones. */
    public boolean isCopied() {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT COUNT(*) FROM artisan_quests_data_copy")
                .mapTo(Integer.class).one() > 0);
    }

    /** Rows of {@code msquests_quest}: tells a fresh install (nothing to copy) from a server with progress. */
    public int legacyQuestRows() {
        return jdbi.withHandle(handle -> count(handle, SqlTables.LEGACY.quest()));
    }

    /** What {@link #copy} would do, without writing anything. */
    public Result plan(MigrationMap map) {
        return jdbi.withHandle(handle -> run(handle, map, false));
    }

    /**
     * Runs the copy and records it. Refuses (and changes nothing) when it already ran, or when an
     * {@code artisan_quests_*} table already holds rows it did not write.
     */
    public Result copy(MigrationMap map) {
        return jdbi.inTransaction(handle -> {
            if (count(handle, "artisan_quests_data_copy") > 0) {
                return new Result(List.of(), List.of("already copied"), true);
            }
            for (String table : targetTables()) {
                int rows = count(handle, table);
                if (rows > 0) {
                    throw new IllegalStateException(table + " already holds " + rows
                            + " row(s) although no copy was recorded; refusing to mix them with msquests_* rows");
                }
            }

            Result result = run(handle, map, true);

            for (TableCount count : result.tables()) {
                int rows = count(handle, target(count.table()));
                if (rows != count.copied()) {
                    throw new IllegalStateException(target(count.table()) + " holds " + rows + " row(s) after the copy, "
                            + count.copied() + " expected; rolled back");
                }
                if (count.copied() + count.skipped() != count.source()) {
                    throw new IllegalStateException(count.table() + ": " + count.copied() + " copied + " + count.skipped()
                            + " left behind != " + count.source() + " read; rolled back");
                }
            }

            String summary = result.summary();
            handle.createUpdate("INSERT INTO artisan_quests_data_copy (id, summary) VALUES (1, :summary)")
                    .bind("summary", summary.length() > 2000 ? summary.substring(0, 2000) : summary)
                    .execute();
            return result;
        });
    }

    private Result run(Handle handle, MigrationMap map, boolean write) {
        SqlTables from = SqlTables.LEGACY;
        SqlTables to = SqlTables.ARTISAN;
        List<TableCount> counts = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        // Profiles and actors carry no config reference: copied as they are.
        List<Object[]> profiles = handle.createQuery(
                        "SELECT id, name, tracked_quest_id, created_at, updated_at FROM " + from.profile())
                .map((rs, ctx) -> new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4), rs.getTimestamp(5)})
                .list();
        if (write) {
            insert(handle, "INSERT INTO " + to.profile() + " (id, name, tracked_quest_id, created_at, updated_at)"
                    + " VALUES (?, ?, ?, ?, ?)", profiles);
        }
        counts.add(new TableCount(from.profile(), profiles.size(), profiles.size(), 0));

        List<Object[]> actors = handle.createQuery("SELECT id, actor_type, created_at, updated_at FROM " + from.actor())
                .map((rs, ctx) -> new Object[]{rs.getString(1), rs.getString(2), rs.getTimestamp(3), rs.getTimestamp(4)})
                .list();
        if (write) {
            insert(handle, "INSERT INTO " + to.actor() + " (id, actor_type, created_at, updated_at) VALUES (?, ?, ?, ?)",
                    actors);
        }
        counts.add(new TableCount(from.actor(), actors.size(), actors.size(), 0));

        List<Object[]> quests = new ArrayList<>();
        Set<String> copiedQuestIds = new HashSet<>();
        Set<String> unknownQuests = new TreeSet<>();
        int questSource = 0;
        for (Object[] row : handle.createQuery("""
                        SELECT id, quest_key, quest_group_key, quest_status, started_at, expires_at, completed_at,
                               actor_id, created_at, updated_at
                        FROM""" + " " + from.quest())
                .map((rs, ctx) -> new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getTimestamp(5), rs.getTimestamp(6), rs.getTimestamp(7), rs.getString(8),
                        rs.getTimestamp(9), rs.getTimestamp(10)})
                .list()) {
            questSource++;
            String questKey = (String) row[1];
            String groupKey = (String) row[2];
            MigrationMap.Quest quest = map.quest(groupKey, questKey);
            if (quest == null) {
                unknownQuests.add(groupKey + "/" + questKey);
                continue;
            }
            copiedQuestIds.add((String) row[0]);
            quests.add(new Object[]{row[0], quest.uid(), questKey, groupKey, row[3], row[4], row[5], row[6],
                    row[7], row[8], row[9]});
        }
        if (write) {
            insert(handle, "INSERT INTO " + to.quest() + " (id, quest_uid, quest_key, group_key, quest_status,"
                    + " started_at, expires_at, completed_at, actor_id, created_at, updated_at)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", quests);
        }
        counts.add(new TableCount(from.quest(), questSource, quests.size(), questSource - quests.size()));
        if (!unknownQuests.isEmpty()) {
            notes.add("quests no longer in the configs, rows left in " + from.quest() + ": " + unknownQuests);
        }

        List<Object[]> objectives = new ArrayList<>();
        Set<String> unknownObjectives = new TreeSet<>();
        for (Object[] row : handle.createQuery("SELECT o.quest_id, o.objective_key, o.objective_status, o.objective_type,"
                        + " o.progress, o.started_at, o.completed_at, o.created_at, o.updated_at,"
                        + " q.quest_group_key, q.quest_key"
                        + " FROM " + from.objective() + " o JOIN " + from.quest() + " q ON q.id = o.quest_id")
                .map((rs, ctx) -> new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getInt(5), rs.getTimestamp(6), rs.getTimestamp(7), rs.getTimestamp(8), rs.getTimestamp(9),
                        rs.getString(10), rs.getString(11)})
                .list()) {
            if (!copiedQuestIds.contains((String) row[0])) {
                continue; // its quest is left behind
            }
            MigrationMap.Quest quest = map.quest((String) row[9], (String) row[10]);
            String stageKey = quest != null ? quest.objectiveStages().get((String) row[1]) : null;
            if (stageKey == null) {
                unknownObjectives.add(row[9] + "/" + row[10] + "/" + row[1]);
                continue;
            }
            objectives.add(new Object[]{row[0], stageKey, row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]});
        }
        if (write) {
            insert(handle, "INSERT INTO " + to.objective() + " (quest_id, stage_key, objective_key, objective_status,"
                    + " objective_type, progress, started_at, completed_at, created_at, updated_at)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", objectives);
        }
        // Counted apart from the join so a row the join missed would show up as left behind.
        int objectiveRows = count(handle, from.objective());
        counts.add(new TableCount(from.objective(), objectiveRows, objectives.size(), objectiveRows - objectives.size()));
        if (!unknownObjectives.isEmpty()) {
            notes.add("objectives no longer in their quest, rows left in " + from.objective() + ": " + unknownObjectives);
        }

        List<Object[]> rotations = new ArrayList<>();
        Set<String> unknownGroups = new TreeSet<>();
        List<Object[]> rotationRows = handle.createQuery("SELECT id, actor_id, group_key, rotated_at FROM " + from.rotation())
                .map((rs, ctx) -> new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4)})
                .list();
        for (Object[] row : rotationRows) {
            MigrationMap.Group group = map.group((String) row[2]);
            if (group == null) {
                unknownGroups.add((String) row[2]);
                continue;
            }
            rotations.add(new Object[]{row[0], row[1], group.uid(), row[3]});
        }
        if (write) {
            insert(handle, "INSERT INTO " + to.rotation() + " (id, actor_id, group_uid, rotated_at) VALUES (?, ?, ?, ?)",
                    rotations);
        }
        counts.add(new TableCount(from.rotation(), rotationRows.size(), rotations.size(),
                rotationRows.size() - rotations.size()));
        if (!unknownGroups.isEmpty()) {
            notes.add("rotations of groups no longer in the configs, rows left in " + from.rotation() + ": " + unknownGroups);
        }

        return new Result(counts, notes, false);
    }

    private static void insert(Handle handle, String sql, List<Object[]> rows) {
        for (int start = 0; start < rows.size(); start += BATCH) {
            PreparedBatch batch = handle.prepareBatch(sql);
            for (Object[] row : rows.subList(start, Math.min(rows.size(), start + BATCH))) {
                for (int i = 0; i < row.length; i++) {
                    Object value = row[i];
                    if (value == null) {
                        batch.bindNull(i, java.sql.Types.NULL);
                    } else if (value instanceof Timestamp ts) {
                        batch.bind(i, ts);
                    } else if (value instanceof Integer n) {
                        batch.bind(i, n);
                    } else {
                        batch.bind(i, value.toString());
                    }
                }
                batch.add();
            }
            batch.execute();
        }
    }

    private static int count(Handle handle, String table) {
        return handle.createQuery("SELECT COUNT(*) FROM " + table).mapTo(Integer.class).one();
    }

    private static List<String> targetTables() {
        SqlTables to = SqlTables.ARTISAN;
        return List.of(to.profile(), to.actor(), to.quest(), to.objective(), to.rotation());
    }

    private static String target(String legacyTable) {
        SqlTables from = SqlTables.LEGACY;
        SqlTables to = SqlTables.ARTISAN;
        if (legacyTable.equals(from.profile())) return to.profile();
        if (legacyTable.equals(from.actor())) return to.actor();
        if (legacyTable.equals(from.quest())) return to.quest();
        if (legacyTable.equals(from.objective())) return to.objective();
        if (legacyTable.equals(from.rotation())) return to.rotation();
        throw new IllegalArgumentException(legacyTable);
    }
}

package com.github.ibanetchep.msquests.bukkit.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Converts the groups of {@code quests/*.yml} into {@code quest_group} Documents, one file per
 * file, and records the uids it chose in a {@link MigrationMap}. {@link #plan} computes
 * everything without writing (the dry run); {@link #apply} writes the planned files.
 *
 * <ul>
 *   <li>Idempotent: a uid already in the previous map, or in an existing Document, is kept.</li>
 *   <li>Never overwrites: a Document that already exists is kept as it is (it may have been
 *       edited since) and reported.</li>
 *   <li>Checked: every Document is read back with {@link QuestGroupDocumentReader} and compared
 *       with the group it came from; any difference is reported.</li>
 * </ul>
 */
public final class LegacyContentConverter {

    /** One group: the Document to write ({@code yaml} null when an existing one is kept). */
    public record GroupPlan(String key, Path target, @Nullable String yaml) {
    }

    public record Plan(List<GroupPlan> groups, MigrationMap map, MigrationReport report) {
        public long toWrite() {
            return groups.stream().filter(g -> g.yaml() != null).count();
        }
    }

    private final String lang;
    private final @Nullable String timezone;

    /**
     * @param lang     language the existing texts are filed under (the project's default)
     * @param timezone zone written for the reset crons: the server's, which is the zone they ran in
     */
    public LegacyContentConverter(String lang, @Nullable String timezone) {
        this.lang = lang;
        this.timezone = timezone;
    }

    public Plan plan(Map<String, QuestGroupConfigDTO> legacy, List<String> legacyFailures, Path questGroupsDir,
                     MigrationMap previous) {
        MigrationReport report = new MigrationReport();
        report.section("Source: quests/*.yml");
        report.line(legacy.size() + " group(s) read.");
        for (String failure : legacyFailures) {
            report.warn("Not converted, the file does not load today either: " + failure);
        }

        Map<String, MigrationMap.Group> groups = new TreeMap<>(previous.groups());
        List<GroupPlan> plans = new ArrayList<>();
        Map<String, Path> documentsByUid = documentsByUid(questGroupsDir);

        for (QuestGroupConfigDTO group : new TreeMap<>(legacy).values()) {
            report.section("Group " + group.key());
            MigrationMap.Group previousEntry = previous.group(group.key());
            Path target = questGroupsDir.resolve(group.key() + ".yaml");
            if (!Files.exists(target) && previousEntry != null && documentsByUid.containsKey(previousEntry.uid())) {
                target = documentsByUid.get(previousEntry.uid()); // renamed in the editor since the last run
                report.line("Already converted, found under its new name " + target.getFileName() + ".");
            }
            Existing existing = readExisting(target, group.key(), report);

            Uids uids = new Uids(group.key(), previous.group(group.key()), existing);
            String groupUid = uids.group();

            Map<String, MigrationMap.Quest> questEntries = new LinkedHashMap<>();
            Map<String, List<QuestStageConfigDTO>> stagesByQuest = new HashMap<>();
            int objectives = 0;
            int actions = countActions(group);
            for (QuestConfigDTO quest : group.quests() != null ? group.quests() : List.<QuestConfigDTO>of()) {
                Map<String, String> objectiveStages = new LinkedHashMap<>();
                List<QuestStageConfigDTO> stages = uniqueObjectiveKeys(group.key(), quest, objectiveStages, report);
                stagesByQuest.put(quest.key(), stages);
                questEntries.put(quest.key(), new MigrationMap.Quest(uids.quest(quest.key()), objectiveStages));
                objectives += objectiveStages.size();
                actions += quest.rewards() != null ? quest.rewards().size() : 0;
            }

            if (existing != null && previousEntry != null) {
                groups.put(group.key(), previousEntry); // the copy already relies on it
            } else if (existing != null && existing.groupUid() == null) {
                groups.remove(group.key());
                report.warn("No uid can be matched to the kept Document: the saved progress of this group"
                        + " will not be copied. Fix or remove " + target.getFileName() + " and run the migration again.");
            } else {
                groups.put(group.key(), new MigrationMap.Group(groupUid, questEntries));
                if (existing != null) {
                    List<String> absent = questEntries.keySet().stream()
                            .filter(k -> !existing.questUids().containsKey(k)).toList();
                    if (!absent.isEmpty()) {
                        report.warn("Quests missing from the kept Document, their saved progress will not load: " + absent);
                    }
                }
            }

            int questCount = group.quests() != null ? group.quests().size() : 0;
            report.line(questCount + " quest(s), " + objectives + " objective(s), " + actions + " action(s).");
            report.line("Texts filed under '" + lang + "', unchanged (translate them in the editor).");
            if (group.resetCron() != null) {
                report.line("Reset cron '" + group.resetCron() + "' now names its zone: " + timezone
                        + " (the server's zone, which it ran in so far).");
            }
            if (actions > 0) {
                report.line("Actions, rewards and conditions kept in the MSQuests format: they become Artisan"
                        + " chains in a later step.");
            }

            String yaml;
            QuestGroupConfigDTO readBack;
            try {
                Map<String, Object> tree = QuestGroupDocumentWriter.write(group, groupUid, uids::quest,
                        q -> stagesByQuest.get(q.key()), lang, timezone);
                yaml = DocumentYaml.write(tree);
                readBack = QuestGroupDocumentReader.read(group.key(), DocumentYaml.parse(yaml), lang);
            } catch (IOException | RuntimeException e) {
                report.warn("Not converted: " + e.getMessage());
                continue;
            }

            List<String> differences = differences(group, readBack);
            if (differences.isEmpty()) {
                report.line("Read back: same content as quests/" + group.key() + ".yml.");
            } else {
                for (String difference : differences) {
                    report.warn("Read back differs at " + difference);
                }
            }

            if (existing != null) {
                report.warn(target.getFileName() + " already exists: kept as it is, not overwritten.");
                plans.add(new GroupPlan(group.key(), target, null));
            } else {
                report.line("Writes editor/quest_groups/" + target.getFileName() + ".");
                plans.add(new GroupPlan(group.key(), target, yaml));
            }
        }

        return new Plan(plans, new MigrationMap(groups), report);
    }

    /** Writes the planned Documents, each built aside and moved in, so a crash never leaves half a file. */
    public void apply(Plan plan, Path questGroupsDir, Path tmpDir) throws IOException {
        Files.createDirectories(questGroupsDir);
        Files.createDirectories(tmpDir);
        for (GroupPlan group : plan.groups()) {
            if (group.yaml() == null) continue;
            if (Files.exists(group.target())) {
                throw new IOException(group.target() + " appeared since the plan was made; nothing overwritten");
            }
            writeAtomically(group.target(), group.yaml(), tmpDir);
        }
    }

    static void writeAtomically(Path target, String content, Path tmpDir) throws IOException {
        Files.createDirectories(tmpDir);
        Path tmp = Files.createTempFile(tmpDir, target.getFileName().toString(), ".tmp");
        try {
            Files.writeString(tmp, content, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /** Root uid of each Document already in the folder, to find one renamed since the last run. */
    private static Map<String, Path> documentsByUid(Path questGroupsDir) {
        Map<String, Path> byUid = new HashMap<>();
        if (!Files.isDirectory(questGroupsDir)) return byUid;
        try (java.util.stream.Stream<Path> list = Files.list(questGroupsDir)) {
            for (Path file : list.filter(f -> f.getFileName().toString().endsWith(".yaml")).sorted().toList()) {
                try {
                    Object uid = DocumentYaml.parse(Files.readString(file, StandardCharsets.UTF_8)).get("uid");
                    if (uid != null) byUid.putIfAbsent(uid.toString(), file);
                } catch (IOException | RuntimeException ignored) {
                    // unreadable: the repository reports it at load
                }
            }
        } catch (IOException ignored) {
            // no index: renamed Documents are not found, the per-key check still holds
        }
        return byUid;
    }

    // --------------------------------------------------------------- uids

    /** A Document already on disk, read for the uids it carries; {@code groupUid} null when unreadable. */
    private record Existing(@Nullable String groupUid, Map<String, String> questUids) {
    }

    private @Nullable Existing readExisting(Path target, String groupKey, MigrationReport report) {
        if (!Files.exists(target)) return null;
        try {
            QuestGroupConfigDTO doc = QuestGroupDocumentReader.read(groupKey,
                    DocumentYaml.parse(Files.readString(target, StandardCharsets.UTF_8)), lang);
            Map<String, String> questUids = new HashMap<>();
            for (QuestConfigDTO quest : doc.quests()) {
                questUids.put(quest.key(), quest.uid());
            }
            return new Existing(doc.uid(), questUids);
        } catch (IOException | RuntimeException e) {
            report.warn(target.getFileName() + " already exists but cannot be read (" + e.getMessage()
                    + "): kept as it is, its uids are not reused.");
            return new Existing(null, Map.of());
        }
    }

    /** Previous map first (the database copy may already rely on it), then an existing Document, then new. */
    private static final class Uids {
        private final @Nullable MigrationMap.Group previous;
        private final @Nullable Existing existing;
        private final String group;
        private final Map<String, String> quests = new HashMap<>();
        private final Set<String> used = new HashSet<>();

        Uids(String groupKey, @Nullable MigrationMap.Group previous, @Nullable Existing existing) {
            this.previous = previous;
            this.existing = existing;
            String uid = previous != null ? previous.uid() : existing != null ? existing.groupUid() : null;
            this.group = uid != null ? uid : Ulid.generate();
            used.add(group);
        }

        String group() {
            return group;
        }

        String quest(String questKey) {
            return quests.computeIfAbsent(questKey, key -> {
                String uid = null;
                if (previous != null && previous.quests().get(key) != null) uid = previous.quests().get(key).uid();
                if (uid == null && existing != null) uid = existing.questUids().get(key);
                if (uid == null || used.contains(uid)) uid = Ulid.generate();
                used.add(uid);
                return uid;
            });
        }
    }

    // ------------------------------------------------------- objective keys

    /**
     * The quest's stages with objective keys made unique within the quest, filling
     * {@code objectiveStages} with each stored key's stage. Today two objectives of a quest that
     * share a key share one database row; the first one keeps the key (and the row), a later one
     * is renamed {@code <key>_<stage>} and starts from 0.
     */
    private static List<QuestStageConfigDTO> uniqueObjectiveKeys(String groupKey, QuestConfigDTO quest,
                                                                 Map<String, String> objectiveStages,
                                                                 MigrationReport report) {
        List<QuestStageConfigDTO> stages = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (QuestStageConfigDTO stage : quest.stages() != null ? quest.stages() : List.<QuestStageConfigDTO>of()) {
            List<QuestObjectiveConfigDTO> objectives = new ArrayList<>();
            for (QuestObjectiveConfigDTO objective : stage.objectives() != null
                    ? stage.objectives() : List.<QuestObjectiveConfigDTO>of()) {
                String key = objective.key();
                if (used.add(key)) {
                    objectiveStages.put(key, stage.key());
                    objectives.add(objective);
                    continue;
                }
                String renamed = key + "_" + stage.key();
                for (int n = 2; !used.add(renamed); n++) {
                    renamed = key + "_" + stage.key() + "_" + n;
                }
                report.warn("Quest " + groupKey + "/" + quest.key() + ": objective key '" + key + "' is used twice;"
                        + " the one in stage '" + stage.key() + "' becomes '" + renamed + "' and starts from 0"
                        + " (both shared one saved progress until now, which stays with the first).");
                objectives.add(new QuestObjectiveConfigDTO(renamed, objective.type(), objective.params()));
            }
            stages.add(new QuestStageConfigDTO(stage.key(), stage.name(), stage.flow(), objectives));
        }
        return stages;
    }

    private static int countActions(QuestGroupConfigDTO group) {
        if (group.actions() == null) return 0;
        int count = 0;
        for (List<QuestActionDTO> hook : List.of(
                nonNull(group.actions().actorLoad()), nonNull(group.actions().questDistribution()),
                nonNull(group.actions().questStart()), nonNull(group.actions().questComplete()),
                nonNull(group.actions().objectiveProgress()), nonNull(group.actions().objectiveComplete()),
                nonNull(group.actions().allPeriodQuestsComplete()))) {
            count += hook.size();
        }
        return count;
    }

    private static List<QuestActionDTO> nonNull(@Nullable List<QuestActionDTO> list) {
        return list != null ? list : List.of();
    }

    // ---------------------------------------------------------- comparison

    /** Paths where two groups differ, after the changes the conversion makes on purpose. */
    static List<String> differences(QuestGroupConfigDTO legacy, QuestGroupConfigDTO readBack) {
        List<String> out = new ArrayList<>();
        diff("", normalize(DocumentYaml.MAPPER.valueToTree(legacy)), normalize(DocumentYaml.MAPPER.valueToTree(readBack)), out);
        return out.size() > 20 ? List.copyOf(out.subList(0, 20)) : out;
    }

    /**
     * Drops what carries no meaning (nulls, empty lists, a zero duration: no limit) and the case
     * of enum-like values, which the Document writes in lower case.
     */
    private static JsonNode normalize(JsonNode node) {
        if (node instanceof ObjectNode object) {
            for (String name : names(object)) {
                JsonNode value = normalize(object.get(name));
                boolean empty = value.isNull() || (value.isContainerNode() && value.isEmpty())
                        || ("duration".equals(name) && value.isNumber() && value.asLong() == 0);
                if (empty) {
                    object.remove(name);
                } else if (value.isTextual() && List.of("actorType", "strategy", "flow").contains(name)) {
                    object.set(name, TextNode.valueOf(value.asText().toLowerCase(Locale.ROOT)));
                } else if (value instanceof ArrayNode array && "triggers".equals(name)) {
                    ArrayNode lower = DocumentYaml.MAPPER.createArrayNode();
                    array.forEach(t -> lower.add(t.asText().toLowerCase(Locale.ROOT)));
                    object.set(name, lower);
                } else {
                    object.set(name, value);
                }
            }
        } else if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                array.set(i, normalize(array.get(i)));
            }
        }
        return node;
    }

    private static void diff(String path, @Nullable JsonNode a, @Nullable JsonNode b, List<String> out) {
        if (a == null || b == null) {
            if (a != b) out.add((path.isEmpty() ? "/" : path) + ": " + a + " became " + b);
            return;
        }
        if (a.isObject() && b.isObject()) {
            Set<String> names = new TreeSet<>();
            names.addAll(names(a));
            names.addAll(names(b));
            for (String name : names) diff(path + "/" + name, a.get(name), b.get(name), out);
        } else if (a.isArray() && b.isArray() && a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) diff(path + "[" + i + "]", a.get(i), b.get(i), out);
        } else if (!a.equals(b) && !(a.isNumber() && b.isNumber() && a.asDouble() == b.asDouble())) {
            out.add(path + ": " + abbreviate(a) + " became " + abbreviate(b));
        }
    }

    private static List<String> names(JsonNode node) {
        return node.properties().stream().map(Map.Entry::getKey).toList();
    }

    private static String abbreviate(JsonNode node) {
        String text = node.toString();
        return text.length() > 120 ? text.substring(0, 117) + "..." : text;
    }
}

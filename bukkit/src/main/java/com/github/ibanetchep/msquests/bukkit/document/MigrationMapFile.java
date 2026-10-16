package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code migration/map.yaml}: the uids {@code /msquests migrate} gave to groups and quests, and
 * the stage of each objective key. Kept outside the editor project, which is content only.
 *
 * <pre>
 * groups:
 *   daily:
 *     uid: 01J...
 *     quests:
 *       mine_stone_64:
 *         uid: 01J...
 *         objectives: { obj_1: stage_1 }
 * </pre>
 */
public final class MigrationMapFile {

    private static final String HEADER = "# Written by /msquests migrate: the uid of each group and quest of quests/*.yml,\n"
            + "# and the stage of each objective. Read to copy the saved progress into artisan_quests_*,\n"
            + "# and by a later run of the migration to keep the same uids. Do not edit.\n";

    private MigrationMapFile() {
    }

    /** The map on disk, or null when the migration never ran. */
    public static @Nullable MigrationMap read(Path file) throws IOException {
        if (!Files.exists(file)) return null;
        Map<String, Object> tree = DocumentYaml.parse(Files.readString(file, StandardCharsets.UTF_8));
        Map<String, MigrationMap.Group> groups = new LinkedHashMap<>();
        for (Map.Entry<String, Object> group : map(tree.get("groups"), "groups").entrySet()) {
            String path = "groups." + group.getKey();
            Map<String, Object> groupTree = map(group.getValue(), path);
            Map<String, MigrationMap.Quest> quests = new LinkedHashMap<>();
            for (Map.Entry<String, Object> quest : map(groupTree.get("quests"), path + ".quests").entrySet()) {
                String questPath = path + ".quests." + quest.getKey();
                Map<String, Object> questTree = map(quest.getValue(), questPath);
                Map<String, String> stages = new LinkedHashMap<>();
                map(questTree.get("objectives"), questPath + ".objectives")
                        .forEach((objective, stage) -> stages.put(objective, String.valueOf(stage)));
                quests.put(quest.getKey(), new MigrationMap.Quest(uid(questTree, questPath), stages));
            }
            groups.put(group.getKey(), new MigrationMap.Group(uid(groupTree, path), quests));
        }
        return new MigrationMap(groups);
    }

    public static void write(Path file, MigrationMap map, Path tmpDir) throws IOException {
        Map<String, Object> groups = new LinkedHashMap<>();
        for (Map.Entry<String, MigrationMap.Group> group : new java.util.TreeMap<>(map.groups()).entrySet()) {
            Map<String, Object> quests = new LinkedHashMap<>();
            group.getValue().quests().forEach((key, quest) -> {
                Map<String, Object> questTree = new LinkedHashMap<>();
                questTree.put("uid", quest.uid());
                questTree.put("objectives", new LinkedHashMap<>(quest.objectiveStages()));
                quests.put(key, questTree);
            });
            Map<String, Object> groupTree = new LinkedHashMap<>();
            groupTree.put("uid", group.getValue().uid());
            groupTree.put("quests", quests);
            groups.put(group.getKey(), groupTree);
        }
        Files.createDirectories(file.getParent());
        LegacyContentConverter.writeAtomically(file, HEADER + DocumentYaml.write(Map.of("groups", groups)), tmpDir);
    }

    private static String uid(Map<String, Object> tree, String path) {
        Object uid = tree.get("uid");
        if (uid == null || uid.toString().isBlank()) throw new DocumentException(path + ".uid", "is required");
        return uid.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(@Nullable Object value, String path) {
        if (value == null) return Map.of();
        if (value instanceof Map<?, ?> m) return (Map<String, Object>) m;
        if (value instanceof List<?> l && l.isEmpty()) return Map.of();
        throw new DocumentException(path, "expected a mapping");
    }
}

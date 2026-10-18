package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Reads the quest groups from the {@code quest_group} Documents of <b>every</b> Artisan project:
 * {@code <project>/quest_groups/<group>.yaml}, the group key being the file name (the Document id).
 * A module loaded on the server is usable in any project, so a group lives wherever its author
 * put it. Texts are loaded in the {@code default_lang} of the group's own project.
 *
 * <p>Reports a failure per file, like the {@code quests/*.yml} reader, plus:
 * <ul>
 *   <li>one per group key held by two projects: commands and placeholders name a group by its
 *       key alone, so it must be unique on the server. Neither copy loads, whatever the order the
 *       projects are listed in;</li>
 *   <li>one per uid used by two groups: rows are resolved by uid, so a duplicated one would hand
 *       a quest's progress to another quest.</li>
 * </ul>
 */
public final class QuestGroupDocumentRepository implements QuestConfigRepository {

    public static final String DIR = "quest_groups";

    private final ProjectFiles projects;

    public QuestGroupDocumentRepository(ProjectFiles projects) {
        this.projects = projects;
    }

    /** The module's own project only: the converter's view, and the tests'. */
    public QuestGroupDocumentRepository(Path projectDir) {
        this(ProjectFiles.directory(EditorProject.PROJECT_ID, projectDir));
    }

    @Override
    public CompletableFuture<QuestConfigLoad> loadAllGroups() {
        return CompletableFuture.supplyAsync(this::loadNow);
    }

    QuestConfigLoad loadNow() {
        List<String> failures = new ArrayList<>();
        Map<String, List<GroupFile>> byKey = new LinkedHashMap<>();
        for (String project : projects.projectIds()) {
            for (String name : files(project)) {
                String key = name.substring(0, name.length() - ".yaml".length());
                byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(new GroupFile(project, name));
            }
        }

        Map<String, QuestGroupConfigDTO> groups = new LinkedHashMap<>();
        Map<String, String> uidOwners = new HashMap<>();
        Map<String, String> langs = new HashMap<>();
        for (Map.Entry<String, List<GroupFile>> entry : byKey.entrySet()) {
            String key = entry.getKey();
            List<GroupFile> files = entry.getValue();
            if (files.size() > 1) {
                failures.add("group '" + key + "' is defined by " + String.join(" and ",
                        files.stream().map(GroupFile::path).toList())
                        + ": a group key must be unique on the server, rename or delete one of them");
                continue;
            }
            GroupFile file = files.get(0);

            QuestGroupConfigDTO group;
            try {
                String lang = langs.computeIfAbsent(file.project(), this::defaultLang);
                String source = projects.read(file.project(), DIR + "/" + file.name());
                if (source == null) {
                    failures.add(file.path() + ": the file disappeared while reading");
                    continue;
                }
                group = QuestGroupDocumentReader.read(key, DocumentYaml.parse(source), lang)
                        .withSourceProject(file.project());
            } catch (IOException | RuntimeException e) {
                failures.add(file.path() + ": " + e.getMessage());
                continue;
            }

            // claimed aside, so a rejected Document reserves none of its uids
            Map<String, String> claimed = new HashMap<>();
            String clash = claim(uidOwners, claimed, group.uid(), key);
            for (QuestConfigDTO quest : group.quests()) {
                if (clash == null) clash = claim(uidOwners, claimed, quest.uid(), key + "/" + quest.key());
            }
            if (clash != null) {
                failures.add(file.path() + ": " + clash);
                continue;
            }
            uidOwners.putAll(claimed);
            groups.put(key, group);
        }
        return new QuestConfigLoad(groups, failures);
    }

    /**
     * Fingerprint of the Documents as they are on disk now, in every project, to tell an Artisan
     * reload that changed them from one that did not (a publish of another file, a menu edit).
     */
    public String fingerprint() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String project : projects.projectIds()) {
                List<String> files = files(project);
                if (files.isEmpty()) continue;
                digest.update(("\0" + project + "\0").getBytes(StandardCharsets.UTF_8));
                String manifest = projects.read(project, "manifest.yaml");
                if (manifest != null) digest.update(manifest.getBytes(StandardCharsets.UTF_8));
                for (String name : files) {
                    String content = projects.read(project, DIR + "/" + name);
                    digest.update(name.getBytes(StandardCharsets.UTF_8));
                    if (content != null) digest.update(content.getBytes(StandardCharsets.UTF_8));
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (RuntimeException e) {
            return "unreadable:" + System.nanoTime(); // differs every time: forces a reload attempt
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** File names of {@code <project>/quest_groups/*.yaml}, the folder's own files only, sorted. */
    private List<String> files(String project) {
        return projects.list(project, DIR).stream()
                .filter(name -> !name.contains("/"))
                .filter(name -> name.endsWith(".yaml"))
                .filter(name -> !name.startsWith("."))
                .sorted()
                .toList();
    }

    /** The project's default language, {@code en} when the manifest names none. */
    public String defaultLang(String project) {
        String manifest = projects.read(project, "manifest.yaml");
        if (manifest == null) return "en";
        Object lang;
        try {
            lang = DocumentYaml.parse(manifest).get("default_lang");
        } catch (IOException e) {
            throw new UncheckedIOException(project + "/manifest.yaml: " + e.getMessage(), e);
        }
        return lang != null && !lang.toString().isBlank() ? lang.toString() : "en";
    }

    /** The default language of the module's own project. */
    public String defaultLang() {
        return defaultLang(EditorProject.PROJECT_ID);
    }

    private static @Nullable String claim(Map<String, String> owners, Map<String, String> claimed,
                                          String uid, String owner) {
        String previous = owners.get(uid);
        if (previous == null) previous = claimed.putIfAbsent(uid, owner);
        return previous != null ? "uid " + uid + " of " + owner + " is already used by " + previous : null;
    }

    private record GroupFile(String project, String name) {
        String path() {
            return project + "/" + DIR + "/" + name;
        }
    }
}

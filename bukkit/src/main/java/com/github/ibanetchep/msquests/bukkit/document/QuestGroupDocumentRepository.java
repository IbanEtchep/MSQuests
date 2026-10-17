package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.util.stream.Stream;

/**
 * Reads the quest groups from the {@code quest_group} Documents of the editor project:
 * {@code editor/quest_groups/<group>.yaml}, the group key being the file name (the Document id).
 * Texts are loaded in the project's {@code default_lang} ({@code editor/manifest.yaml}).
 *
 * <p>Reports a failure per file, like the {@code quests/*.yml} reader, plus one per uid used by
 * two groups: rows are resolved by uid, so a duplicated one would hand a quest's progress to
 * another quest.
 */
public final class QuestGroupDocumentRepository implements QuestConfigRepository {

    public static final String DIR = "quest_groups";

    private final Path projectDir;

    public QuestGroupDocumentRepository(Path projectDir) {
        this.projectDir = projectDir;
    }

    public Path groupsDir() {
        return projectDir.resolve(DIR);
    }

    @Override
    public CompletableFuture<QuestConfigLoad> loadAllGroups() {
        return CompletableFuture.supplyAsync(this::loadNow);
    }

    QuestConfigLoad loadNow() {
        Map<String, QuestGroupConfigDTO> groups = new LinkedHashMap<>();
        List<String> failures = new ArrayList<>();
        Map<String, String> uidOwners = new HashMap<>();
        String lang;
        try {
            lang = defaultLang();
        } catch (IOException | RuntimeException e) {
            return new QuestConfigLoad(Map.of(), List.of("manifest.yaml: " + e.getMessage()));
        }

        for (Path file : files()) {
            String name = file.getFileName().toString();
            String key = name.substring(0, name.length() - ".yaml".length());
            QuestGroupConfigDTO group;
            try {
                group = QuestGroupDocumentReader.read(key, DocumentYaml.parse(Files.readString(file, StandardCharsets.UTF_8)), lang);
            } catch (IOException | RuntimeException e) {
                failures.add(DIR + "/" + name + ": " + e.getMessage());
                continue;
            }

            // claimed aside, so a rejected Document reserves none of its uids
            Map<String, String> claimed = new HashMap<>();
            String clash = claim(uidOwners, claimed, group.uid(), key);
            for (QuestConfigDTO quest : group.quests()) {
                if (clash == null) clash = claim(uidOwners, claimed, quest.uid(), key + "/" + quest.key());
            }
            if (clash != null) {
                failures.add(DIR + "/" + name + ": " + clash);
                continue;
            }
            uidOwners.putAll(claimed);
            groups.put(key, group);
        }
        return new QuestConfigLoad(groups, failures);
    }

    /**
     * Fingerprint of the Documents as they are on disk now, to tell an Artisan reload that
     * changed them from one that did not (a publish of another project, a menu edit).
     */
    public String fingerprint() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Path manifest = projectDir.resolve("manifest.yaml");
            if (Files.exists(manifest)) digest.update(Files.readAllBytes(manifest));
            for (Path file : files()) {
                digest.update(file.getFileName().toString().getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(file));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            return "unreadable:" + System.nanoTime(); // differs every time: forces a reload attempt
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Path> files() {
        Path dir = groupsDir();
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> list = Files.list(dir)) {
            return list.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".yaml"))
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list " + dir, e);
        }
    }

    /** The project's default language, {@code en} when the manifest names none. */
    public String defaultLang() throws IOException {
        Path manifest = projectDir.resolve("manifest.yaml");
        if (!Files.exists(manifest)) return "en";
        Object lang = DocumentYaml.parse(Files.readString(manifest, StandardCharsets.UTF_8)).get("default_lang");
        return lang != null && !lang.toString().isBlank() ? lang.toString() : "en";
    }

    private static @Nullable String claim(Map<String, String> owners, Map<String, String> claimed,
                                          String uid, String owner) {
        String previous = owners.get(uid);
        if (previous == null) previous = claimed.putIfAbsent(uid, owner);
        return previous != null ? "uid " + uid + " of " + owner + " is already used by " + previous : null;
    }
}

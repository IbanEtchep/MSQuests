package com.github.ibanetchep.msquests.bukkit.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ibanetchep.msquests.bukkit.document.LegacyActions;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Reads the groups of {@code quests/*.yml}, the MSQuests format. Their actions and conditions are
 * turned into Artisan chains and conditions as they are read ({@link LegacyActions}): the plugin
 * has no other way to run them. What that conversion changes is logged and kept in
 * {@link #getConversionNotes()} for the migration report.
 */
public class QuestConfigYamlRepository implements QuestConfigRepository {

    private final Logger logger;
    private final Path rootFolder;
    private final Function<String, String> messageKeys;
    /** Group key → what the action conversion changed, from the last load. */
    private final Map<String, List<String>> conversionNotes = new ConcurrentHashMap<>();
    /**
     * Group key - path
     */
    private final Map<String, Path> questConfigPaths = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper;

    public QuestConfigYamlRepository(Path rootFolder, Logger logger) {
        this(rootFolder, logger, key -> null);
    }

    /**
     * @param messageKeys text of a MSQuests lang key ({@code message_key} of an action), null when
     *                    unknown: a converted action carries its text
     */
    public QuestConfigYamlRepository(Path rootFolder, Logger logger, Function<String, String> messageKeys) {
        this.rootFolder = rootFolder;
        this.logger = logger;
        this.messageKeys = messageKeys;

        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.yamlMapper.findAndRegisterModules();
        this.yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public CompletableFuture<QuestConfigLoad> loadAllGroups() {
        return CompletableFuture.supplyAsync(() -> {
            conversionNotes.clear();
            Map<String, QuestGroupConfigDTO> questGroups = new HashMap<>();
            List<String> failures = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(rootFolder)) {
                paths
                        .filter(Files::isRegularFile)
                        .filter(this::isValidQuestFile)
                        .sorted()
                        .forEach(path -> {
                            try {
                                QuestGroupConfigDTO group = loadGroup(path);
                                QuestGroupConfigDTO previous = questGroups.putIfAbsent(group.key(), group);
                                if (previous != null) {
                                    failures.add(path + ": group key '" + group.key() + "' is already defined by another file");
                                }
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Failed to load quest group " + path, e);
                                failures.add(path + ": " + rootMessage(e));
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read quest config files", e);
            }

            return new QuestConfigLoad(questGroups, failures);
        });
    }

    private static String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }

    public QuestGroupConfigDTO loadGroup(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> tree = yamlMapper.readValue(input, new TypeReference<>() {});
            if (tree == null) throw new IOException("empty file");
            LegacyActions legacy = new LegacyActions(messageKeys);
            legacy.upgradeGroup(tree);
            QuestGroupConfigDTO group = yamlMapper.convertValue(tree, QuestGroupConfigDTO.class);
            if (!legacy.notes().isEmpty()) {
                conversionNotes.put(group.key(), legacy.notes());
                logger.info("quests/" + path.getFileName() + ": actions read as Artisan chains, with these changes: "
                        + String.join(" | ", legacy.notes()));
            }

            String fileName = path.getFileName().toString().replace(".yml", "");
            if (!fileName.equals(group.key())) {
                logger.warning("File name '" + fileName + "' doesn't match group key '" + group.key() + "'");
            }

            questConfigPaths.put(group.key(), path);

            return group;
        }
    }

    public void saveGroup(QuestGroupConfigDTO group) throws IOException {
        Path path = questConfigPaths.computeIfAbsent(group.key(), k -> rootFolder.resolve(group.key() + ".yml"));
        yamlMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), group);
    }

    /** Group key → what reading its actions as Artisan chains changed, from the last load. */
    public Map<String, List<String>> getConversionNotes() {
        return Map.copyOf(conversionNotes);
    }

    public Path getGroupPath(String groupKey) {
        return questConfigPaths.get(groupKey);
    }

    private boolean isValidQuestFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml");
    }
}
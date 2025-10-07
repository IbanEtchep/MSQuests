package com.github.ibanetchep.msquests.bukkit.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class QuestConfigYamlRepository implements QuestConfigRepository {

    private final Logger logger;
    private final Path rootFolder;
    /**
     * Group key - path
     */
    private final Map<String, Path> questConfigPaths = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper;

    public QuestConfigYamlRepository(Path rootFolder, Logger logger) {
        this.rootFolder = rootFolder;
        this.logger = logger;

        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.yamlMapper.findAndRegisterModules();
        this.yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public CompletableFuture<Map<String, QuestGroupConfigDTO>> getAllGroups() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, QuestGroupConfigDTO> questGroups = new HashMap<>();

            try (Stream<Path> paths = Files.walk(rootFolder)) {
                paths
                        .filter(Files::isRegularFile)
                        .filter(this::isValidQuestFile)
                        .forEach(path -> {
                            try {
                                QuestGroupConfigDTO group = loadGroup(path);
                                questGroups.put(group.key(), group);
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Failed to load quest group " + path, e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read quest config files", e);
            }

            return questGroups;
        });
    }

    public QuestGroupConfigDTO loadGroup(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            QuestGroupConfigDTO group = yamlMapper.readValue(input, QuestGroupConfigDTO.class);

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

    public Path getGroupPath(String groupKey) {
        return questConfigPaths.get(groupKey);
    }

    private boolean isValidQuestFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml");
    }
}
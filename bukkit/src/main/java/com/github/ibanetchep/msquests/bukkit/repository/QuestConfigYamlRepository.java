package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class QuestConfigYamlRepository implements QuestConfigRepository {

    /**
     * The root folder where the quest config files are stored. (default: pluginFolder/quests)
     */
    private final Path rootFolder;

    /**
     * A map that stores the path of each loaded quest config.
     * Key: quest config key
     * Value: path to the quest config file
     */
    private final Map<String, Path> questConfigPaths = new ConcurrentHashMap<>();

    public QuestConfigYamlRepository(Path rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public CompletableFuture<Map<String, QuestConfigDTO>> getAll() {
        Map<String, QuestConfigDTO> questConfigs = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try (Stream<Path> paths = Files.walk(rootFolder)) {
                paths
                        .filter(path -> path.getFileName().toString().endsWith(".yml"))
                        .forEach(path -> {
                            List<QuestConfigDTO> quests = loadFileQuests(path);
                            for (QuestConfigDTO quest : quests) {
                                questConfigs.put(quest.key(), quest);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read quest config files", e);
            }

            return questConfigs;
        });
    }

    public List<QuestConfigDTO> loadFileQuests(Path path) {
        List<QuestConfigDTO> questConfigs;

        questConfigs = new ArrayList<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);

            if (section == null) {
                continue;
            }

            ConfigurationSection objectiveSection = section.getConfigurationSection("objectives");
            Map<String, QuestObjectiveConfigDTO> objectives = new HashMap<>();

            if (objectiveSection != null) {
                for (String objectiveKey : objectiveSection.getKeys(false)) {
                    ConfigurationSection objectiveConfig = objectiveSection.getConfigurationSection(objectiveKey);
                    if (objectiveConfig != null) {
                        Map<String, Object> data = objectiveConfig.getValues(true);
                        objectives.put(objectiveKey, new QuestObjectiveConfigDTO(objectiveKey, data));
                    }
                }
            }

            QuestConfigDTO questConfig = new QuestConfigDTO(
                    key,
                    config.getString(key + ".name"),
                    config.getString(key + ".description"),
                    config.getLong(key + ".duration"),
                    section.getStringList("tags"),
                    section.getStringList("rewards"),
                    objectives
            );

            questConfigs.add(questConfig);
            questConfigPaths.put(questConfig.key(), path);
        }

        return questConfigs;
    }

    @Override
    public CompletableFuture<Void> upsert(QuestConfigDTO dto) {
        return CompletableFuture.runAsync(() -> {
            Path path = questConfigPaths.computeIfAbsent(dto.key(), k -> rootFolder.resolve(dto.key() + ".yml"));

            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            config.set(dto.key() + ".name", dto.name());
            config.set(dto.key() + ".description", dto.description());
            config.set(dto.key() + ".duration", dto.duration());
            config.set(dto.key() + ".tags", dto.tags());
            config.set(dto.key() + ".rewards", dto.rewards());

            for (QuestObjectiveConfigDTO objective : dto.objectives().values()) {
                String objectiveKey = dto.key() + ".objectives." + objective.key();
                config.set(objectiveKey, objective.config());
            }

            try {
                config.save(path.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save quest config file: " + path, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            Path path = questConfigPaths.get(key);
            if (path == null) {
                throw new RuntimeException("Quest config not found: " + key);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            config.set(key, null);

            try {
                config.save(path.toFile());
                questConfigPaths.remove(key);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete quest config: " + key, e);
            }
        });
    }
}

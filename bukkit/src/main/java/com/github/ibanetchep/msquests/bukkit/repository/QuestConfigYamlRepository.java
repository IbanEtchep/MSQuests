package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroupType;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
     * Key: group key
     * Value: path to the quest config file
     */
    private final Map<String, Path> questConfigPaths = new ConcurrentHashMap<>();

    public QuestConfigYamlRepository(Path rootFolder) {
        this.rootFolder = rootFolder;
        System.out.println("QuestConfigYamlRepository rootFolder: " + rootFolder);
    }

    @Override
    public CompletableFuture<Map<String, QuestGroupDTO>> getAllGroups() {
        Map<String, QuestGroupDTO> questGroups = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try (Stream<Path> paths = Files.walk(rootFolder)) {
                paths
                        .filter(path -> path.getFileName().toString().endsWith(".yml"))
                        .forEach(path -> {
                            System.out.println("Found quest config file: " + path);
                            List<QuestGroupDTO> groups = loadFileGroups(path);
                            for (QuestGroupDTO group : groups) {
                                questGroups.put(group.key(), group);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read quest config files", e);
            }

            return questGroups;
        });
    }

    public List<QuestGroupDTO> loadFileGroups(Path path) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        List<QuestGroupDTO> questGroups = new ArrayList<>();

        for (String groupKey : config.getKeys(false)) {
            ConfigurationSection groupSection = config.getConfigurationSection(groupKey);
            if (groupSection == null) continue;

            QuestGroupDTO groupDTO = parseGroup(groupKey, groupSection, path);
            questGroups.add(groupDTO);
        }

        return questGroups;
    }

    private QuestGroupDTO parseGroup(String groupKey, ConfigurationSection groupSection, Path path) {
        ConfigurationSection questsSection = groupSection.getConfigurationSection("quests");
        List<QuestConfigDTO> quests = new ArrayList<>();

        if (questsSection != null) {
            for (String questKey : questsSection.getKeys(false)) {
                ConfigurationSection questSection = questsSection.getConfigurationSection(questKey);
                if (questSection == null) continue;

                Map<String, QuestObjectiveConfigDTO> objectives = parseObjectives(questSection);
                QuestConfigDTO questConfig = new QuestConfigDTO(
                        questKey,
                        groupKey,
                        questSection.getString("name"),
                        questSection.getString("description"),
                        questSection.getLong("duration"),
                        questSection.getStringList("tags"),
                        questSection.getStringList("rewards"),
                        objectives
                );

                quests.add(questConfig);
            }
        }

        Instant startAt = null;
        Instant endAt = null;
        String startAtString = groupSection.getString("startAt");
        String endAtString = groupSection.getString("endAt");

        if (startAtString != null) {
            startAt = Instant.parse(startAtString);
        }
        if (endAtString != null) {
            endAt = Instant.parse(endAtString);
        }

        Integer maxActiveQuests = null;
        Integer maxPerPeriod = null;
        String periodSwitchCron = null;

        if (groupSection.contains("maxActiveQuests")) {
            maxActiveQuests = groupSection.getInt("maxActiveQuests");
        }
        if (groupSection.contains("maxPerPeriod")) {
            maxPerPeriod = groupSection.getInt("maxPerPeriod");
        }
        if (groupSection.contains("periodSwitchCron")) {
            periodSwitchCron = groupSection.getString("periodSwitchCron");
        }

        questConfigPaths.put(groupKey, path);
        return new QuestGroupDTO(
                groupKey,
                groupSection.getString("name"),
                groupSection.getString("description"),
                quests,
                QuestGroupType.valueOf(groupSection.getString("type")),
                maxActiveQuests,
                maxPerPeriod,
                periodSwitchCron,
                startAt,
                endAt
        );
    }

    private Map<String, QuestObjectiveConfigDTO> parseObjectives(ConfigurationSection questSection) {
        Map<String, QuestObjectiveConfigDTO> objectives = new HashMap<>();
        ConfigurationSection objectivesSection = questSection.getConfigurationSection("objectives");

        if (objectivesSection != null) {
            for (String objectiveKey : objectivesSection.getKeys(false)) {
                ConfigurationSection objectiveConfig = objectivesSection.getConfigurationSection(objectiveKey);
                if (objectiveConfig != null) {
                    Map<String, Object> data = objectiveConfig.getValues(true);
                    objectives.put(objectiveKey, new QuestObjectiveConfigDTO(objectiveKey, data));
                }
            }
        }

        return objectives;
    }
}

package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.*;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
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
import java.util.stream.Collectors;
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
    }

    @Override
    public CompletableFuture<Map<String, QuestGroupDTO>> getAllGroups() {
        Map<String, QuestGroupDTO> questGroups = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try (Stream<Path> paths = Files.walk(rootFolder)) {
                paths
                        .filter(path -> path.getFileName().toString().endsWith(".yml"))
                        .forEach(path -> {
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

                Map<String, QuestStageConfigDTO> stages = parseStages(questSection);
                List<QuestActionDTO> rewards = parseRewards(questSection);

                QuestConfigDTO questConfig = new QuestConfigDTO(
                        questKey,
                        groupKey,
                        questSection.getString("name"),
                        questSection.getString("description"),
                        questSection.getLong("duration"),
                        rewards,
                        stages
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

        questConfigPaths.put(groupKey, path);

        return new QuestGroupDTO(
                groupKey,
                groupSection.getString("name"),
                groupSection.getString("description"),
                quests,
                groupSection.getString("distribution_mode"),
                groupSection.getInt("max_active"),
                groupSection.getInt("max_per_period"),
                groupSection.getString("reset_cron"),
                startAt,
                endAt
        );

    }

    private Map<String, QuestStageConfigDTO> parseStages(ConfigurationSection questSection) {
        List<Map<?, ?>> stagesList = questSection.getMapList("stages");
        Map<String, QuestStageConfigDTO> stages = new HashMap<>();

        for (Map<?, ?> map : stagesList) {
            String stageKey = (String) map.get("key");
            if (stageKey == null) continue;

            String stageName = (String) map.get("name");
            String flowString = "PARALLEL";

            if (map.containsKey("flow")) {
                flowString = (String) map.get("flow");
            }

            Flow flow = Flow.valueOf(flowString.toUpperCase());

            Object rawObjectives = map.get("objectives");
            List<Map<?, ?>> objectivesList = new ArrayList<>();

            if (rawObjectives instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m) {
                        objectivesList.add(m);
                    }
                }
            }

            Map<String, QuestObjectiveConfigDTO> objectives = new HashMap<>();
            if (objectivesList != null) {
                for (Map<?, ?> objMap : objectivesList) {
                    Map<String, Object> config = objMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    e -> e.getKey().toString(),
                                    Map.Entry::getValue
                            ));
                    String objKey = (String) config.get("key");
                    String objType = (String) config.get("type");
                    if (objKey == null || objType == null) continue;

                    objectives.put(objKey, new QuestObjectiveConfigDTO(objKey, objType, config));
                }
            }

            QuestStageConfigDTO stageDTO = new QuestStageConfigDTO(
                    stageKey,
                    stageName,
                    flow,
                    objectives
            );

            stages.put(stageKey, stageDTO);
        }

        return stages;
    }


    private List<QuestActionDTO> parseRewards(ConfigurationSection questSection) {
        List<Map<?, ?>> rewardsList = questSection.getMapList("rewards");
        List<QuestActionDTO> rewards = new ArrayList<>();

        for (Map<?, ?> map : rewardsList) {
            Map<String, Object> config = map.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            Map.Entry::getValue
                    ));
            String type = (String) config.get("type");
            String name = (String) config.get("name");

            if (type == null) continue;

            rewards.add(new QuestActionDTO(type, name, config));
        }

        return rewards;
    }
}

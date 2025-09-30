package com.github.ibanetchep.msquests.core.quest.config.action;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class QuestAction implements Translatable, PlaceholderProvider {

    private final String type;
    private @Nullable String name = null;

    public QuestAction(QuestActionDTO rewardDto) {
        this.type = rewardDto.type();

        if (rewardDto.name() != null) {
            this.name = rewardDto.name();
        }
    }

    public String getType() {
        return type;
    }

    public @Nullable String getName() {
        return name;
    }

    public abstract void execute(Quest quest);

    public void execute(QuestObjective objective) {
        execute(objective.getQuest());
    }

    public abstract QuestActionDTO toDTO();

    @Override
    public String getTranslationKey() {
        return "action." + type;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of();
    }
}

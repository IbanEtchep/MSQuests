package com.github.ibanetchep.msquests.core.quest.config.action;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class QuestAction implements Translatable, PlaceholderProvider {

    private final String type;
    private @Nullable String name = null;
    private List<Condition> conditions = List.of();

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

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean testConditions(PlayerProfile profile) {
        return conditions.stream().allMatch(c -> c.test(profile));
    }

    public boolean testConditions(PlayerProfile profile, Map<String, String> contextPlaceholders) {
        return conditions.stream().allMatch(c -> c.test(profile, contextPlaceholders));
    }

    public abstract void execute(Quest quest);

    public void execute(QuestObjective objective) {
        execute(objective.getQuest());
    }

    public void execute(QuestActor actor) {
        // default no-op; override for actor-level events like distribution
    }

    public void execute(QuestActor actor, QuestGroupConfig groupConfig) {
        execute(actor);
    }

    public abstract QuestActionDTO toDTO();

    @Override
    public String getTranslationKey() {
        return "action." + type;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of();
    }
}

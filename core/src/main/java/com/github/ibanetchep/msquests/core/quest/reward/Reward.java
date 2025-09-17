package com.github.ibanetchep.msquests.core.quest.reward;

import com.github.ibanetchep.msquests.core.dto.RewardDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class Reward implements Translatable, PlaceholderProvider {

    private final String type;
    private final @Nullable String name;

    public Reward(RewardDTO rewardDto) {
        this.type = rewardDto.type();
        this.name = rewardDto.name();
    }

    public String getType() {
        return type;
    }

    public @Nullable String getName() {
        return name;
    }

    public abstract void give(QuestActor actor);
    public abstract RewardDTO toDTO();

    @Override
    public String getTranslationKey() {
        return "reward." + type;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of();
    }
}

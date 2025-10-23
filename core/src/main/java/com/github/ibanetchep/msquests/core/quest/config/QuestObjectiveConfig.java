package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Base class for all quest objective configs.
 */
public abstract class QuestObjectiveConfig implements Translatable, PlaceholderProvider {

    protected final String key;
    protected final String type;

    protected QuestObjectiveConfig(QuestObjectiveConfigDTO dto) {
        this.key = dto.key();
        this.type = dto.type();
    }

    /** Convert this params back to a DTO */
    public abstract QuestObjectiveConfigDTO toDTO();

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getTranslationKey() {
        return "objective." + type;
    }
}

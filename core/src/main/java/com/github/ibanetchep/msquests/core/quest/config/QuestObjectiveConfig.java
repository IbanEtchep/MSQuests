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
        loadConfig(dto.config());
    }

    /**
     * Convert a Map<String, Object> (already validated) into the class fields.
     */
    protected void loadConfig(Map<String, Object> config) {
        for (Field field : this.getClass().getDeclaredFields()) {
            ConfigField annotation = field.getAnnotation(ConfigField.class);
            if (annotation == null) continue;

            Object rawValue = config.get(annotation.name());
            Object value = convertValue(field.getType(), rawValue);

            try {
                field.setAccessible(true);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set config field: " + annotation.name(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Class<?> type, Object rawValue) {
        if (rawValue == null) return null;

        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class),
                    rawValue.toString().toUpperCase());
        }

        // Si c'est déjà du bon type, pas besoin de conversion
        if (type.isInstance(rawValue)) return rawValue;

        String strValue = rawValue.toString();
        if (type == int.class || type == Integer.class) return Integer.parseInt(strValue);
        if (type == long.class || type == Long.class) return Long.parseLong(strValue);
        if (type == double.class || type == Double.class) return Double.parseDouble(strValue);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(strValue);
        if (type == String.class) return strValue;

        return rawValue;
    }

    /** Convert this config back to a DTO */
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

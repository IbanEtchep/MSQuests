package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class QuestObjectiveConfig implements Translatable, PlaceholderProvider {

    protected final String key;
    protected final String type;
    protected boolean valid = false;

    protected QuestObjectiveConfig(QuestObjectiveConfigDTO dto) {
        this.key = dto.key();
        this.type = dto.type();
        loadConfig(dto.config());
    }

    public abstract QuestObjectiveConfigDTO toDTO();

    protected void loadConfig(Map<String, Object> config) {
        for (Field field : this.getClass().getDeclaredFields()) {
            ConfigField annotation = field.getAnnotation(ConfigField.class);
            if (annotation == null) continue;

            String fieldName = annotation.name();
            Object rawValue = config.get(fieldName);

            Object value = null;
            if (rawValue != null && !rawValue.toString().isBlank()) {
                value = convertValue(field.getType(), rawValue);
            }

            try {
                field.setAccessible(true);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set config field: " + fieldName, e);
            }
        }

        validate();
    }

    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            ConfigField annotation = field.getAnnotation(ConfigField.class);
            if (annotation == null) continue;
            if (!annotation.required()) continue;

            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value == null || (value instanceof String s && s.isBlank())) {
                    errors.put(annotation.name(), "Field is required");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        AtLeastOneOfFields atLeast = this.getClass().getAnnotation(AtLeastOneOfFields.class);
        if (atLeast != null) {
            boolean satisfied = false;
            for (String fieldName : atLeast.value()) {
                try {
                    Field field = this.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value != null && !(value instanceof String s && s.isBlank())) {
                        satisfied = true;
                        break;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    errors.put(fieldName, "Invalid field in AtLeastOneOfFields");
                }
            }
            if (!satisfied) {
                errors.put("class", String.format(atLeast.message(), String.join(", ", atLeast.value())));
            }
        }

        return errors;
    }

    public void validate() {
        Map<String, String> errors = getValidationErrors();
        valid = errors.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Class<?> type, Object rawValue) {
        if (rawValue == null) return null;
        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), rawValue.toString().toUpperCase());
        }
        if (type == int.class || type == Integer.class) return Integer.parseInt(rawValue.toString());
        if (type == String.class) return rawValue.toString();
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(rawValue.toString());
        return rawValue;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String getTranslationKey() {
        return "objective." + type;
    }
}
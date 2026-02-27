package com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.PLACEHOLDER)
public class PlaceholderObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "placeholder", required = true)
    private final String placeholder;

    @ConfigField(name = "value", required = true)
    private final String value;

    public PlaceholderObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        this.placeholder = dto.params().get("placeholder").toString();
        this.value = dto.params().get("value").toString();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getValue() {
        return value;
    }

    public boolean evaluateCondition(String resolvedValue) {
        String trimmed = value.trim();
        String operator;
        String expected;

        if (trimmed.startsWith(">=")) {
            operator = ">=";
            expected = trimmed.substring(2).trim();
        } else if (trimmed.startsWith("<=")) {
            operator = "<=";
            expected = trimmed.substring(2).trim();
        } else if (trimmed.startsWith("!=")) {
            operator = "!=";
            expected = trimmed.substring(2).trim();
        } else if (trimmed.startsWith(">")) {
            operator = ">";
            expected = trimmed.substring(1).trim();
        } else if (trimmed.startsWith("<")) {
            operator = "<";
            expected = trimmed.substring(1).trim();
        } else if (trimmed.startsWith("==")) {
            operator = "==";
            expected = trimmed.substring(2).trim();
        } else {
            operator = "==";
            expected = trimmed;
        }

        try {
            double resolvedNum = Double.parseDouble(resolvedValue.trim());
            double expectedNum = Double.parseDouble(expected);
            return switch (operator) {
                case ">=" -> resolvedNum >= expectedNum;
                case "<=" -> resolvedNum <= expectedNum;
                case ">" -> resolvedNum > expectedNum;
                case "<" -> resolvedNum < expectedNum;
                case "==" -> resolvedNum == expectedNum;
                case "!=" -> resolvedNum != expectedNum;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return switch (operator) {
                case "==" -> resolvedValue.trim().equals(expected);
                case "!=" -> !resolvedValue.trim().equals(expected);
                default -> false;
            };
        }
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "placeholder", placeholder,
                "value", value
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "placeholder", placeholder,
                        "value", value
                )
        );
    }
}

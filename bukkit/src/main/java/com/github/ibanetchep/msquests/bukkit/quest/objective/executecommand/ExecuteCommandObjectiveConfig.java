package com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.EXECUTE_COMMAND)
@AtLeastOneOfFields({"command"})
public class ExecuteCommandObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "command")
    private String command;

    public ExecuteCommandObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if(dto.params().containsKey("command")) {
            command = dto.params().get("command").toString();
        }
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "command", command
                )
        );
    }

    public String getCommand() {
        return command;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "command", command
        );
    }
}

package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Message that is sent to each online player that is actor of the quest.
 */

@ActionType("message")
@AtLeastOneOfFields({"message", "message_key"})
public class PlayerMessageAction extends BukkitQuestAction {

    @ConfigField(name = "message")
    private final @Nullable String message;
    @ConfigField(name = "message_key")
    private final @Nullable String messageKey;

    @ConfigField(name = "objective_template")
    private final @Nullable String objectiveTemplate;
    @ConfigField(name = "objective_template_key")
    private final @Nullable String objectiveTemplateKey;

    public PlayerMessageAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
        super(dto, plugin);
        this.message = (String) dto.params().get("message");
        this.messageKey = (String) dto.params().get("message_key");
        this.objectiveTemplate = (String) dto.params().get("objective_template");
        this.objectiveTemplateKey = (String) dto.params().get("objective_template_key");
    }

    @Override
    public void execute(Quest quest) {
        MessageBuilder messageBuilder = MessageBuilder.raw(resolveMessage())
                .applyPlaceholderResolver(quest);

        if(resolveObjectiveTemplate() != null) {
            List<QuestObjective> objectiveList = quest.getObjectives().values().stream().toList();
            String template = resolveObjectiveTemplate();
            messageBuilder.listPlaceholder("objectives", objectiveList, objective ->
                    MessageBuilder.raw(template).applyPlaceholderResolver(objective).toStringRaw());
        }

        getOnlinePlayers(quest).forEach(player -> {
            messageBuilder.applyPlaceholderResolver(player);

            if(quest.getActor().isMember(player.getUniqueId())) {
                player.sendMessage(messageBuilder.toComponent());
            }
        });
    }

    @Override
    public void execute(QuestObjective objective) {
        Quest quest = objective.getQuest();

        getOnlinePlayers(quest).forEach(player -> {
            MessageBuilder messageBuilder = MessageBuilder.raw(resolveMessage())
                    .applyPlaceholderResolver(objective)
                    .applyPlaceholderResolver(player);

            if(quest.getActor().isMember(player.getUniqueId())) {
                player.sendMessage(messageBuilder.toComponent());
            }
        });
    }

    private String resolveMessage() {
        if(messageKey != null) {
            return MessageBuilder.translatable(messageKey).toStringRaw();
        } else return Objects.requireNonNullElse(message, "");
    }

    private @Nullable String resolveObjectiveTemplate() {
        if(objectiveTemplateKey != null) {
            return MessageBuilder.translatable(objectiveTemplateKey).toStringRaw();
        } else {
            return objectiveTemplate;
        }
    }

    @Override
    public QuestActionDTO toDTO() {
        Map<String, Object> config = new HashMap<>();
        if(message != null) config.put("message", message);
        if(messageKey != null) config.put("message_key", messageKey);
        if(objectiveTemplate != null) config.put("objective_template", objectiveTemplate);
        if(objectiveTemplateKey != null) config.put("objective_template_key", objectiveTemplateKey);
        return new QuestActionDTO(getType(), getName(), config);
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "message", message != null ? message : "",
                "message_key", messageKey != null ? messageKey : ""
        );
    }
}

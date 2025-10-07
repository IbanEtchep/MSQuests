package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ActionBar message that supports direct message or lang key.
 */

@ActionType("action_bar")
@AtLeastOneOfFields({"message", "message_key"})
public class PlayerActionBarAction extends BukkitQuestAction {

    @ConfigField(name = "message")
    private final String message;

    @ConfigField(name = "message_key")
    private final String messageKey;

    public PlayerActionBarAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
        super(dto, plugin);
        this.message = (String) dto.params().get("message");
        this.messageKey = (String) dto.params().get("message_key");
    }

    @Override
    public void execute(Quest quest) {
        getOnlinePlayers(quest).forEach(player -> {
            if (quest.getActor().isMember(player.getUniqueId())) {
                player.sendActionBar(resolveMessage().applyPlaceholderResolver(quest)
                        .applyPlaceholderResolver(player).toComponent());
            }
        });
    }

    private MessageBuilder resolveMessage() {
        if (messageKey != null) {
            return MessageBuilder.translatable(messageKey);
        } else {
            return MessageBuilder.raw(Objects.requireNonNullElse(message, ""));
        }
    }

    @Override
    public QuestActionDTO toDTO() {
        Map<String, Object> config = new HashMap<>();
        if (message != null) config.put("message", message);
        if (messageKey != null) config.put("message_key", messageKey);
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
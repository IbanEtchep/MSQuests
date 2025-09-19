package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ActionBar message that supports direct message or lang key.
 */
public class PlayerActionBarAction extends QuestAction {

    private final String message;
    private final String messageKey;

    public PlayerActionBarAction(QuestActionDTO dto) {
        super(dto);
        this.message = (String) dto.config().get("message");
        this.messageKey = (String) dto.config().get("message_key");
    }

    @Override
    public void execute(Quest quest) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (quest.getActor().isActor(player.getUniqueId())) {
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
package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Title message that supports direct text or lang key for title and subtitle.
 */

@ActionType("title")
@AtLeastOneOfFields({"title", "title_key"})
@AtLeastOneOfFields({"subtitle", "subtitle_key"})
public class PlayerTitleAction extends BukkitQuestAction {

    @ConfigField(name = "title")
    private final String title;
    @ConfigField(name = "title_key")
    private final String titleKey;
    @ConfigField(name = "subtitle")
    private final String subtitle;
    @ConfigField(name = "subtitle_key")
    private final String subtitleKey;

    public PlayerTitleAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
        super(dto, plugin);
        this.title = (String) dto.params().get("title");
        this.titleKey = (String) dto.params().get("title_key");
        this.subtitle = (String) dto.params().get("subtitle");
        this.subtitleKey = (String) dto.params().get("subtitle_key");
    }

    @Override
    public void execute(Quest quest) {
        Component titleComponent = resolveTitle().applyPlaceholderResolver(quest).toComponent();
        Component subtitleComponent = resolveSubtitle().applyPlaceholderResolver(quest).toComponent();

        getOnlinePlayers(quest).forEach(player -> {
            if (quest.getActor().isMember(player.getUniqueId())) {
                player.showTitle(Title.title(titleComponent, subtitleComponent));
            }
        });
    }

    private MessageBuilder resolveTitle() {
        if (titleKey != null) return MessageBuilder.translatable(titleKey);
        return MessageBuilder.raw(Objects.requireNonNullElse(title, ""));
    }

    private MessageBuilder resolveSubtitle() {
        if (subtitleKey != null) return MessageBuilder.translatable(subtitleKey);
        return MessageBuilder.raw(Objects.requireNonNullElse(subtitle, ""));
    }

    @Override
    public QuestActionDTO toDTO() {
        Map<String, Object> config = new HashMap<>();
        if (title != null) config.put("title", title);
        if (titleKey != null) config.put("title_key", titleKey);
        if (subtitle != null) config.put("subtitle", subtitle);
        if (subtitleKey != null) config.put("subtitle_key", subtitleKey);
        return new QuestActionDTO(getType(), getName(), config);
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "title", title != null ? title : "",
                "title_key", titleKey != null ? titleKey : "",
                "subtitle", subtitle != null ? subtitle : "",
                "subtitle_key", subtitleKey != null ? subtitleKey : ""
        );
    }
}

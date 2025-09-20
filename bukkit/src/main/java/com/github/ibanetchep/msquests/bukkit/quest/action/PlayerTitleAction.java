package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Title message that supports direct text or lang key for title and subtitle.
 */
public class PlayerTitleAction extends BukkitQuestAction {

    private final String title;
    private final String titleKey;
    private final String subtitle;
    private final String subtitleKey;

    public PlayerTitleAction(QuestActionDTO dto) {
        super(dto);
        this.title = (String) dto.config().get("title");
        this.titleKey = (String) dto.config().get("title_key");
        this.subtitle = (String) dto.config().get("subtitle");
        this.subtitleKey = (String) dto.config().get("subtitle_key");
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

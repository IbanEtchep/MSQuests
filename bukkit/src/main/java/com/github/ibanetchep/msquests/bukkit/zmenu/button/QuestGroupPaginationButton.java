package com.github.ibanetchep.msquests.bukkit.zmenu.button;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestGroupPaginationButton extends PaginateButton {

    private final BukkitQuestsPlugin plugin;
    private final String groupKey;

    public QuestGroupPaginationButton(BukkitQuestsPlugin plugin, String groupKey) {
        this.plugin = plugin;
        this.groupKey = groupKey;
    }

    @Override
    public int getPaginationSize(Player player) {
        QuestGroupConfig groupConfig = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (groupConfig == null) {
            return 0;
        }
        return groupConfig.getOrderedQuests().size();
    }

    @Override
    public void onRender(Player player, InventoryEngine inventoryEngine) {
        QuestGroupConfig groupConfig = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (groupConfig == null) {
            return;
        }

        List<QuestConfig> quests = groupConfig.getOrderedQuests();
        QuestActor actor = plugin.getQuestActorRegistry().getActors().get(player.getUniqueId());

        this.paginate(quests, inventoryEngine, (slot, questConfig) -> {
            Placeholders placeholders = new Placeholders();
            placeholders.register("quest_name", questConfig.getName());
            placeholders.register("quest_description", questConfig.getDescription());
            placeholders.register("quest_key", questConfig.getKey());
            placeholders.register("quest_status", getQuestStatus(actor, groupConfig, questConfig));

            inventoryEngine.displayFinalButton(this, placeholders, slot);
        });
    }

    private String getQuestStatus(QuestActor actor, QuestGroupConfig groupConfig, QuestConfig questConfig) {
        if (actor == null) {
            return "UNAVAILABLE";
        }

        ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
        if (actorGroup == null) {
            return "AVAILABLE";
        }

        String key = questConfig.getKey();
        if (actorGroup.hasActive(key)) {
            return "IN_PROGRESS";
        }
        if (actorGroup.hasStarted(key)) {
            return "COMPLETED";
        }
        return "AVAILABLE";
    }
}

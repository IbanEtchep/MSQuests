package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Gives an item to each online player that is actor of the quest.
 */

@ActionType("give_item")
public class GiveItemAction extends BukkitQuestAction {

    @ConfigField(name = "material", required = true)
    private final Material item;

    @ConfigField(name = "amount", required = true)
    private final int amount;

    public GiveItemAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
        super(dto, plugin);
        this.item = Material.valueOf(((String) dto.config().get("material")).toUpperCase());
        this.amount = (int) dto.config().get("amount");
    }

    @Override
    public void execute(Quest quest) {
        getOnlinePlayers(quest).forEach(player -> {
            Map<Integer, ItemStack> notFittedItems = player.getInventory().addItem(new ItemStack(item, amount));
            if(!notFittedItems.isEmpty()) {
                for(var entry : notFittedItems.entrySet()) {
                    player.getWorld().dropItem(player.getLocation(), entry.getValue());
                }
            }
        });
    }

    @Override
    public QuestActionDTO toDTO() {
        return new QuestActionDTO(getType(), getName(), Map.of("material", item.name(), "amount", amount));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "item", "<lang:" + item.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }
}

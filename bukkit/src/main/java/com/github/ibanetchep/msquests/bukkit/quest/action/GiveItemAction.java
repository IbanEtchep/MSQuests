package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Gives an item to each online player that is actor of the quest.
 */
public class GiveItemAction extends BukkitQuestAction {

    private final Material item;
    private final int amount;

    public GiveItemAction(QuestActionDTO dto) {
        super(dto);
        this.item = Material.valueOf(((String) dto.config().get("item")).toUpperCase());
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

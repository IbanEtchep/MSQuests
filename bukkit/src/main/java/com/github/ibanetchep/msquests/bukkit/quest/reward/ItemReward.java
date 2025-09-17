package com.github.ibanetchep.msquests.bukkit.quest.reward;

import com.github.ibanetchep.msquests.core.dto.RewardDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.reward.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Map;

public class ItemReward extends Reward {

    private final Material material;
    private final int amount;

    public ItemReward(RewardDTO dto) {
        super(dto);
        this.material = Material.valueOf((String) dto.config().get("material"));
        this.amount = (int) dto.config().get("amount");
    }

    @Override
    public void give(QuestActor actor) {

    }

    @Override
    public RewardDTO toDTO() {
        return new RewardDTO(getType(), getName(), Map.of("material", material.name(), "amount", amount));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "material", "<lang:" + material.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }
}

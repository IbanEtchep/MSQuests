package com.github.ibanetchep.msquests.bukkit.zmenu.button;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class QuestGroupPaginationButton extends PaginateButton {

    private final BukkitQuestsPlugin plugin;
    private final String groupKey;
    private final boolean assignedOnly;
    private final String actionsLore;
    private final Material availableMaterial;
    private final Material activeMaterial;
    private final Material completedMaterial;
    private final Map<Integer, Placeholders> slotPlaceholders = new HashMap<>();
    private final Set<Integer> completedSlots = new HashSet<>();

    public QuestGroupPaginationButton(BukkitQuestsPlugin plugin, String groupKey, boolean assignedOnly,
                                      List<String> actionsLore, Material availableMaterial, Material activeMaterial, Material completedMaterial) {
        this.plugin = plugin;
        this.groupKey = groupKey;
        this.assignedOnly = assignedOnly;
        this.actionsLore = String.join("\n", actionsLore);
        this.availableMaterial = availableMaterial;
        this.activeMaterial = activeMaterial;
        this.completedMaterial = completedMaterial;
    }

    @Override
    public int getPaginationSize(Player player) {
        QuestGroupConfig groupConfig = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (groupConfig == null) return 0;
        if (!assignedOnly) return groupConfig.getOrderedQuests().size();

        QuestActor actor = plugin.getQuestActorRegistry().getActors().get(player.getUniqueId());
        if (actor == null) return 0;
        ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
        if (actorGroup == null) return 0;
        return (int) groupConfig.getOrderedQuests().stream()
                .filter(qc -> actorGroup.hasStartedInCurrentPeriod(qc.getKey()))
                .count();
    }

    @Override
    public void onRender(Player player, InventoryEngine inventoryEngine) {
        QuestGroupConfig groupConfig = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (groupConfig == null) return;

        QuestActor actor = plugin.getQuestActorRegistry().getActors().get(player.getUniqueId());
        ActorQuestGroup actorGroup = actor != null ? actor.getActorQuestGroup(groupConfig) : null;

        List<QuestConfig> quests = groupConfig.getOrderedQuests();
        if (assignedOnly && actorGroup != null) {
            quests = quests.stream()
                    .filter(qc -> actorGroup.hasStartedInCurrentPeriod(qc.getKey()))
                    .toList();
        }

        slotPlaceholders.clear();
        completedSlots.clear();
        this.paginate(quests, inventoryEngine, (slot, questConfig) -> {
            String questKey = questConfig.getKey();
            Quest attempt = actorGroup != null ? actorGroup.getCurrentAttempt(questKey) : null;
            boolean isActive = attempt != null && attempt.isActive();
            // "Finished this period": completed, but also failed — either way the player is
            // done with it, so the slot stays locked and shows the completed material.
            boolean isCompleted = attempt != null && !isActive;

            Placeholders placeholders = new Placeholders();
            placeholders.register("quest_name", questConfig.getName());
            placeholders.register("quest_description", questConfig.getDescription());
            placeholders.register("quest_key", questKey);
            placeholders.register("quest_status", getQuestStatus(actorGroup, questConfig));
            placeholders.register("quest_status_key", getQuestStatusKey(actorGroup, questConfig));

            if (questConfig.getTier() != null) {
                placeholders.register("quest_tier", questConfig.getTier());
                QuestTierConfig tierConfig = groupConfig.getTier(questConfig.getTier());
                if (tierConfig != null) {
                    placeholders.register("quest_tier_name", tierConfig.getName());
                }
            }

            addProgressPlaceholders(placeholders, actorGroup, questKey);
            addRewardsPlaceholder(placeholders, questConfig);

            if (isCompleted) {
                completedSlots.add(slot);
            }
            placeholders.register("quest_is_completed", String.valueOf(isCompleted));
            placeholders.register("quest_actions_lore", isCompleted ? "" : actionsLore);

            Material material = isCompleted ? completedMaterial : (isActive ? activeMaterial : availableMaterial);
            this.getItemStack().setMaterial(material.name());

            slotPlaceholders.put(slot, placeholders);
            inventoryEngine.displayFinalButton(this, placeholders, slot);
        });
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryEngine inventoryEngine, int slot, Placeholders placeholders) {
        if (completedSlots.contains(slot)) return;

        Placeholders slotPlaceholder = slotPlaceholders.get(slot);
        if (slotPlaceholder != null) {
            placeholders = slotPlaceholder;
        }
        super.onClick(player, event, inventoryEngine, slot, placeholders);
    }

    /**
     * The attempt's own status. A failed quest used to read as "completed", because
     * {@code hasStartedInCurrentPeriod} accepts every status but EXPIRED.
     */
    private String getQuestStatus(ActorQuestGroup actorGroup, QuestConfig questConfig) {
        Quest attempt = currentAttempt(actorGroup, questConfig);
        return attempt != null
                ? BukkitTranslator.raw(attempt.getStatus())
                : BukkitTranslator.raw(TranslationKey.ZMENU_QUEST_STATUS_AVAILABLE);
    }

    private String getQuestStatusKey(ActorQuestGroup actorGroup, QuestConfig questConfig) {
        Quest attempt = currentAttempt(actorGroup, questConfig);
        return attempt != null ? attempt.getStatus().name() : "AVAILABLE";
    }

    private @Nullable Quest currentAttempt(ActorQuestGroup actorGroup, QuestConfig questConfig) {
        return actorGroup != null ? actorGroup.getCurrentAttempt(questConfig.getKey()) : null;
    }

    private void addRewardsPlaceholder(Placeholders placeholders, QuestConfig questConfig) {
        List<QuestAction> rewards = questConfig.getRewards();
        if (rewards.isEmpty()) {
            placeholders.register("quest_rewards", "Aucune");
            return;
        }

        StringJoiner joiner = new StringJoiner("\n");
        for (QuestAction reward : rewards) {
            String name = reward.getName();

            if (name != null && !name.isEmpty()) {
                joiner.add(name);
            } else {
                joiner.add(reward.getType());
            }
        }
        placeholders.register("quest_rewards", joiner.toString());
    }

    private void addProgressPlaceholders(Placeholders placeholders, ActorQuestGroup actorGroup, String questKey) {
        Quest attempt = actorGroup != null ? actorGroup.getCurrentAttempt(questKey) : null;
        if (attempt == null) {
            placeholders.register("quest_objective_progress", "-");
            return;
        }

        QuestObjective currentObjective = attempt.isActive()
                ? attempt.getStagesList().stream()
                        .flatMap(s -> s.getObjectives().values().stream())
                        .filter(o -> !o.isCompleted())
                        .findFirst()
                        .orElse(null)
                : null;

        if (currentObjective != null) {
            placeholders.register("quest_objective_progress",
                    currentObjective.getProgress() + "/" + currentObjective.getTarget());
        } else if (attempt.isActive()) {
            placeholders.register("quest_objective_progress",
                    BukkitTranslator.raw(TranslationKey.ZMENU_QUEST_PROGRESS_COMPLETED));
        } else {
            // Was a hardcoded French "Complétée !" — and wrong for a failed attempt.
            placeholders.register("quest_objective_progress", BukkitTranslator.raw(attempt.getStatus()));
        }
    }
}

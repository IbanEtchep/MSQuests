package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.ObjectiveCompletedEvent;
import com.github.ibanetchep.msquests.bukkit.event.ObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.bukkit.event.QuestCompleteEvent;
import com.github.ibanetchep.msquests.bukkit.event.QuestStartedEvent;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestListeners implements Listener {

    private final BukkitQuestsPlugin plugin;

    public QuestListeners(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onStart(QuestStartedEvent event) {
        Quest quest = event.getQuest();

        for (QuestAction action : quest.getQuestGroup().getQuestStartActions()) {
            action.execute(quest);
        }
    }

    @EventHandler
    public void onComplete(QuestCompleteEvent event) {
        Quest quest = event.getQuest();

        for (QuestAction action : quest.getQuestGroup().getQuestCompleteActions()) {
            action.execute(quest);
        }
    }

    @EventHandler
    public void onProgress(ObjectiveProgressedEvent event) {
        QuestObjective objective = event.getObjective();
        Quest quest = objective.getQuest();

        for (QuestAction action : quest.getQuestGroup().getObjectiveProgressActions()) {
            action.execute(objective);
        }
    }

    @EventHandler
    public void onObjectiveComplete(ObjectiveCompletedEvent event) {
        QuestObjective objective = event.getObjective();
        Quest quest = objective.getQuest();

        for (QuestAction action : quest.getQuestGroup().getObjectiveCompleteActions()) {
            action.execute(objective);
        }
    }
}

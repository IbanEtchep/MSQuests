package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.ObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestProgressListener implements Listener {

    private final MSQuestsPlugin plugin;

    public QuestProgressListener(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onStart(ObjectiveProgressedEvent event) {
        QuestObjective objective = event.getObjective();
        Quest quest = objective.getQuest();
        QuestActor actor = quest.getActor();

        for (QuestAction action : quest.getQuestGroup().getObjectiveCompleteActions()) {
            action.execute(objective);
        }
    }

}

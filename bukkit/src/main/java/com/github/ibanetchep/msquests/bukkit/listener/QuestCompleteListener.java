package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.QuestCompleteEvent;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestCompleteListener implements Listener {

    private final MSQuestsPlugin plugin;

    public QuestCompleteListener(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onComplete(QuestCompleteEvent event) {
        Quest quest = event.getQuest();
        QuestActor actor = event.getQuest().getActor();

        for (QuestAction action : quest.getQuestGroup().getQuestCompleteActions()) {
            action.execute(quest);
        }
    }
}

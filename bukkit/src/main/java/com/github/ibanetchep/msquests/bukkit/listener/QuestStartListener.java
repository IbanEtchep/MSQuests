package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.PlayerQuestStartEvent;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestGlobalActor;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class QuestStartListener implements Listener {

    private final MSQuestsPlugin plugin;

    public QuestStartListener(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onStart(PlayerQuestStartEvent event) {
        QuestActor actor = event.getActor();
        QuestConfig questConfig = event.getQuestConfig();

        if(actor instanceof QuestPlayerActor playerActor) {
            StringBuilder objectives = new StringBuilder();

            for (QuestObjectiveConfig objectiveConfig : questConfig.getObjectives().values()) {
                objectives.append(Translator.raw(TranslationKey.PLAYER_QUEST_STARTED_OBJECTIVE, Map.of(
                        "objective", Translator.raw(objectiveConfig)
                )));
            }

            System.out.println(objectives.toString());

            playerActor.sendMessage(Translator.t(TranslationKey.PLAYER_QUEST_STARTED_BODY, Map.of(
                    "quest", questConfig.getName(),
                    "description", questConfig.getDescription(),
                    "objectives", objectives.toString()
            )));
        }

        if(actor instanceof QuestGlobalActor globalActor) {
            StringBuilder objectives = new StringBuilder();

            for (QuestObjectiveConfig objectiveConfig : questConfig.getObjectives().values()) {
                objectives.append(Translator.raw(TranslationKey.GLOBAL_QUEST_STARTED_OBJECTIVE, Map.of(
                        "objective", Translator.raw(objectiveConfig)
                )));
            }

            globalActor.sendMessage(Translator.t(TranslationKey.GLOBAL_QUEST_STARTED_BODY, Map.of(
                    "quest", questConfig.getName(),
                    "description", questConfig.getDescription(),
                    "objectives", objectives.toString()
            )));
        }
    }

}

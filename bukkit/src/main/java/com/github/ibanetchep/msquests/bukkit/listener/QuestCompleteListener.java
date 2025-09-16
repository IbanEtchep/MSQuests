package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.PlayerQuestCompleteEvent;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestGlobalActor;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class QuestCompleteListener implements Listener {

    private final MSQuestsPlugin plugin;

    public QuestCompleteListener(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onComplete(PlayerQuestCompleteEvent event) {
        Quest quest = event.getQuest();
        QuestActor actor = quest.getActor();
        QuestConfig questConfig = quest.getQuestConfig();
        Player player = event.getPlayer();

        if(actor instanceof QuestPlayerActor) {
            if(plugin.getConfiguration().getBoolean("player_quest.notification.complete.show_message", true)) {
                StringBuilder objectives = new StringBuilder();

                for (QuestObjectiveConfig objectiveConfig : questConfig.getObjectives().values()) {
                    objectives.append(Translator.raw(TranslationKey.PLAYER_QUEST_COMPLETED_OBJECTIVE, Map.of(
                            "objective", Translator.raw(objectiveConfig)
                    )));
                }

                player.sendMessage(Translator.t(TranslationKey.PLAYER_QUEST_COMPLETED_BODY, Map.of(
                        "quest", questConfig.getName(),
                        "description", questConfig.getDescription(),
                        "objectives", objectives.toString()
                )));
            }

            if(plugin.getConfiguration().getBoolean("player_quest.notification.complete.show_title", true)) {
                player.showTitle(Title.title(
                        Translator.t(TranslationKey.PLAYER_QUEST_COMPLETED_TITLE, Map.of(
                                "quest", questConfig.getName(),
                                "description", questConfig.getDescription()
                        )),
                        Translator.t(TranslationKey.PLAYER_QUEST_COMPLETED_SUBTITLE, Map.of(
                                "quest", questConfig.getName(),
                                "description", questConfig.getDescription()
                        )))
                );
            }

            if(plugin.getConfiguration().getBoolean("player_quest.notification.complete.show_action_bar", true)) {
                player.sendActionBar(Translator.t(TranslationKey.PLAYER_QUEST_COMPLETED_ACTION_BAR, Map.of(
                        "quest", questConfig.getName(),
                        "description", questConfig.getDescription()
                )));
            }
        }

        if(actor instanceof QuestGlobalActor) {
            if(plugin.getConfiguration().getBoolean("global_quest.notification.complete.show_message", true)) {
                StringBuilder objectives = new StringBuilder();

                for (QuestObjectiveConfig objectiveConfig : questConfig.getObjectives().values()) {
                    objectives.append(Translator.raw(TranslationKey.GLOBAL_QUEST_COMPLETED_OBJECTIVE, Map.of(
                            "objective", Translator.raw(objectiveConfig)
                    )));
                }

                player.sendMessage(Translator.t(TranslationKey.GLOBAL_QUEST_COMPLETED_BODY, Map.of(
                        "quest", questConfig.getName(),
                        "description", questConfig.getDescription(),
                        "objectives", objectives.toString()
                )));
            }

            if(plugin.getConfiguration().getBoolean("global_quest.notification.complete.show_title", true)) {
                player.showTitle(Title.title(
                        Translator.t(TranslationKey.GLOBAL_QUEST_COMPLETED_TITLE, Map.of(
                                "quest", questConfig.getName(),
                                "description", questConfig.getDescription()
                        )),
                        Translator.t(TranslationKey.GLOBAL_QUEST_COMPLETED_SUBTITLE, Map.of(
                                "quest", questConfig.getName(),
                                "description", questConfig.getDescription()
                        ))
                ));
            }

            if(plugin.getConfiguration().getBoolean("global_quest.notification.complete.show_action_bar", true)) {
                player.sendActionBar(Translator.t(TranslationKey.GLOBAL_QUEST_COMPLETED_ACTION_BAR, Map.of(
                        "quest", questConfig.getName(),
                        "description", questConfig.getDescription()
                )));
            }
        }
    }

}

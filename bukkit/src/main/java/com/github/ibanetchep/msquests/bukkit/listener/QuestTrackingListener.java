package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.event.ObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.bukkit.event.QuestCompleteEvent;
import com.github.ibanetchep.msquests.bukkit.service.QuestPlayerService;
import com.github.ibanetchep.msquests.bukkit.service.TrackingBossBarService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuestTrackingListener implements Listener {

    private final TrackingBossBarService trackingBossBarService;
    private final QuestPlayerService questPlayerService;

    public QuestTrackingListener(TrackingBossBarService trackingBossBarService, QuestPlayerService questPlayerService) {
        this.trackingBossBarService = trackingBossBarService;
        this.questPlayerService = questPlayerService;
    }

    @EventHandler
    public void onObjectiveProgressed(ObjectiveProgressedEvent event) {
        trackingBossBarService.onQuestProgress(event.getObjective());
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent event) {
        trackingBossBarService.onQuestComplete(event.getQuest());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        trackingBossBarService.hideBossBar(player);
        questPlayerService.unloadPlayer(player);
    }
}

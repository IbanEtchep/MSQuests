package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.bukkit.config.PlaceholdersConfig;
import com.github.ibanetchep.msquests.bukkit.placeholder.QuestPlaceholders;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The per-quest placeholders the conditions of menus and chains read ({@code %quests_status_<group>:<quest>%}). */
class QuestPlaceholdersTest {

    private final UUID playerId = UUID.randomUUID();
    private QuestPlaceholders placeholders;
    private TestActor actor;
    private QuestConfig mineStone;

    @BeforeEach
    void setUp() {
        QuestGroupConfig daily = ArtisanFixtures.group("daily", "player");
        mineStone = ArtisanFixtures.quest(daily, "mine_stone", "easy");
        ArtisanFixtures.quest(daily, "fish", "easy");
        QuestConfigRegistry configs = new QuestConfigRegistry();
        configs.registerQuestGroupConfig(daily);

        actor = new TestActor(playerId, "Steve", "player");
        QuestActorRegistry actors = new QuestActorRegistry();
        actors.registerActor(actor);
        PlayerProfileRegistry profiles = new PlayerProfileRegistry();
        profiles.registerPlayerProfile(new PlayerProfile(playerId, "Steve"));

        placeholders = new QuestPlaceholders(profiles, configs, actors, new PlaceholdersConfig(5));
    }

    @Test
    void statusAndCompletedFollowTheCurrentAttempt() {
        assertEquals("NOT_STARTED", placeholders.resolve(playerId, "status_daily:mine_stone"));
        assertEquals("false", placeholders.resolve(playerId, "completed_daily:mine_stone"));

        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 3);
        assertEquals("IN_PROGRESS", placeholders.resolve(playerId, "status_daily:mine_stone"));
        assertEquals("false", placeholders.resolve(playerId, "completed_daily:mine_stone"));
        assertEquals("NOT_STARTED", placeholders.resolve(playerId, "status_daily:fish"));
    }

    @Test
    void aCompletedAttemptReadsTrue() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.COMPLETED, 64);

        assertEquals("COMPLETED", placeholders.resolve(playerId, "status_daily:mine_stone"));
        assertEquals("true", placeholders.resolve(playerId, "completed_daily:mine_stone"));
    }

    @Test
    void unknownGroupIsNotStartedAndUnknownKeyIsNull() {
        assertEquals("NOT_STARTED", placeholders.resolve(playerId, "status_weekly:mine_stone"));
        assertNull(placeholders.resolve(playerId, "nope"));
        assertEquals("", placeholders.resolve(null, "tracked_name"));
    }
}

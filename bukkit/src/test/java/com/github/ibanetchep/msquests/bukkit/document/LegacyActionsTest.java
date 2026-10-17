package com.github.ibanetchep.msquests.bukkit.document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every MSQuests action of the shipped {@code daily.yml} (docker/plugins/MSQuests/quests), and its
 * Artisan equivalent. The YAML below is that file's {@code actions} section and first quest, as is.
 */
class LegacyActionsTest {

    private static final String DAILY = """
            key: daily
            actions:
              actor_load: []
              quest_distribution:
                - type: message
                  message: "<#FFD166>✦ <white>Tes <#FFD166>6 quêtes journalières <white>ont été distribuées ! Utilise <#FFD166>/daily <white>pour les consulter."
                - type: title
                  title: "<#FFD166>Quêtes journalières"
                  subtitle: "<white>Tes quêtes du jour sont disponibles !"
              quest_start: []
              quest_complete:
                - type: message
                  message: |-
                    <#2ecc71>Tu as complété la quête %quest_name% !
                    Objectifs : %objectives%
                  objective_template: <newline>   - %objective_name%
                - type: title
                  title: <#2ecc71>Quête complétée !
                  subtitle: '%quest_name%'
              objective_progress:
                - type: boss_bar
                  conditions:
                    - type: placeholder
                      placeholder: "%msquests_tracked_name%"
                      action: DIFFERENT_STRING
                      value: "%quest_name%"
                  message: '%objective_name% - %objective_progress%'
              objective_complete:
                - type: boss_bar
                  message: '<green>%objective_name% - %objective_progress%</green>'
              all_period_quests_complete:
                - type: message
                  message: "<#FFD166>★ <white>Bravo ! Tu as complété toutes tes <#FFD166>quêtes du jour<white> ! Voici ton bonus !"
                - type: title
                  title: "<#FFD166>★ Toutes les quêtes complétées !"
                  subtitle: "<white>+1000 XP bonus +5000$"
                - type: command
                  command: "levels addxp %actor_name% 1000"
                - type: command
                  command: "eco give %actor_name% 5000"
            quests:
              - key: mine_stone_64
                rewards:
                  - type: "command"
                    name: "<white>+150 XP"
                    command: "levels addxp %actor_name% 150"
                  - type: "command"
                    name: "<white>+500$"
                    command: "eco give %actor_name% 500"
            """;

    private Map<String, Object> group;
    private Map<String, Object> hooks;
    private LegacyActions converter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void convert() throws Exception {
        Map<String, Object> tree = new ObjectMapper(new YAMLFactory()).readValue(DAILY, new TypeReference<>() {});
        converter = new LegacyActions(key -> null);
        group = converter.upgradeGroup(tree);
        hooks = (Map<String, Object>) group.get("actions");
    }

    private static Map<String, Object> args(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], pairs[i + 1]);
        return map;
    }

    @Test
    void emptyHooksStayEmpty() {
        assertEquals(List.of(), hooks.get("actor_load"));
        assertEquals(List.of(), hooks.get("quest_start"));
    }

    @Test
    void messageAndTitleBecomeSendMessageAndShowTitle() {
        assertEquals(List.of(
                Map.of("send_message", "<#FFD166>✦ <white>Tes <#FFD166>6 quêtes journalières <white>ont été distribuées !"
                        + " Utilise <#FFD166>/daily <white>pour les consulter."),
                Map.of("show_title", args("title", "<#FFD166>Quêtes journalières",
                        "subtitle", "<white>Tes quêtes du jour sont disponibles !"))
        ), hooks.get("quest_distribution"));
    }

    @Test
    void questCompleteBindsTheQuestAndMovesTheObjectiveTemplateToTheGroup() {
        assertEquals(List.of(
                Map.of("send_message", "<#2ecc71>Tu as complété la quête {quest_name} !\nObjectifs : {objectives}"),
                Map.of("show_title", args("title", "<#2ecc71>Quête complétée !", "subtitle", "{quest_name}"))
        ), hooks.get("quest_complete"));
        assertEquals("<newline>   - {objective_name}", group.get("objective_line"));
    }

    @Test
    void conditionalBossBarBecomesAnIfOnAnExpressionReadingTheQuestBinding() {
        // %quest_name% is a chain binding: only `expression` reads bindings, as bare names.
        assertEquals(List.of(Map.of(
                "if", List.of(Map.of("expression", "%quests_tracked_name% != quest_name")),
                "do", List.of(Map.of("quests:boss_bar", args("text", "{objective_name} - {objective_progress}")))
        )), hooks.get("objective_progress"));
    }

    @Test
    void bossBarBecomesQuestsBossBar() {
        assertEquals(List.of(Map.of("quests:boss_bar",
                args("text", "<green>{objective_name} - {objective_progress}</green>"))
        ), hooks.get("objective_complete"));
    }

    @Test
    void commandsRunAsConsoleWithTheActorBinding() {
        List<?> steps = (List<?>) hooks.get("all_period_quests_complete");
        assertEquals(4, steps.size());
        assertEquals(Map.of("send_message", "<#FFD166>★ <white>Bravo ! Tu as complété toutes tes <#FFD166>quêtes du jour<white>"
                + " ! Voici ton bonus !"), steps.get(0));
        assertEquals(Map.of("show_title", args("title", "<#FFD166>★ Toutes les quêtes complétées !",
                "subtitle", "<white>+1000 XP bonus +5000$")), steps.get(1));
        assertEquals(Map.of("run_command", args("command", "levels addxp {actor_name} 1000", "as", "console")), steps.get(2));
        assertEquals(Map.of("run_command", args("command", "eco give {actor_name} 5000", "as", "console")), steps.get(3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rewardsBecomeAChainAndTheirNamesTheDisplay() {
        Map<String, Object> quest = (Map<String, Object>) ((List<?>) group.get("quests")).get(0);
        assertEquals(List.of(
                Map.of("run_command", args("command", "levels addxp {actor_name} 150", "as", "console")),
                Map.of("run_command", args("command", "eco give {actor_name} 500", "as", "console"))
        ), quest.get("rewards"));
        assertEquals(List.of("<white>+150 XP", "<white>+500$"), quest.get("reward_display"));
    }

    @Test
    void dailyConvertsWithoutApproximation() {
        assertEquals(List.of(), converter.notes());
    }

    @Test
    void anAlreadyConvertedGroupIsLeftAsIs() throws Exception {
        Map<String, Object> again = new LegacyActions(key -> null).upgradeGroup(
                new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(group), new TypeReference<>() {}));
        assertEquals(group, again);
    }

    // ------------------------------------------------------- beyond daily.yml

    @Test
    void literalPlaceholderComparisonsUsePlaceholderConditions() {
        assertEquals(List.of(Map.of("not", Map.of("placeholder_equals",
                        args("placeholder", "%player_world%", "value", "world_nether")))),
                converter.conditions(List.of(Map.of("type", "placeholder", "placeholder", "%player_world%",
                        "action", "DIFFERENT_STRING", "value", "world_nether")), "t"));
        assertEquals(List.of(Map.of("expression", "%player_level% >= 10")),
                converter.conditions(List.of(Map.of("type", "placeholder", "placeholder", "%player_level%",
                        "action", "SUPERIOR_OR_EQUAL", "value", "10")), "t"));
        assertEquals(List.of(Map.of("has_permission", "quests.vip")),
                converter.conditions(List.of(Map.of("type", "permission", "permission", "quests.vip")), "t"));
    }

    @Test
    void giveItemAndLangKeysAreConvertedWithANote() {
        LegacyActions withKeys = new LegacyActions(key -> key.equals("custom.done") ? "<green>Fini %quest_name%" : null);
        assertEquals(List.of(
                Map.of("give_item", args("material", "DIAMOND", "count", 3)),
                Map.of("send_message", "<green>Fini {quest_name}"),
                Map.of("quests:action_bar", "{objective_progress}")
        ), withKeys.actions(List.of(
                Map.of("type", "give_item", "material", "diamond", "amount", 3),
                Map.of("type", "message", "message_key", "custom.done"),
                Map.of("type", "action_bar", "message", "%objective_progress%")), "t"));
        assertEquals(1, withKeys.notes().size(), withKeys.notes()::toString);
        assertTrue(withKeys.notes().get(0).contains("give_item"));
    }

    @Test
    void anUnknownActionTypeIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.actions(List.of(Map.of("type", "teleport")), "t"));
    }
}

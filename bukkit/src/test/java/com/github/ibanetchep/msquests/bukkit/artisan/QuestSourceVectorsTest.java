package com.github.ibanetchep.msquests.bukkit.artisan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentReader;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjectiveConfig;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TRANSLATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * The vectors of {@code testdata/sources/}: a quest group as the editor writes it, and the
 * definition fields its rows must carry. The editor computes the same rows from the draft
 * ({@code editor/src/sourceRows.test.ts} reads these files), so a field that changes here and
 * not there fails one of the two suites. Its {@code instances} are stored attempts: rebuilt
 * against the document the way the server loads them after a deploy, they give the rows the
 * editor must compute by laying the deployed rows' progress over the draft.
 */
class QuestSourceVectorsTest {

    private static final File VECTORS = new File("../testdata/sources");
    private static final ObjectMapper JSON = new ObjectMapper();

    @TestFactory
    List<DynamicTest> rowsMatchTheSharedVectors() throws Exception {
        List<DynamicTest> tests = new ArrayList<>();
        for (File file : Objects.requireNonNull(VECTORS.listFiles((dir, name) -> name.endsWith(".json")))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> vector = JSON.readValue(file, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> document = (Map<String, Object>) vector.get("document");
            @SuppressWarnings("unchecked")
            Map<String, List<Object>> expected = (Map<String, List<Object>>) vector.get("expected");
            QuestObjectiveFactory objectives = objectiveFactory();
            QuestGroupConfig group = load(document, (String) vector.get("lang"), objectives);

            tests.add(DynamicTest.dynamicTest(file.getName() + ": " + MsQuestsArtisanModule.GROUPS, () -> {
                Object rows = List.of(QuestRows.groupRow(group, null, TRANSLATOR));
                assertEquals(expected.get(MsQuestsArtisanModule.GROUPS),
                        pick(json(rows), expected.get(MsQuestsArtisanModule.GROUPS)));
            }));
            tests.add(DynamicTest.dynamicTest(file.getName() + ": " + MsQuestsArtisanModule.QUESTS, () -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                for (QuestConfig quest : group.getOrderedQuests()) {
                    rows.add(QuestRows.questRow(group, quest, null, TRANSLATOR));
                }
                assertEquals(expected.get(MsQuestsArtisanModule.QUESTS),
                        pick(json(rows), expected.get(MsQuestsArtisanModule.QUESTS)));
            }));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> instances =
                    (List<Map<String, Object>>) vector.getOrDefault("instances", List.of());
            for (Map<String, Object> instance : instances) {
                tests.add(DynamicTest.dynamicTest(
                        file.getName() + ": " + MsQuestsArtisanModule.ACTIVE + ", " + instance.get("name"), () -> {
                    Quest quest = new QuestFactory(objectives).buildQuest(
                            group.getQuestConfigs().get((String) instance.get("quest")),
                            new ArtisanFixtures.TestActor(UUID.randomUUID(), "Steve", "player"),
                            dto(instance));
                    Object row = json(QuestRows.activeRow(quest, null, TRANSLATOR));
                    assertEquals(instance.get("expected"), pick(row, instance.get("expected")));
                }));
            }
        }
        return tests;
    }

    /** What the database hands back for an attempt: each objective's progress and status. */
    @SuppressWarnings("unchecked")
    private static QuestDTO dto(Map<String, Object> instance) {
        UUID id = UUID.randomUUID();
        Map<String, QuestObjectiveDTO> objectives = new LinkedHashMap<>();
        ((Map<String, Map<String, Object>>) instance.get("objectives")).forEach((key, o) ->
                objectives.put(key, new QuestObjectiveDTO(id, key, "", QuestObjectiveStatus.valueOf((String) o.get("status")),
                        (Integer) o.get("progress"))));
        return new QuestDTO(id, (String) instance.get("quest"), "", null,
                QuestStatus.valueOf((String) instance.get("status")), null, null, null, objectives);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static QuestObjectiveFactory objectiveFactory() {
        QuestObjectiveFactory objectives = new QuestObjectiveFactory();
        objectives.register(BlockBreakObjectiveConfig.class, BlockBreakObjective.class, mock(QuestObjectiveHandler.class));
        objectives.register(TravelObjectiveConfig.class, TravelObjective.class, mock(QuestObjectiveHandler.class));
        objectives.register(ExecuteCommandObjectiveConfig.class, ExecuteCommandObjective.class,
                mock(QuestObjectiveHandler.class));
        return objectives;
    }

    /** The plugin's own path: the document reader, then the mappers the registry uses. */
    private static QuestGroupConfig load(Map<String, Object> document, String lang, QuestObjectiveFactory objectives) {
        Map<String, Object> doc = new LinkedHashMap<>(document);
        String key = (String) doc.remove("id");
        return new QuestGroupMapper(new QuestConfigMapper(objectives))
                .toEntity(QuestGroupDocumentReader.read(key, doc, lang));
    }

    /** Rows as the WebSocket carries them: JSON numbers and lists, as the vector holds them. */
    private static Object json(Object rows) throws Exception {
        return JSON.readValue(JSON.writeValueAsString(rows), Object.class);
    }

    /** {@code actual} reduced to the shape of {@code expected}: the fields the vector pins. */
    @SuppressWarnings("unchecked")
    private static Object pick(Object actual, Object expected) {
        if (expected instanceof List<?> exp && actual instanceof List<?> act) {
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < act.size(); i++) {
                out.add(pick(act.get(i), exp.get(Math.min(i, exp.size() - 1))));
            }
            return out;
        }
        if (expected instanceof Map<?, ?> exp && actual instanceof Map<?, ?> act) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Object k : exp.keySet()) out.put((String) k, pick(act.get(k), exp.get(k)));
            return out;
        }
        return actual;
    }
}

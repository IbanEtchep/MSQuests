package com.github.ibanetchep.msquests.bukkit.artisan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentReader;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjectiveConfig;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TRANSLATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * The vectors of {@code testdata/sources/}: a quest group as the editor writes it, and the
 * definition fields its rows must carry. The editor computes the same rows from the draft
 * ({@code editor/src/sourceRows.test.ts} reads these files), so a field that changes here and
 * not there fails one of the two suites.
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
            QuestGroupConfig group = load(document, (String) vector.get("lang"));

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
        }
        return tests;
    }

    /** The plugin's own path: the document reader, then the mappers the registry uses. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static QuestGroupConfig load(Map<String, Object> document, String lang) {
        QuestObjectiveFactory objectives = new QuestObjectiveFactory();
        objectives.register(BlockBreakObjectiveConfig.class, BlockBreakObjective.class, mock(QuestObjectiveHandler.class));
        objectives.register(TravelObjectiveConfig.class, TravelObjective.class, mock(QuestObjectiveHandler.class));
        objectives.register(ExecuteCommandObjectiveConfig.class, ExecuteCommandObjective.class,
                mock(QuestObjectiveHandler.class));
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

package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentRepository;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import net.artisanmc.modules.api.template.TemplateDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExampleTemplateTest {

    @TempDir
    Path project;

    private static TemplateDeclaration template() {
        return TemplateDeclaration.fromResources(ExampleTemplateTest.class.getClassLoader(),
                        MsQuestsArtisanModule.TEMPLATE_ROOT, MsQuestsArtisanModule.TEMPLATE,
                        MsQuestsArtisanModule.TEMPLATE_VERSION, MsQuestsArtisanModule.TEMPLATE_FILES)
                .seed(EditorProject.PROJECT_ID)
                .build();
    }

    @Test
    void theJarShipsEveryFileOfTheTemplate() {
        TemplateDeclaration template = template();

        assertEquals(MsQuestsArtisanModule.TEMPLATE_FILES, template.getPaths());
        assertEquals("quests", template.getSeedProject());
    }

    @Test
    void theExampleGroupLoadsInTheProjectLanguage() throws Exception {
        TemplateDeclaration template = template();
        Files.writeString(project.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: fr\nlanguages: [fr]\n");
        for (String path : template.getPaths()) {
            Path target = project.resolve(path);
            Files.createDirectories(target.getParent());
            Files.write(target, template.getFile(path));
        }

        QuestConfigLoad load = new QuestGroupDocumentRepository(project).loadAllGroups().join();

        assertEquals(List.of(), load.failures());
        QuestGroupConfigDTO group = load.groups().get("example_daily");
        assertEquals("Quêtes journalières", group.name());
        assertEquals(3, group.maxActive());
        assertEquals("RANDOM", group.distribution().strategy());
        assertEquals(3, group.distribution().amount());
        assertEquals(List.of("miner", "hunter", "angler"), group.quests().stream().map(QuestConfigDTO::key).toList());
        assertEquals(Map.of("give_item", Map.of("material", "IRON_INGOT", "count", 8)),
                group.quests().getFirst().rewards().getFirst());
    }
}

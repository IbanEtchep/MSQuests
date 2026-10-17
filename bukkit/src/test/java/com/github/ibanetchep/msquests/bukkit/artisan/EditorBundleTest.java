package com.github.ibanetchep.msquests.bukkit.artisan;

import net.artisanmc.modules.api.editor.EditorBundleDeclaration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EditorBundleTest {

    /** The jar carries the view {@code editor/} builds, and Artisan accepts it as declared. */
    @Test
    void theJarCarriesTheEditorView() {
        EditorBundleDeclaration bundle = EditorBundleDeclaration.fromResource(
                MsQuestsArtisanModule.class.getClassLoader(),
                MsQuestsArtisanModule.EDITOR_BUNDLE,
                MsQuestsArtisanModule.EDITOR_SDK);

        String script = new String(bundle.getScript(), StandardCharsets.UTF_8);
        assertTrue(script.contains("quest_group"), "the bundle declares the quest_group kind");
        assertTrue(script.contains("\"msquests\""), "the bundle is the msquests module");
        assertTrue(script.contains("sdk:\"" + MsQuestsArtisanModule.EDITOR_SDK + "\"")
                        || script.contains("sdk: \"" + MsQuestsArtisanModule.EDITOR_SDK + "\""),
                "EDITOR_SDK is the SDK version the bundle was built against");
    }
}

package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Translator {

    private final File langFolder;
    private final String language;
    private final Function<String, InputStream> defaultLangProvider;
    private final Logger logger;
    private YamlDocument messages;

    private static Translator INSTANCE;

    public Translator(File langFolder,
                      String language,
                      Function<String, InputStream> defaultLangProvider,
                      Logger logger) {
        this.langFolder = langFolder;
        this.language = language;
        this.defaultLangProvider = defaultLangProvider;
        this.logger = logger;
        INSTANCE = this;
    }

    public static String raw(String key) {
        return INSTANCE.getRaw(key);
    }

    public static String raw(Translatable translatable) {
        return INSTANCE.getRaw(translatable);
    }

    public static String raw(Translatable translatable, Map<String, String> placeholders) {
        return INSTANCE.getRaw(translatable, placeholders);
    }

    public void load() {
        if (!langFolder.exists() && langFolder.mkdirs()) {
            logger.info("Created lang folder.");
        }

        try {
            messages = loadLocale(language);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading language files!", e);
        }
    }

    private YamlDocument loadLocale(String locale) throws IOException {
        File langFile = new File(langFolder, locale + ".yml");
        InputStream defaultLangFile = defaultLangProvider.apply(locale);

        YamlDocument yamlDocument;

        DumperSettings dumperSettings = DumperSettings.builder()
                .setScalarFormatter((tag, value, role, def) -> {
                    if(tag != Tag.STR) return def;

                    if(value.contains("\n")) return ScalarStyle.LITERAL;

                    return def;
                })
                .build();

        UpdaterSettings updaterSettings = UpdaterSettings.builder().setVersioning(new BasicVersioning("file_version")).build();

        if (defaultLangFile != null) {
            yamlDocument = YamlDocument.create(
                    langFile,
                    defaultLangFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    dumperSettings,
                    updaterSettings
            );
        } else {
            yamlDocument = YamlDocument.create(
                    langFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    dumperSettings,
                    updaterSettings
            );
        }

        yamlDocument.update();

        // Ajout des clés manquantes
        for (TranslationKey translationKey : TranslationKey.values()) {
            String key = translationKey.getTranslationKey();
            if (!yamlDocument.contains(key)) {
                yamlDocument.set(key, "__" + key.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + key + " in " + locale + ".yml");
            }
        }

        for (QuestStatus status : QuestStatus.values()) {
            String key = status.getTranslationKey();
            if (!yamlDocument.contains(key)) {
                yamlDocument.set(key, "__" + key.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + key + " in " + locale + ".yml");
            }

            String symbolKey = status.getSymbolTranslationKey();
            if (!yamlDocument.contains(symbolKey)) {
                yamlDocument.set(key, "__" + symbolKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + symbolKey + " in " + locale + ".yml");
            }
        }

        for (QuestObjectiveStatus status : QuestObjectiveStatus.values()) {
            String key = status.getTranslationKey();
            if (!yamlDocument.contains(key)) {
                yamlDocument.set(key, "__" + key.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + key + " in " + locale + ".yml");
            }

            String symbolKey = status.getSymbolTranslationKey();
            if (!yamlDocument.contains(symbolKey)) {
                yamlDocument.set(key, "__" + symbolKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + symbolKey + " in " + locale + ".yml");
            }
        }

        yamlDocument.save();
        return yamlDocument;
    }


    /**
     * Returns the raw message from the YAML file
     */
    private String getRaw(Translatable translatable) {
        Map<String, String> placeholders = null;

        if(translatable instanceof PlaceholderProvider placeholderProvider) {
            placeholders = placeholderProvider.getPlaceholders();
        }

        return getRaw(translatable, placeholders);
    }

    private String getRaw(Translatable translatable, Map<String, String> placeholders) {
        String raw = getRaw(translatable.getTranslationKey());

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return raw;
    }

    private String getRaw(String key) {
        return messages.getString(key, "Missing translation: " + key);
    }
}
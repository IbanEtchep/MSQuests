package com.github.ibanetchep.msquests.core.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

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

    public static Component t(Translatable translatable) {
        return INSTANCE.component(translatable);
    }

    public static Component t(Translatable translatable, Map<String, String> placeholders) {
        return INSTANCE.component(translatable, placeholders);
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

        if (defaultLangFile != null) {
            yamlDocument = YamlDocument.create(
                    langFile,
                    defaultLangFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        } else {
            yamlDocument = YamlDocument.create(
                    langFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        }

        // Ajout des clés manquantes
        for (TranslationKey translationKey : TranslationKey.values()) {
            String key = translationKey.getTranslationKey();
            if (!yamlDocument.contains(key)) {
                yamlDocument.set(key, "__" + key.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + key + " in " + locale + ".yml");
            }
        }

        yamlDocument.save();
        return yamlDocument;
    }

    /**
     * Returns the message Adventure Component without placeholders
     */
    public Component component(Translatable translatable) {
        if(translatable instanceof PlaceholderProvider placeholderProvider) {
            return component(translatable, placeholderProvider.getPlaceholders());
        }

        String raw = getRaw(translatable.getTranslationKey());

        return MINI_MESSAGE.deserialize(raw);
    }

    /**
     * Returns the message Adventure Component with placeholders
     */
    public Component component(Translatable translatable, Map<String, String> placeholders) {
        String message = getRaw(translatable.getTranslationKey());

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Returns the message as a plain text string
     */
    public String plainText(Translatable translatable) {
        return PLAIN_SERIALIZER.serialize(component(translatable));
    }

    public String plainText(Translatable translatable, Map<String, String> placeholders) {
        return PLAIN_SERIALIZER.serialize(component(translatable, placeholders));
    }

    /**
     * Returns the message as a legacy string
     */
    public String toString(Translatable translatable) {
        return LEGACY_SERIALIZER.serialize(component(translatable));
    }

    public String toString(Translatable translatable, Map<String, String> placeholders) {
        return LEGACY_SERIALIZER.serialize(component(translatable, placeholders));
    }

    /**
     * Returns the raw message from the YAML file
     */
    private String getRaw(String key) {
        return messages.getString(key, "Missing translation: " + key);
    }

    /**
     * Registers a key if it is missing in the YAML file
     */
    public void registerKeyIfMissing(String key) {
        if (!messages.contains(key)) {
            try {
                messages.set(key, "__" + key.toLowerCase().replace(".", "_"));
                messages.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
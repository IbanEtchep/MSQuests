package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.artisanmc.modules.api.translation.MessageBundle;
import net.artisanmc.modules.api.translation.MessageBundleDeclaration;
import net.artisanmc.modules.api.translation.TranslationsApi;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MSQuests' messages. The lang file is still read and kept up to date here (BoostedYAML,
 * {@code file_version}, custom keys kept); its keys are served through Artisan's translations
 * as the bundle {@value #NAMESPACE}, in the configured {@code language}.
 */
public class BukkitTranslator implements Translator {

    /** The bundle of these messages in Artisan's translations. */
    public static final String NAMESPACE = "msquests";

    private final File langFolder;
    private final String language;
    private final Function<String, InputStream> defaultLangProvider;
    private final Logger logger;
    private final @Nullable Plugin owner;
    private final @Nullable TranslationsApi translations;
    private YamlDocument messages;
    private @Nullable MessageBundle bundle;

    private static BukkitTranslator INSTANCE;

    /** Without Artisan (tests): the keys are read from the lang file directly. */
    public BukkitTranslator(File langFolder,
                            String language,
                            Function<String, InputStream> defaultLangProvider,
                            Logger logger) {
        this(langFolder, language, defaultLangProvider, logger, null, null);
    }

    public BukkitTranslator(File langFolder,
                            String language,
                            Function<String, InputStream> defaultLangProvider,
                            Logger logger,
                            @Nullable Plugin owner,
                            @Nullable TranslationsApi translations) {
        this.langFolder = langFolder;
        this.language = language;
        this.defaultLangProvider = defaultLangProvider;
        this.logger = logger;
        this.owner = owner;
        this.translations = translations;
        INSTANCE = this;
    }

    public static String raw(String key) {
        return INSTANCE.getRaw(key);
    }

    public static String raw(Translatable translatable) {
        return INSTANCE.getRaw(translatable);
    }

    public static Translator getInstance() {
        return INSTANCE;
    }

    public void load() {
        if (!langFolder.exists() && langFolder.mkdirs()) {
            logger.info("Created lang folder.");
        }

        try {
            messages = loadLocale(language);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading language files!", e);
            return;
        }

        if (owner != null && translations != null) {
            // A reload builds a new translator: its bundle replaces the previous one.
            translations.unregister(NAMESPACE);
            Map<String, Map<String, String>> tables = Map.of(language, flatten(messages));
            bundle = translations.register(MessageBundleDeclaration.of(owner, NAMESPACE, () -> tables)
                    .withDefaultLang(language));
        }
    }

    /** Every text of the file by dotted key. */
    private static Map<String, String> flatten(YamlDocument document) {
        Map<String, String> table = new LinkedHashMap<>();
        for (String route : document.getRoutesAsStrings(true)) {
            if (document.isSection(route)) continue;
            Object value = document.get(route);
            if (value != null) table.put(route, value.toString());
        }
        return table;
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

        // keepAll: a server's lang file may hold keys the bundled defaults do not know (custom
        // message_key values used by its quest files); the updater must never delete them.
        UpdaterSettings updaterSettings = UpdaterSettings.builder()
                .setVersioning(new BasicVersioning("file_version"))
                .setKeepAll(true)
                .build();

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

            String prefixKey = status.getPrefixTranslationKey();
            if (!yamlDocument.contains(prefixKey)) {
                yamlDocument.set(prefixKey, "__" + prefixKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + prefixKey + " in " + locale + ".yml");
            }

            String suffixKey = status.getSuffixTranslationKey();
            if (!yamlDocument.contains(suffixKey)) {
                yamlDocument.set(suffixKey, "__" + suffixKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + suffixKey + " in " + locale + ".yml");
            }
        }

        for (QuestObjectiveStatus status : QuestObjectiveStatus.values()) {
            String key = status.getTranslationKey();
            if (!yamlDocument.contains(key)) {
                yamlDocument.set(key, "__" + key.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + key + " in " + locale + ".yml");
            }

            String prefixKey = status.getPrefixTranslationKey();
            if (!yamlDocument.contains(prefixKey)) {
                yamlDocument.set(prefixKey, "__" + prefixKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + prefixKey + " in " + locale + ".yml");
            }

            String suffixKey = status.getSuffixTranslationKey();
            if (!yamlDocument.contains(suffixKey)) {
                yamlDocument.set(suffixKey, "__" + suffixKey.toLowerCase().replace(".", "_"));
                logger.warning("Missing translation for key: " + suffixKey + " in " + locale + ".yml");
            }
        }

        yamlDocument.save();
        return yamlDocument;
    }


    /**
     * Returns the raw message from the YAML file
     */
    @Override
    public String getRaw(Translatable translatable) {
        Map<String, String> placeholders = null;

        if(translatable instanceof PlaceholderProvider placeholderProvider) {
            placeholders = placeholderProvider.getPlaceholders(this);
        }

        String raw = getRaw(translatable.getTranslationKey());

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return raw;
    }

    @Override
    public String getRaw(String key) {
        String text = bundle != null
                ? bundle.find(bundle.getDefaultLang(), key)
                : messages != null ? messages.getString(key) : null;
        return text != null ? text : "Missing translation: " + key;
    }
}
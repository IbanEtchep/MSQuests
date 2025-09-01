package com.github.ibanetchep.msquests.bukkit.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public enum Lang {

    CONFIG_RELOADED("messages.config.reloaded"),
    QUEST_STARTED("messages.quest.started"),
    QUEST_COULD_NOT_START("messages.quest.could_not_start");


    private final String key;
    private static YamlDocument messages;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    Lang(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static void setMessages(YamlDocument messagesFile) {
        messages = messagesFile;
    }

    private String getRaw() {
        return messages.getString(key, "Missing translation: " + key);
    }

    public Component component() {
        return MINI_MESSAGE.deserialize(getRaw());
    }

    public Component component(String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be paired (key, value)");
        }

        String message = getRaw();
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i + 1];
            message = message.replace("%" + placeholders[i] + "%", placeholder);
        }

        return MINI_MESSAGE.deserialize(message);
    }

    public String plainText() {
        return PLAIN_SERIALIZER.serialize(MINI_MESSAGE.deserialize(getRaw()));
    }

    public String plainText(String... placeholders) {
        return PLAIN_SERIALIZER.serialize(component(placeholders));
    }

    public String toString() {
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(getRaw()));
    }

    public String toString(String... placeholders) {
        return LEGACY_SERIALIZER.serialize(component(placeholders));
    }
}
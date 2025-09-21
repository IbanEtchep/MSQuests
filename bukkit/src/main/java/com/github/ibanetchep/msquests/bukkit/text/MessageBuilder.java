package com.github.ibanetchep.msquests.bukkit.text;

import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderEngine;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A builder class for creating and formatting messages with placeholders and lists.
 * Supports multiple output formats including MiniMessage components, legacy strings, and plain text.
 */
public class MessageBuilder {

    private final String message;
    private final List<Object> placeholderResolvers = new ArrayList<>();
    private final Map<String, String> placeholders = new HashMap<>();

    /**
     * Creates a new MessageBuilder with the specified message.
     *
     * @param message The message content to build upon
     */
    private MessageBuilder(String message) {
        this.message = message;
    }

    /**
     * Creates a MessageBuilder from a Translatable message.
     *
     * @param translatable The translatable message to build from
     * @return A new MessageBuilder instance with the translated message
     */
    public static MessageBuilder translatable(Translatable translatable) {
        return new MessageBuilder(Translator.raw(translatable));
    }

    /**
     * Creates a MessageBuilder from a Translatable message.
     *
     * @param key The translation key to build from
     * @return A new MessageBuilder instance with the translated message
     */
    public static MessageBuilder translatable(String key) {
        return new MessageBuilder(Translator.raw(key));
    }

    /**
     * Creates a MessageBuilder from a pre-translated raw string.
     *
     * @param rawMessage The raw message string to build from
     * @return A new MessageBuilder instance with the provided message
     */
    public static MessageBuilder raw(String rawMessage) {
        return new MessageBuilder(rawMessage);
    }

    /**
     * Applies a resolver to the message.
     *
     * @param obj The context object containing data for placeholders
     * @return This builder for method chaining
     */
    public MessageBuilder applyPlaceholderResolver(@NotNull Object obj) {
        placeholderResolvers.add(obj);
        return this;
    }

    /**
     * Adds a simple placeholder replacement.
     *
     * @param key The placeholder key (without % symbols)
     * @param value The value to replace the placeholder with
     * @return This builder for method chaining
     */
    public MessageBuilder placeholder(String key, String value) {
        placeholders.put(key, value);
        return this;
    }

    public <T> MessageBuilder listPlaceholder(String placeholderKey, List<T> items, Function<T, String> renderer) {
        StringBuilder sb = new StringBuilder();

        for (T item : items) {
            sb.append(renderer.apply(item));
        }

        placeholders.put(placeholderKey, sb.toString());

        return this;
    }

    /**
     * Renders the final message with all placeholders and lists resolved.
     *
     * @return The fully resolved message as a raw string
     */
    public String toStringRaw() {
        String result = message;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        for (Object resolver : placeholderResolvers) {
            result = PlaceholderEngine.getInstance().apply(result, resolver);
        }

        return result;
    }

    /**
     * Renders the message as a MiniMessage Component.
     *
     * @return The message as an Adventure Component
     */
    public Component toComponent() {
        return MiniMessage.miniMessage().deserialize(toStringRaw());
    }

    /**
     * Renders the message as a legacy string with section color codes.
     *
     * @return The message in legacy format with § color codes
     */
    public String toLegacyString() {
        return LegacyComponentSerializer.legacySection().serialize(toComponent());
    }

    /**
     * Renders the message as plain text with no formatting.
     *
     * @return The message as plain text with all formatting removed
     */
    public String toPlainText() {
        return PlainTextComponentSerializer.plainText().serialize(toComponent());
    }
}

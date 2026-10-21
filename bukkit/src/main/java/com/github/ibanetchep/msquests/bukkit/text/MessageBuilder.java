package com.github.ibanetchep.msquests.bukkit.text;

import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderEngine;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import net.artisanmc.modules.api.text.TextApi;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A builder class for creating and formatting messages with placeholders and lists.
 * Messages are MiniMessage; Artisan's text API parses and sends them, so no Adventure type is
 * touched here and the plugin runs on Spigot as well as Paper.
 */
public class MessageBuilder {

    private static volatile TextApi text = TextApi.NOOP;

    private final String message;
    private final List<PlaceholderProvider> placeholderProviders = new ArrayList<>();
    private final Map<String, String> placeholders = new HashMap<>();

    /**
     * Creates a new MessageBuilder with the specified message.
     *
     * @param message The message content to build upon
     */
    private MessageBuilder(String message) {
        this.message = message;
    }

    /** Called at enable with {@code api.getText()}. */
    public static void useText(TextApi api) {
        text = api;
    }

    /** Parses {@code miniMessage} and sends it to {@code to}. */
    public static void send(CommandSender to, String miniMessage) {
        text.send(to, miniMessage);
    }

    /** {@code miniMessage} as a {@code §}-coded string, for Bukkit's String methods. */
    public static String legacy(String miniMessage) {
        return text.legacy(miniMessage);
    }

    /**
     * Creates a MessageBuilder from a Translatable message.
     *
     * @param translatable The translatable message to build from
     * @return A new MessageBuilder instance with the translated message
     */
    public static MessageBuilder translatable(Translatable translatable) {
        return new MessageBuilder(BukkitTranslator.raw(translatable));
    }

    /**
     * Creates a MessageBuilder from a Translatable message.
     *
     * @param key The translation key to build from
     * @return A new MessageBuilder instance with the translated message
     */
    public static MessageBuilder translatable(String key) {
        return new MessageBuilder(BukkitTranslator.raw(key));
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
    public MessageBuilder applyPlaceholderResolver(@NotNull PlaceholderProvider obj) {
        placeholderProviders.add(obj);
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

        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            result = PlaceholderEngine.getInstance().apply(result, placeholderProvider);
        }

        return result;
    }

    /** Sends the rendered message to {@code to}. */
    public void send(CommandSender to) {
        text.send(to, toStringRaw());
    }

    /**
     * Renders the message as a legacy string with section color codes.
     *
     * @return The message in legacy format with § color codes
     */
    public String toLegacyString() {
        return text.legacy(toStringRaw());
    }

    /**
     * Renders the message as plain text with no formatting.
     *
     * @return The message as plain text with all formatting removed
     */
    public String toPlainText() {
        return text.plain(toStringRaw());
    }
}

package com.github.ibanetchep.msquests.bukkit.placeholderapi;

import com.github.ibanetchep.msquests.bukkit.placeholder.QuestPlaceholders;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code %msquests_<key>%}: the placeholders' name before ArtisanQuests, kept as an alias of
 * {@code %quests_<key>%} for one version (C7) so scoreboards and menus keep working.
 */
public class QuestsPlaceholderExpansion extends PlaceholderExpansion {

    private final QuestPlaceholders placeholders;

    public QuestsPlaceholderExpansion(QuestPlaceholders placeholders) {
        this.placeholders = placeholders;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "msquests";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Iban";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Nullable
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String value = placeholders.resolve(player != null ? player.getUniqueId() : null, params);
        return value != null ? value : "";
    }
}

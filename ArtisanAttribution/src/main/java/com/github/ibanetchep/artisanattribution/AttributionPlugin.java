package com.github.ibanetchep.artisanattribution;

import com.github.ibanetchep.artisanattribution.artisan.AttributionModule;
import com.github.ibanetchep.artisanattribution.command.AttributionCommand;
import com.github.ibanetchep.artisanattribution.config.AttributionConfig;
import com.github.ibanetchep.artisanattribution.config.ConfigException;
import com.github.ibanetchep.artisanattribution.config.ConfigReader;
import com.github.ibanetchep.artisanattribution.listener.PlayerConnectionListener;
import com.github.ibanetchep.artisanattribution.storage.AttributionRepository;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.translation.MessageBundleDeclaration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Entry point. Artisan is required: its menus show the question, its database keeps the answers,
 * its translations serve the messages. Without a compatible Artisan the plugin disables itself
 * and says why.
 */
public final class AttributionPlugin extends JavaPlugin {

    /**
     * Lowest Artisan API this plugin compiles against: {@code TemplateRegistry} (2.6.0),
     * {@code TranslationsApi} (2.5.0), {@code ModuleActionsApi.run} with a structured chain (2.2.0).
     */
    public static final String MIN_API_VERSION = "2.6.0";

    private @Nullable ArtisanAPI api;
    private @Nullable AttributionService service;
    private @Nullable Messages messages;

    @Override
    public void onEnable() {
        api = resolveArtisan();
        if (api == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        AttributionConfig config;
        try {
            config = readConfig();
        } catch (ConfigException e) {
            getLogger().severe("config.yml cannot be used, ArtisanAttribution is disabled: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        AttributionRepository repository;
        try {
            repository = new AttributionRepository(api.getDatabase().dataSource(this));
            repository.migrate();
        } catch (SQLException | IllegalStateException e) {
            getLogger().log(Level.SEVERE, "The database is not available, ArtisanAttribution is disabled. "
                    + "Check plugins/Artisan/environment.yml.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        messages = new Messages(api.getTranslations().register(
                MessageBundleDeclaration.files(this, Messages.NAMESPACE, "en", "fr")));
        service = new AttributionService(api, api.getScheduler().forPlugin(this), repository, messages,
                getLogger(), config);
        try {
            service.refreshCounts();
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Could not count the answers; the counts start at 0", e);
        }

        api.getModules().register(new AttributionModule(service, getLogger()));
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(service), this);

        AttributionCommand command = new AttributionCommand(this, service, messages);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register("attribution",
                        "Tell how you found the server; admins: open, stats, reset, reload",
                        List.of(), command));

        loadOnlinePlayers(service);
        getLogger().info("Enabled with " + config.sources().size() + " sources, menu " + config.menu()
                + " (Artisan API " + api.getApiVersion() + ").");
    }

    @Override
    public void onDisable() {
        if (api != null && service != null) {
            api.getModules().unregister(AttributionModule.ID);
        }
        service = null;
        messages = null;
        api = null;
    }

    /** Re-reads config.yml and the lang files; on a config error the previous settings stay. */
    public void reloadAttribution(CommandSender sender) {
        if (service == null || messages == null) return;
        try {
            AttributionConfig config = readConfig();
            service.applyConfig(config);
            messages.reload();
            messages.send(sender, "command.reloaded", Map.of("sources", config.sources().size()));
        } catch (ConfigException e) {
            getLogger().warning("config.yml was not reloaded: " + e.getMessage());
            messages.send(sender, "command.reload_failed", Map.of("error", e.getMessage()));
        }
    }

    private AttributionConfig readConfig() throws ConfigException {
        File file = new File(getDataFolder(), "config.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new ConfigException("cannot read " + file.getPath() + ": " + e.getMessage(), e);
        }
        List<String> warnings = new ArrayList<>();
        AttributionConfig config = ConfigReader.read(yaml, AttributionPlugin::isItem, warnings);
        warnings.forEach(warning -> getLogger().warning("config.yml " + warning));
        return config;
    }

    private static boolean isItem(String name) {
        Material material = Material.matchMaterial(name);
        return material != null && material.isItem() && !material.isAir();
    }

    /** After a reload of the plugin, players already online get their answer loaded (never asked). */
    private void loadOnlinePlayers(AttributionService service) {
        List<UUID> online = getServer().getOnlinePlayers().stream().map(Player::getUniqueId).toList();
        if (online.isEmpty()) return;
        api.getScheduler().forPlugin(this).runAsync(() -> online.forEach(service::load));
    }

    private @Nullable ArtisanAPI resolveArtisan() {
        try {
            ArtisanAPI found = getServer().getServicesManager().load(ArtisanAPI.class);
            if (found == null) {
                getLogger().severe("Artisan is required but its API is not available. "
                        + "Install Artisan (API " + MIN_API_VERSION + "+) next to ArtisanAttribution.");
                return null;
            }
            if (!found.isCompatibleWith(MIN_API_VERSION)) {
                getLogger().severe("Artisan API " + found.getApiVersion() + " is too old: ArtisanAttribution needs "
                        + MIN_API_VERSION + "+ (same major). Update Artisan.");
                return null;
            }
            return found;
        } catch (LinkageError e) {
            getLogger().log(Level.SEVERE, "Installed Artisan is incompatible: ArtisanAttribution needs API "
                    + MIN_API_VERSION + "+.", e);
            return null;
        }
    }
}

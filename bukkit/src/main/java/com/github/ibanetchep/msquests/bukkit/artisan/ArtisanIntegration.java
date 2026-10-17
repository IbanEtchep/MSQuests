package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import net.artisanmc.modules.api.ArtisanAPI;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Artisan integration. Artisan is a required dependency ({@code paper-plugin.yml}): the plugin
 * resolves its API first thing in {@code onEnable} and refuses to start without a compatible one,
 * then registers {@link MsQuestsArtisanModule} once its services are wired.
 */
public final class ArtisanIntegration {

    /**
     * Lowest Artisan API this plugin compiles against: {@code ConditionsApi} and the structured
     * {@code ModuleActionsApi.run} (2.2.0), {@code DocumentKindRegistry} (2.1.0) and
     * the split {@code net.artisanmc.modules.api.*} packages (2.0.0).
     */
    public static final String MIN_API_VERSION = "2.2.0";

    private final BukkitQuestsPlugin plugin;
    private final ArtisanAPI api;
    private @Nullable MsQuestsArtisanModule module;

    private ArtisanIntegration(BukkitQuestsPlugin plugin, ArtisanAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    /**
     * The integration bound to the running Artisan, or null after logging why MSQuests cannot
     * start: Artisan missing, its API not registered, or older than {@link #MIN_API_VERSION}.
     */
    public static @Nullable ArtisanIntegration resolve(BukkitQuestsPlugin plugin) {
        try {
            ArtisanAPI api = plugin.getServer().getServicesManager().load(ArtisanAPI.class);
            if (api == null) {
                plugin.getLogger().severe("Artisan is required but its API is not available. "
                        + "Install Artisan (API " + MIN_API_VERSION + "+) next to MSQuests.");
                return null;
            }
            if (!api.isCompatibleWith(MIN_API_VERSION)) {
                plugin.getLogger().severe("Artisan API " + api.getApiVersion() + " is too old: MSQuests needs "
                        + MIN_API_VERSION + "+ (same major). Update Artisan.");
                return null;
            }
            return new ArtisanIntegration(plugin, api);
        } catch (LinkageError e) {
            // An Artisan 1.x jar: the 2.x packages this plugin links against do not exist there.
            plugin.getLogger().log(Level.SEVERE, "Installed Artisan is incompatible: MSQuests needs API "
                    + MIN_API_VERSION + "+.", e);
            return null;
        }
    }

    /** Registers the module: data sources, commands, actions, the quest_group kind and the editor project. */
    public void register() {
        MsQuestsArtisanModule created = new MsQuestsArtisanModule(plugin);
        api.getModules().register(created);
        this.module = created;
        plugin.getLogger().info("Artisan module registered (API " + api.getApiVersion() + ").");
    }

    public ArtisanAPI getApi() {
        return api;
    }

    /** Tells Artisan menus to re-fetch the quest data sources. */
    public void signalDataReload() {
        if (module != null) {
            module.signalDataReload();
        }
    }
}

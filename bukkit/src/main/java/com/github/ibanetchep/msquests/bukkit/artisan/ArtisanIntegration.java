package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import net.artisanmc.modules.api.ArtisanAPI;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Optional Artisan integration: registers {@link MsQuestsArtisanModule} when the Artisan
 * plugin is installed, does nothing otherwise. Artisan is a soft dependency — MSQuests
 * runs unchanged without it.
 *
 * <p>Everything here is guarded so that a missing or incompatible Artisan never prevents
 * MSQuests from enabling: the integration is a bonus surface, not a requirement.
 */
public final class ArtisanIntegration {

    /** Core version introducing {@code DataSourceDeclaration.stability} / {@code keyField}. */
    private static final String EXPECTED_CORE_VERSION = "1.4.0";

    private final BukkitQuestsPlugin plugin;
    private @Nullable MsQuestsArtisanModule module;

    public ArtisanIntegration(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (plugin.getServer().getPluginManager().getPlugin("Artisan") == null) {
            return;
        }
        try {
            RegisteredServiceProvider<ArtisanAPI> registration =
                    plugin.getServer().getServicesManager().getRegistration(ArtisanAPI.class);
            if (registration == null) {
                plugin.getLogger().warning("Artisan is installed but its API is not available yet — integration skipped.");
                return;
            }
            ArtisanAPI api = registration.getProvider();
            if (!api.isCompatibleWith(EXPECTED_CORE_VERSION)) {
                // Informational only: `coreVersion` is Artisan's plugin.yml version, which
                // does not track the API surface (it still reads 1.0.0 while the API is at
                // 1.6). Gating on it would disable the integration on up-to-date servers.
                // A genuinely too-old API fails at the catch below instead, loudly.
                plugin.getLogger().info("Artisan reports core version " + api.getCoreVersion()
                        + " (expected " + EXPECTED_CORE_VERSION + "+) — registering anyway.");
            }
            MsQuestsArtisanModule created = new MsQuestsArtisanModule(plugin);
            api.getModules().register(created);
            this.module = created;
            plugin.getLogger().info("Artisan integration registered (msquests:groups, msquests:quests, msquests:active).");
        } catch (Throwable e) {
            // NoClassDefFoundError included: a partially installed Artisan must not take MSQuests down.
            plugin.getLogger().log(Level.WARNING, "Failed to register the Artisan integration", e);
        }
    }

    /** No-op when Artisan is absent. */
    public void signalDataReload() {
        if (module != null) {
            module.signalDataReload();
        }
    }
}

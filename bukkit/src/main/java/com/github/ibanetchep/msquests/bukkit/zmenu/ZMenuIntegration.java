package com.github.ibanetchep.msquests.bukkit.zmenu;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.zmenu.button.QuestGroupPaginationLoader;
import fr.maxlego08.menu.api.ButtonManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ZMenuIntegration {

    private final BukkitQuestsPlugin plugin;

    public ZMenuIntegration(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ButtonManager buttonManager = getProvider(ButtonManager.class);
        buttonManager.register(new QuestGroupPaginationLoader(plugin));
        plugin.getLogger().info("ZMenu integration registered.");
    }

    private <T> T getProvider(Class<T> classz) {
        RegisteredServiceProvider<T> provider = plugin.getServer().getServicesManager().getRegistration(classz);
        if (provider == null) {
            throw new IllegalStateException("Unable to retrieve zMenu provider: " + classz.getName());
        }
        return provider.getProvider();
    }
}

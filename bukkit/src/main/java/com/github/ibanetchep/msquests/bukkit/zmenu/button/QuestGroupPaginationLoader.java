package com.github.ibanetchep.msquests.bukkit.zmenu.button;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class QuestGroupPaginationLoader extends ButtonLoader {

    private final BukkitQuestsPlugin plugin;

    public QuestGroupPaginationLoader(BukkitQuestsPlugin plugin) {
        super(plugin, "MSQUESTS_QUEST_GROUP");
        this.plugin = plugin;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        String groupKey = configuration.getString(path + "group", "");
        boolean assignedOnly = configuration.getBoolean(path + "assigned_only", false);
        List<String> actionsLore = configuration.getStringList(path + "actions-lore");
        Material availableMaterial = Material.valueOf(configuration.getString(path + "available_material", "GRAY_DYE"));
        Material activeMaterial = Material.valueOf(configuration.getString(path + "active_material", "LIGHT_BLUE_DYE"));
        Material completedMaterial = Material.valueOf(configuration.getString(path + "completed_material", "LIME_DYE"));
        return new QuestGroupPaginationButton(plugin, groupKey, assignedOnly, actionsLore, availableMaterial, activeMaterial, completedMaterial);
    }
}

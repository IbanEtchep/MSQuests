package com.github.ibanetchep.msquests.bukkit.zmenu.button;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestGroupPaginationLoader extends ButtonLoader {

    private final BukkitQuestsPlugin plugin;

    public QuestGroupPaginationLoader(BukkitQuestsPlugin plugin) {
        super(plugin, "MSQUESTS_QUEST_GROUP");
        this.plugin = plugin;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        String groupKey = configuration.getString(path + "group", "");
        return new QuestGroupPaginationButton(plugin, groupKey);
    }
}

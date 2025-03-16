package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;

public abstract class AbstractManager {

    protected final MSQuestsPlugin plugin;

    public AbstractManager(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }
}

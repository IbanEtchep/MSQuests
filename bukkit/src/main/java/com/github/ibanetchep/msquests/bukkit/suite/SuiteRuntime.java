package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.bukkit.document.ProjectFiles;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationsPort;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemMatcher;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * What the quests take from the suite's runtime, chosen once at {@code onEnable}: the Artisan
 * plugin when it runs ({@code ArtisanIntegration}: its engine, registries, menus and editor),
 * else MSQuests' own copy of the runtime ({@link LocalSuiteRuntime}: the same engine without
 * menus, registries or editor). The gameplay and the persistence never depend on the choice.
 */
public interface SuiteRuntime {

    /** Whether the Artisan plugin runs: menus, data sources, the editor and inter-module actions exist. */
    boolean hasArtisan();

    /** Runs the quests' chains (rewards, hooks). */
    ChainRunner chainRunner();

    /** Evaluates requirements and objective conditions. */
    ConditionChecker conditionChecker();

    /** Builds and compares the objectives' items. */
    ItemMatcher items();

    /** The projects the {@code quest_group} Documents are read from. */
    ProjectFiles projectFiles();

    /** Where MSQuests' messages are served from, null when the lang file is read directly. */
    @Nullable TranslationsPort translations();

    /** The project {@code quests} ({@code <projects>/quests}), where the migration writes. */
    File questsProject();

    /** Re-reads the projects from disk, after Documents were written outside the editor. */
    void reloadProjects();

    /** Tells the menus to re-fetch the quest data (nothing without Artisan). */
    void signalDataReload();

    /** Once the services are wired: registers the Artisan module (nothing without Artisan). */
    void register();

    void close();
}

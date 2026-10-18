package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.bukkit.artisan.QuestActions;
import com.github.ibanetchep.msquests.bukkit.document.ProjectFiles;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationsPort;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemMatcher;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import com.github.ibanetchep.msquests.suite.LocalRuntime;
import com.github.ibanetchep.msquests.suite.SuiteProjects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The suite's runtime without the Artisan plugin: MSQuests' own copy of Artisan's engine
 * ({@link LocalRuntime}, shaded and relocated in the {@code suite} module). Chains, conditions
 * and items work as with Artisan, the {@code quest_group} Documents are read from the same
 * projects folder; there are no menus, no editor, no example template and no actions of other
 * modules, and the messages are read from the lang file directly.
 */
public final class LocalSuiteRuntime implements SuiteRuntime {

    private final LocalRuntime runtime;
    private final ChainRunner chainRunner;
    private final ConditionChecker conditionChecker;
    private final ItemMatcher items;
    private final ProjectFiles projectFiles;

    private LocalSuiteRuntime(BukkitQuestsPlugin plugin, LocalRuntime runtime) {
        this.runtime = runtime;
        this.chainRunner = new MemberChainRunner(plugin, runtime::runChain);
        this.conditionChecker = new PlayerConditionChecker(new ConditionsPort() {
            @Override
            public boolean test(Player player, List<?> conditions, Map<String, ?> args) {
                return runtime.testConditions(player, conditions, args);
            }

            @Override
            public List<String> validate(List<?> conditions) {
                return runtime.validateConditions(conditions);
            }
        }, plugin.getLogger());
        this.items = new ItemMatcher() {
            @Override
            public boolean matches(Map<String, ?> spec, @Nullable ItemStack stack, Set<String> comparisons) {
                return runtime.matchesItem(spec, stack, comparisons);
            }

            @Override
            public @Nullable ItemStack build(Map<String, ?> spec) {
                return runtime.buildItem(spec);
            }
        };
        this.projectFiles = new ProjectFiles() {
            @Override
            public List<String> projectIds() {
                return runtime.projectIds().stream().sorted().toList();
            }

            @Override
            public List<String> list(String projectId, String dir) {
                return runtime.listFiles(projectId, dir);
            }

            @Override
            public @Nullable String read(String projectId, String path) {
                return runtime.readFile(projectId, path);
            }
        };
    }

    /**
     * Starts the local runtime: the projects root follows the same rule as the database
     * ({@link SuiteProjects#root}), the {@code quests:*} actions are registered in it.
     */
    public static LocalSuiteRuntime start(BukkitQuestsPlugin plugin) {
        plugin.getLogger().info("The Artisan plugin is not running: the quests run on MSQuests' own copy of its runtime"
                + " (no menus, no editor, no actions of other modules).");
        LocalRuntime runtime = new LocalRuntime(
                plugin,
                SuiteProjects.root(plugin),
                new QuestActions(plugin).declarations(),
                plugin.getGlobalConfig().language(),
                () -> plugin.langText("artisan.menus_unavailable", "This interface needs the Artisan plugin."),
                plugin.getLogger());
        return new LocalSuiteRuntime(plugin, runtime);
    }

    @Override
    public boolean hasArtisan() {
        return false;
    }

    @Override
    public ChainRunner chainRunner() {
        return chainRunner;
    }

    @Override
    public ConditionChecker conditionChecker() {
        return conditionChecker;
    }

    @Override
    public ItemMatcher items() {
        return items;
    }

    @Override
    public ProjectFiles projectFiles() {
        return projectFiles;
    }

    @Override
    public @Nullable TranslationsPort translations() {
        return null;
    }

    @Override
    public File questsProject() {
        return new File(runtime.getProjectsRoot(), EditorProject.PROJECT_ID);
    }

    @Override
    public void reloadProjects() {
        runtime.reloadProjects();
    }

    @Override
    public void signalDataReload() {
    }

    @Override
    public void register() {
    }

    @Override
    public void close() {
        runtime.close();
    }
}

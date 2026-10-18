package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.document.ProjectFiles;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationsPort;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemMatcher;
import com.github.ibanetchep.msquests.bukkit.suite.ConditionsPort;
import com.github.ibanetchep.msquests.bukkit.suite.MemberChainRunner;
import com.github.ibanetchep.msquests.bukkit.suite.PlayerConditionChecker;
import com.github.ibanetchep.msquests.bukkit.suite.SuiteRuntime;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.item.ItemComparison;
import net.artisanmc.modules.api.item.ItemStackApi;
import net.artisanmc.modules.api.translation.MessageBundle;
import net.artisanmc.modules.api.translation.MessageBundleDeclaration;
import net.artisanmc.modules.api.translation.TranslationsApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The suite's runtime when the Artisan plugin runs ({@link SuiteRuntime}): its engine runs the
 * chains, its registries take the module (data sources, commands, actions, the {@code quest_group}
 * kind, the example template, the editor view), its translations serve the messages. Artisan is an
 * optional dependency ({@code paper-plugin.yml}): {@link #resolve} answers null when it is absent,
 * not enabled or older than {@link #MIN_API_VERSION}, and the plugin then runs on its own copy of
 * the runtime ({@code LocalSuiteRuntime}).
 *
 * <p>This is the one class, with {@link MsQuestsArtisanModule} and {@link ArtisanActions}, that
 * names Artisan's API: without the Artisan plugin those classes do not exist in the server.
 */
public final class ArtisanIntegration implements SuiteRuntime {

    /**
     * Lowest Artisan API this plugin compiles against: the example template
     * ({@code TemplateRegistry}, 2.6.0), its messages served by {@code TranslationsApi}
     * (2.5.0), item specs built and compared by
     * {@code ItemStackApi.matches} (2.4.0), {@code ConditionsApi} and the structured
     * {@code ModuleActionsApi.run} (2.2.0), {@code DocumentKindRegistry} (2.1.0) and
     * the split {@code net.artisanmc.modules.api.*} packages (2.0.0).
     */
    public static final String MIN_API_VERSION = "2.6.0";

    private final BukkitQuestsPlugin plugin;
    private final ArtisanAPI api;
    private @Nullable MsQuestsArtisanModule module;

    private ArtisanIntegration(BukkitQuestsPlugin plugin, ArtisanAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    /**
     * The integration bound to the running Artisan, or null after logging why the quests run
     * without it: Artisan absent or not enabled (its API is not registered), older than
     * {@link #MIN_API_VERSION}, or a 1.x jar.
     */
    public static @Nullable ArtisanIntegration resolve(BukkitQuestsPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Artisan") == null) {
            return null; // not installed: nothing to say, the local runtime logs what it does
        }
        try {
            ArtisanAPI api = plugin.getServer().getServicesManager().load(ArtisanAPI.class);
            if (api == null) {
                plugin.getLogger().warning("Artisan is installed but its API is not available (did it fail to enable?):"
                        + " the quests run without it, with no menus and no editor, until the next restart.");
                return null;
            }
            if (!api.isCompatibleWith(MIN_API_VERSION)) {
                plugin.getLogger().warning("Artisan API " + api.getApiVersion() + " is too old: MSQuests needs "
                        + MIN_API_VERSION + "+ (same major). The quests run without Artisan until it is updated.");
                return null;
            }
            return new ArtisanIntegration(plugin, api);
        } catch (LinkageError e) {
            // An Artisan 1.x jar: the 2.x packages this plugin links against do not exist there.
            plugin.getLogger().log(Level.WARNING, "Installed Artisan is incompatible (MSQuests needs API "
                    + MIN_API_VERSION + "+): the quests run without it.", e);
            return null;
        }
    }

    public ArtisanAPI getApi() {
        return api;
    }

    @Override
    public boolean hasArtisan() {
        return true;
    }

    @Override
    public ChainRunner chainRunner() {
        return new MemberChainRunner(plugin, api.getActions()::run);
    }

    @Override
    public ConditionChecker conditionChecker() {
        return new PlayerConditionChecker(new ConditionsPort() {
            @Override
            public boolean test(Player player, List<?> conditions, Map<String, ?> args) {
                return api.getConditions().test(player, conditions, args);
            }

            @Override
            public List<String> validate(List<?> conditions) {
                return api.getConditions().validate(conditions);
            }
        }, plugin.getLogger());
    }

    @Override
    public ItemMatcher items() {
        ItemStackApi items = api.getItems();
        return new ItemMatcher() {
            @Override
            public boolean matches(Map<String, ?> spec, @Nullable ItemStack stack, Set<String> comparisons) {
                EnumSet<ItemComparison> criteria = EnumSet.noneOf(ItemComparison.class);
                for (String id : comparisons) {
                    ItemComparison c = ItemComparison.fromId(id);
                    if (c != null) criteria.add(c);
                }
                return items.matches(spec, stack, criteria.isEmpty() ? items.defaultComparisons(spec) : criteria);
            }

            @Override
            public @Nullable ItemStack build(Map<String, ?> spec) {
                return items.build(spec);
            }
        };
    }

    @Override
    public ProjectFiles projectFiles() {
        return new ArtisanProjectFiles(api.getProject());
    }

    @Override
    public TranslationsPort translations() {
        TranslationsApi translations = api.getTranslations();
        return new TranslationsPort() {
            @Override
            public Lookup register(Plugin owner, String namespace, String language, Supplier<Map<String, Map<String, String>>> tables) {
                MessageBundle bundle = translations.register(MessageBundleDeclaration.of(owner, namespace, tables::get)
                        .withDefaultLang(language));
                return key -> bundle.find(bundle.getDefaultLang(), key);
            }

            @Override
            public void unregister(String namespace) {
                translations.unregister(namespace);
            }
        };
    }

    /** The project {@code quests} in Artisan's projects folder ({@link EditorProject}). */
    @Override
    public File questsProject() {
        Plugin artisan = plugin.getServer().getPluginManager().getPlugin("Artisan");
        File artisanData = artisan != null ? artisan.getDataFolder() : new File(plugin.getDataFolder().getParentFile(), "Artisan");
        return EditorProject.directory(artisanData);
    }

    /** Artisan serves what it read at load: new Documents reach the editor on a reload. */
    @Override
    public void reloadProjects() {
        api.getProject().reload();
    }

    /** Tells Artisan menus to re-fetch the quest data sources. */
    @Override
    public void signalDataReload() {
        if (module != null) {
            module.signalDataReload();
        }
    }

    /** Registers the module: data sources, commands, actions, the quest_group kind and the example template. */
    @Override
    public void register() {
        MsQuestsArtisanModule created = new MsQuestsArtisanModule(plugin);
        api.getModules().register(created);
        this.module = created;
        plugin.getLogger().info("Artisan module registered (API " + api.getApiVersion() + ").");
    }

    @Override
    public void close() {
        // Artisan drops the module with the plugin's class loader.
    }
}

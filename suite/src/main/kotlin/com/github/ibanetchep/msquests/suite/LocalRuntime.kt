package com.github.ibanetchep.msquests.suite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import net.artisanmc.modules.api.action.ActionContext
import net.artisanmc.modules.api.action.ActionDeclaration
import net.artisanmc.modules.api.action.ActionHandler
import net.artisanmc.modules.api.action.ActionRegistry
import net.artisanmc.modules.api.action.ActionRuntime
import net.artisanmc.modules.api.command.CommandRegistry
import net.artisanmc.modules.api.item.ItemComparison
import net.artisanmc.modules.api.model.ModelField
import net.artisanmc.modules.api.model.ModelFieldType
import net.artisanmc.plugin.customitems.CustomItemPlugins
import net.artisanmc.plugin.engine.ActionChainCompiler
import net.artisanmc.plugin.engine.ActionExecutor
import net.artisanmc.plugin.engine.ConditionEvaluator
import net.artisanmc.plugin.engine.CooldownStore
import net.artisanmc.plugin.engine.CoreActionHandlers
import net.artisanmc.plugin.engine.ExecutionContext
import net.artisanmc.plugin.engine.MenuSessionStore
import net.artisanmc.plugin.engine.ModuleConditions
import net.artisanmc.plugin.integrations.SoftDepRegistry
import net.artisanmc.plugin.items.CapturedItemStore
import net.artisanmc.plugin.items.ItemSpecs
import net.artisanmc.plugin.model.withLangFallback
import net.artisanmc.plugin.project.ProjectContentReader
import net.artisanmc.plugin.project.ProjectRoots
import net.artisanmc.plugin.scheduling.schedulerFor
import net.artisanmc.plugin.session.NavigationHistoryStore
import net.artisanmc.plugin.yaml.LangFlattener
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Artisan's runtime, instantiated by MSQuests from its own copy when the Artisan plugin is not
 * running (ADR `runtime-library` ④): the core actions (`give_item`, `send_message`,
 * `run_command`...) and the [actions] MSQuests declares run from here, conditions are evaluated
 * here, item specs are built and compared here, and the projects are read from [projectsRoot]
 * by Artisan's reader (the `.artisan*` metadata stays out of reach). Menus are absent: an
 * `open_menu` tells the player so.
 *
 * Everything is MSQuests' own copy, relocated: nothing here is shared with another plugin, and an
 * action another module registers in Artisan does not exist here (logged once by the engine,
 * skipped).
 */
class LocalRuntime(
    private val plugin: JavaPlugin,
    /** `plugins/Artisan/projects/` or the plugin's own ([SuiteProjects.root]). */
    val projectsRoot: File,
    actions: List<ActionSpec>,
    /** The plugin's `language` (`fr_FR`): the language texts and `{i18n:key}` resolve in. */
    language: String,
    /** The message a player gets when a chain opens a menu. */
    menusUnavailable: () -> String,
    private val logger: Logger,
) {
    private val lang = language.split('_', '-', limit = 2)[0].lowercase(Locale.ROOT).ifBlank { "en" }
    private val scheduler = schedulerFor(plugin)
    private val roots = ProjectRoots(projectsRoot, logger)
    private val content = ProjectContentReader(roots)
    private val capturedItems = CapturedItemStore(roots, logger)
    private val customItems = CustomItemPlugins.withApi()
    private val deps = SoftDepRegistry(plugin, capturedItems, null)
    private val cooldowns = CooldownStore()
    private val evaluator = ConditionEvaluator(deps, cooldowns)
    private val registry: ActionRegistry = CoreActionHandlers.registry().also { registry ->
        actions.forEach { registry.register(declaration(it)) }
    }
    private val executor = ActionExecutor(
        plugin, NoMenus(menusUnavailable, logger), MenuSessionStore(), deps, cooldowns, evaluator,
        CommandRegistry(), NavigationHistoryStore(), registry,
    )
    private val conditions = ModuleConditions(
        evaluator = { evaluator },
        ownsPlayer = { scheduler.ownsPlayer(it.uniqueId) },
        warn = { logger.warning("[Artisan runtime] $it") },
    )
    private val itemSpecs = ItemSpecs(
        buildCustom = { material, count -> deps.buildCustomStack(material, count) },
        customItems = { customItems },
        logger = logger,
    )
    private val yaml = ObjectMapper(YAMLFactory())

    private class ProjectLang(val defaultLang: String, val maps: Map<String, Map<String, String>>)

    @Volatile private var langs: Map<String, ProjectLang> = emptyMap()

    init {
        reloadProjects()
        logger.info("Artisan runtime started without the Artisan plugin: ${registry.names().size} actions, projects read from $projectsRoot")
    }

    // ------------------------------------------------------------------ actions

    /** The equivalent of `api.getActions().run`: the chain compiled and run on the player's thread. */
    fun runChain(player: Player, chain: Any?, args: Map<String, Any>, projectId: String?) {
        if (chain == null || (chain is String && chain.isBlank())) return
        scheduler.onPlayer(player.uniqueId) {
            val steps = try {
                ActionChainCompiler.compile(chain)
            } catch (e: Exception) {
                logger.warning("[Artisan runtime] quest actions skipped, unreadable chain: ${e.message}")
                return@onPlayer
            }
            if (steps.isEmpty()) return@onPlayer
            val project = projectId?.let { langs[it] }
            try {
                executor.executeSteps(
                    steps,
                    ExecutionContext(
                        player = player,
                        args = args,
                        lang = lang,
                        defaultLang = project?.defaultLang ?: lang,
                        langMap = project?.let { withLangFallback(it.maps, it.defaultLang, lang) } ?: emptyMap(),
                        projectId = projectId,
                    ),
                )
            } catch (e: Exception) {
                logger.log(Level.WARNING, "[Artisan runtime] quest actions failed for ${player.name}", e)
            }
        }
    }

    // --------------------------------------------------------------- conditions

    /** The equivalent of `api.getConditions().test`: on the player's thread, else an [IllegalStateException]. */
    fun testConditions(player: Player, conditions: List<*>, args: Map<String, *>): Boolean =
        this.conditions.test(player, conditions, args)

    fun validateConditions(conditions: List<*>): List<String> = this.conditions.validate(conditions)

    // -------------------------------------------------------------------- items

    fun buildItem(spec: Map<String, *>): ItemStack? = itemSpecs.build(spec)

    /** [comparisons] by id (`material`, `name`...); empty: the criteria the spec carries. */
    fun matchesItem(spec: Map<String, *>, stack: ItemStack?, comparisons: Set<String>): Boolean {
        val criteria = comparisons.mapNotNull { ItemComparison.fromId(it) }.toSet()
        return itemSpecs.matches(spec, stack, criteria.ifEmpty { itemSpecs.defaultComparisons(spec) })
    }

    // ----------------------------------------------------------------- projects

    fun projectIds(): List<String> = content.projectIds()
    fun listFiles(projectId: String, dir: String): List<String> = content.list(projectId, dir)
    fun readFile(projectId: String, path: String): String? = content.read(projectId, path)

    /** Re-reads what the chains need from disk: the captured items and the projects' `lang/`. */
    fun reloadProjects() {
        capturedItems.reload()
        langs = roots.all().mapValues { (_, dir) -> readLang(dir) }
    }

    fun close() {
        // Nothing to release: the stores are the plugin's own, dropped with it.
    }

    /** `manifest.yaml` + `lang/<lang>.yaml`, tolerant per file like Artisan's reader. */
    private fun readLang(projectDir: File): ProjectLang {
        val manifest = readYaml(File(projectDir, "manifest.yaml")) ?: emptyMap()
        val defaultLang = manifest["default_lang"]?.toString()?.ifBlank { null } ?: "en"
        val languages = ((manifest["languages"] as? List<*>)?.map { it.toString() } ?: emptyList()) + defaultLang + lang
        val maps = languages.distinct().associateWith { code ->
            readYaml(File(projectDir, "lang/$code.yaml"))?.let { LangFlattener.flatten(it) } ?: emptyMap()
        }
        return ProjectLang(defaultLang, maps)
    }

    private fun readYaml(file: File): Map<String, Any?>? {
        if (!file.isFile) return null
        return try {
            @Suppress("UNCHECKED_CAST")
            yaml.readValue(file, Map::class.java) as Map<String, Any?>
        } catch (e: Exception) {
            logger.warning("[Artisan runtime] ${file.relativeTo(projectsRoot)} unreadable, ignored: ${e.message}")
            null
        }
    }

    // ------------------------------------------------------------- declarations

    private fun declaration(spec: ActionSpec): ActionDeclaration =
        ActionDeclaration(spec.name, spec.args.map(::field), spec.primary, handler(spec.handler))

    private fun field(arg: ActionArg): ModelField {
        val type = when (arg.type) {
            ActionArg.Type.STRING -> ModelFieldType.string()
            ActionArg.Type.LTEXT -> ModelFieldType.ltext()
            ActionArg.Type.INTEGER -> ModelFieldType.integer()
            ActionArg.Type.BOOL -> ModelFieldType.bool()
            ActionArg.Type.ENUM -> ModelFieldType.enumOf(arg.values)
            ActionArg.Type.DOCUMENT_REF -> ModelFieldType.documentRef(arg.documentKind!!)
        }
        var field = ModelField(arg.name, type, arg.required)
        if (arg.default != null) field = field.withDefault(arg.default)
        if (arg.help != null) field = field.withHelp(arg.help)
        if (arg.min != null) field = field.withRange(arg.min, null)
        return field
    }

    private fun handler(handler: ActionCallHandler) = ActionHandler { ctx, args, runtime ->
        handler.run(Call(ctx, args, runtime))
        ctx
    }

    private class Call(
        private val ctx: ActionContext,
        override val args: Map<String, Any>,
        private val runtime: ActionRuntime,
    ) : ActionCall {
        override val player: Player get() = ctx.player
        override val contextArgs: Map<String, Any> get() = ctx.args
        override fun text(value: Any?): String = runtime.text(ctx, value)
        override fun warn(message: String) = runtime.warn(message)
    }
}

package com.github.ibanetchep.msquests.suite

import org.bukkit.entity.Player

/**
 * An action MSQuests declares (`quests:*`), in MSQuests' own words: registered in Artisan's
 * registry when the plugin runs ([ArtisanIntegration] builds the `ActionDeclaration`), in the
 * local engine's when it runs without it ([LocalRuntime]). The two registries are different
 * classes (the local one is relocated), so the declaration is neutral and each side adapts it.
 */
class ActionSpec(
    val name: String,
    val args: List<ActionArg>,
    /** The argument a scalar wire value binds (`quests:distribute: daily`); null = no scalar form. */
    val primary: String?,
    val handler: ActionCallHandler,
) {
    init {
        require(NAME.matches(name)) { "action name '$name' must match ${NAME.pattern}" }
        val names = args.map { it.name }
        require(names.size == names.toSet().size) { "action '$name' declares an argument twice" }
        require(primary == null || primary in names) { "action '$name': primary argument '$primary' is not declared" }
    }

    fun arg(name: String): ActionArg? = args.firstOrNull { it.name == name }

    /** The wire value as the arguments map, the declared defaults filled in (like Artisan's `bind`). */
    fun bind(wire: Any?): Map<String, Any> {
        val out = LinkedHashMap<String, Any>()
        when {
            wire == null -> {}
            wire is Map<*, *> -> wire.forEach { (k, v) -> if (v != null) out[k.toString()] = v }
            primary != null -> out[primary] = wire
        }
        for (arg in args) {
            val default = arg.default
            if (default != null && arg.name !in out) out[arg.name] = default
        }
        return out
    }

    private companion object {
        val NAME = Regex("^[a-z][a-z0-9_]*(:[a-z][a-z0-9_]*)?$")
    }
}

/** One argument of an [ActionSpec]; the subset of Artisan's `ModelField` the quest actions use. */
class ActionArg(
    val name: String,
    val type: Type,
    val required: Boolean = false,
    val default: Any? = null,
    val help: String? = null,
    val min: Double? = null,
    /** The values of an [Type.ENUM] argument. */
    val values: List<String> = emptyList(),
    /** The Document kind of a [Type.DOCUMENT_REF] argument. */
    val documentKind: String? = null,
) {
    enum class Type { STRING, LTEXT, INTEGER, BOOL, ENUM, DOCUMENT_REF }

    fun required(): ActionArg = copy(required = true)
    fun withDefault(value: Any?): ActionArg = copy(default = value)
    fun withHelp(help: String?): ActionArg = copy(help = help)
    fun withMin(min: Double?): ActionArg = copy(min = min)

    private fun copy(
        required: Boolean = this.required,
        default: Any? = this.default,
        help: String? = this.help,
        min: Double? = this.min,
    ) = ActionArg(name, type, required, default, help, min, values, documentKind)

    companion object {
        @JvmStatic fun string(name: String) = ActionArg(name, Type.STRING)
        @JvmStatic fun ltext(name: String) = ActionArg(name, Type.LTEXT)
        @JvmStatic fun integer(name: String) = ActionArg(name, Type.INTEGER)
        @JvmStatic fun bool(name: String) = ActionArg(name, Type.BOOL)
        @JvmStatic fun enumOf(name: String, values: List<String>) = ActionArg(name, Type.ENUM, values = values)
        @JvmStatic fun documentRef(name: String, kind: String) = ActionArg(name, Type.DOCUMENT_REF, documentKind = kind)
    }
}

/** What a handler sees of one execution: the engine's context and runtime, whichever engine runs it. */
interface ActionCall {
    val player: Player
    /** The action's own arguments, bound and defaulted. */
    val args: Map<String, Any>
    /** The chain's bindings (`quest`, `progress_ratio`...), what `{name}` placeholders read. */
    val contextArgs: Map<String, Any>
    /** Resolves an `ltext` argument for the player: language maps, `{i18n:key}`, placeholders. */
    fun text(value: Any?): String
    /** A warning with the engine's prefix: bad data must not break the chain. */
    fun warn(message: String)
}

fun interface ActionCallHandler {
    fun run(call: ActionCall)
}

package com.github.ibanetchep.msquests.suite

import net.artisanmc.plugin.engine.ExecutionContext
import net.artisanmc.plugin.engine.MenuProvider
import net.artisanmc.plugin.engine.RuntimeMenu
import net.artisanmc.plugin.engine.RuntimeProject
import net.artisanmc.plugin.model.RuntimeDialog
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.logging.Logger

/**
 * The menus without the Artisan plugin: none. An `open_menu` / `open_dialog` in a chain tells the
 * player the interface is unavailable and logs it once (ADR `runtime-library` ④).
 */
internal class NoMenus(private val message: () -> String, private val logger: Logger) : MenuProvider {
    @Volatile private var warned = false

    override fun load() {}
    override fun unload() {}
    override fun shutdown() {}
    override fun findMenu(menuId: String): RuntimeMenu? = null
    override fun findMenu(ref: String, fromProject: String?): RuntimeMenu? = null
    override fun listMenuIds(): List<String> = emptyList()
    override fun runtimeProjects(): List<RuntimeProject> = emptyList()
    override fun findDialog(dialogId: String): RuntimeDialog? = null
    override fun findDialog(ref: String, fromProject: String?): RuntimeDialog? = null

    override fun openMenu(player: Player, menuId: String, lang: String, ctx: ExecutionContext) = unavailable(player, "menu '$menuId'")
    override fun openMenu(player: Player, menuId: String, lang: String) = unavailable(player, "menu '$menuId'")
    override fun openDialog(player: Player, dialogId: String, lang: String, ctx: ExecutionContext) = unavailable(player, "dialog '$dialogId'")

    private fun unavailable(player: Player, what: String) {
        player.sendMessage(Component.text(message()))
        if (!warned) {
            warned = true
            logger.warning("A quest chain opens the $what: menus need the Artisan plugin, the player was told the interface is unavailable.")
        }
    }
}

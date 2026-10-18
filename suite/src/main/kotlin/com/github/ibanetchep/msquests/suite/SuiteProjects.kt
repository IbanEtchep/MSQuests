package com.github.ibanetchep.msquests.suite

import org.bukkit.plugin.Plugin
import java.io.File

/** Where the projects are, for a plugin reading them without the Artisan plugin. */
object SuiteProjects {
    const val ARTISAN_PROJECTS = "Artisan/projects"

    /**
     * `plugins/Artisan/projects/` when that folder exists (Artisan installed, running or not: the
     * content is the same), else `plugins/<plugin>/projects/`, created. The same rule as the
     * database: what Artisan has is used, nothing is created under `plugins/Artisan/`.
     */
    @JvmStatic
    fun root(plugin: Plugin): File {
        val artisan = File(plugin.server.pluginsFolder, ARTISAN_PROJECTS)
        if (artisan.isDirectory) return artisan
        return File(plugin.dataFolder, "projects").also { it.mkdirs() }
    }
}

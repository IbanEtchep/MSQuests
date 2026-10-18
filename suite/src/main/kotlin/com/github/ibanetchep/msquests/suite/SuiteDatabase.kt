package com.github.ibanetchep.msquests.suite

import net.artisanmc.plugin.database.ModuleDatabase
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.function.Consumer
import java.util.logging.Logger
import javax.sql.DataSource

/**
 * The plugin's pool, opened by the suite's runtime (Artisan's `ModuleDatabase`, ADR
 * `runtime-library` ⑤): `plugins/Artisan/environment.yml` when it exists, else the plugin's own
 * `database` section (written with H2 defaults when absent), `database.own: true` forcing the
 * latter. One H2 file per plugin, under `plugins/<plugin>/database/`.
 */
class SuiteDatabase private constructor(private val db: ModuleDatabase, private val owner: String) {

    /** Where the settings were read: the path of Artisan's file, or `config.yml`. */
    val source: String get() = db.source

    /** `h2`, `mysql` or `mariadb`. */
    val type: String get() = db.settings.type

    /** The pool, opened on the first call (a connection is checked). */
    fun dataSource(): DataSource = db.dataSource(owner)

    fun close() = db.close()

    companion object {
        /** The H2 file MSQuests used to write next to its config, before the suite's runtime. */
        private const val LEGACY_H2 = "database.mv.db"

        /**
         * Resolves the settings and opens nothing yet. [onMissingEnv] is told of a `${VAR}`
         * reference the environment does not define.
         */
        @JvmStatic
        @JvmOverloads
        fun open(plugin: Plugin, onMissingEnv: Consumer<String> = Consumer {}): SuiteDatabase {
            val db = ModuleDatabase.of(plugin) { onMissingEnv.accept(it) }
            moveLegacyH2(plugin.dataFolder, db, plugin.logger)
            return SuiteDatabase(db, plugin.name)
        }

        /**
         * `plugins/MSQuests/database.mv.db` becomes `plugins/MSQuests/database/suite.mv.db` once,
         * when the new file does not exist yet: a server that ran H2 before keeps its progress.
         */
        internal fun moveLegacyH2(dataFolder: File, db: ModuleDatabase, logger: Logger) {
            if (db.settings.type != "h2") return
            val legacy = File(dataFolder, LEGACY_H2)
            val target = File(db.settings.h2Dir, "suite.mv.db")
            if (!legacy.isFile || target.exists()) return
            target.parentFile.mkdirs()
            if (legacy.renameTo(target)) {
                logger.info("[database] ${legacy.name} moved to ${target.relativeTo(dataFolder)}: the H2 database now lives in database/.")
            } else {
                logger.warning("[database] could not move ${legacy.path} to ${target.path}; the old progress is not read until it is moved by hand.")
            }
        }
    }
}

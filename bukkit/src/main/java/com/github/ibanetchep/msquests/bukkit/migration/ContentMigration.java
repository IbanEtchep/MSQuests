package com.github.ibanetchep.msquests.bukkit.migration;

import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.bukkit.config.ContentSource;
import com.github.ibanetchep.msquests.bukkit.document.LegacyContentConverter;
import com.github.ibanetchep.msquests.bukkit.document.MigrationMapFile;
import com.github.ibanetchep.msquests.bukkit.document.MigrationReport;
import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentRepository;
import com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository;
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.database.LegacyDataCopier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * {@code /msquests migrate}: converts {@code quests/*.yml} into {@code quest_group} Documents of
 * the editor project and says what the database copy will do, in a report written to
 * {@code migration/report-<time>.txt}. A dry run writes only the report; {@code --confirm} also
 * writes the Documents (never over an existing one) and {@code migration/map.yaml}.
 *
 * <p>Nothing here touches {@code quests/*.yml} or the {@code msquests_*} tables. The progress is
 * copied into {@code artisan_quests_*} at the next start with {@code content_source: artisan},
 * when nothing writes to the old tables any more.
 */
public final class ContentMigration {

    public static final String DIR = "migration";

    public record Outcome(Path report, int groups, int quests, long documentsToWrite, int warnings,
                          boolean confirmed, @Nullable LegacyDataCopier.Result database) {
    }

    private final Path dataFolder;
    private final String language;
    private final ContentSource activeSource;
    private final @Nullable LegacyDataCopier copier;
    private final Logger logger;
    private final Function<String, String> messageKeys;

    public ContentMigration(Path dataFolder, String language, ContentSource activeSource,
                            @Nullable LegacyDataCopier copier, Logger logger) {
        this(dataFolder, language, activeSource, copier, logger, key -> null);
    }

    /** @param messageKeys text of a MSQuests lang key, for actions that name one ({@code message_key}) */
    public ContentMigration(Path dataFolder, String language, ContentSource activeSource,
                            @Nullable LegacyDataCopier copier, Logger logger, Function<String, String> messageKeys) {
        this.messageKeys = messageKeys;
        this.dataFolder = dataFolder;
        this.language = language;
        this.activeSource = activeSource;
        this.copier = copier;
        this.logger = logger;
    }

    public static Path mapFile(Path dataFolder) {
        return dataFolder.resolve(DIR).resolve("map.yaml");
    }

    public Outcome run(boolean confirm) throws IOException {
        Path project = EditorProject.directory(dataFolder.toFile()).toPath();
        Path groupsDir = project.resolve(QuestGroupDocumentRepository.DIR);
        Path migrationDir = dataFolder.resolve(DIR);
        Path tmp = migrationDir.resolve("tmp");

        QuestConfigYamlRepository legacyRepository = new QuestConfigYamlRepository(dataFolder.resolve("quests"), logger, messageKeys);
        QuestConfigLoad legacy = legacyRepository.loadAllGroups().join();
        MigrationMap previous = MigrationMapFile.read(mapFile(dataFolder));
        // The language the project already declares, else the one its manifest will get.
        String lang = Files.exists(project.resolve("manifest.yaml"))
                ? new QuestGroupDocumentRepository(project).defaultLang()
                : EditorProject.defaultLang(language);
        String timezone = ZoneId.systemDefault().getId();

        LegacyContentConverter converter = new LegacyContentConverter(lang, timezone);
        LegacyContentConverter.Plan plan = converter.plan(legacy.groups(), legacy.failures(),
                legacyRepository.getConversionNotes(), groupsDir, previous != null ? previous : MigrationMap.empty());
        MigrationReport report = plan.report();

        LegacyDataCopier.Result database = database(plan.map(), report);

        if (confirm) {
            EditorProject.writeIfAbsent(project.toFile(), language);
            converter.apply(plan, groupsDir, tmp);
            MigrationMapFile.write(mapFile(dataFolder), plan.map(), tmp);
            report.section("Written");
            report.line(plan.toWrite() + " Document(s) in editor/quest_groups/, and migration/map.yaml.");
        } else {
            report.section("Dry run");
            report.line("Nothing was written. Run /msquests migrate --confirm to write the Documents.");
        }

        report.section("Next steps");
        report.line("Content source now: " + activeSource.name().toLowerCase() + ".");
        report.line("1. /msquests migrate --confirm (if not done), then read this report again.");
        report.line("2. Set content_source: artisan in config.yml and restart, ideally right after a daily reset:"
                + " the progress is copied into artisan_quests_* during that start.");
        report.line("3. Compare /msquests stats before and after. To go back, set content_source: legacy"
                + " (or put the previous jar back): quests/*.yml and msquests_* are untouched. Progress made"
                + " meanwhile stays in artisan_quests_*, and the copy never runs a second time.");

        Path reportFile = migrationDir.resolve("report-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
        Files.createDirectories(migrationDir);
        Files.writeString(reportFile, header(confirm) + report.render(), StandardCharsets.UTF_8);

        int quests = legacy.groups().values().stream()
                .mapToInt(g -> g.quests() != null ? g.quests().size() : 0).sum();
        return new Outcome(reportFile, legacy.groups().size(), quests, plan.toWrite(), report.warnings(), confirm, database);
    }

    private @Nullable LegacyDataCopier.Result database(MigrationMap map, MigrationReport report) {
        report.section("Saved progress (msquests_* → artisan_quests_*)");
        if (copier == null) {
            report.warn("Database unavailable: the copy could not be checked.");
            return null;
        }
        try {
            if (copier.isCopied()) {
                report.line("Already copied: artisan_quests_* holds the live progress. Nothing more to copy.");
                return new LegacyDataCopier.Result(List.of(), List.of(), true);
            }
            LegacyDataCopier.Result result = copier.plan(map);
            for (LegacyDataCopier.TableCount table : result.tables()) {
                report.line(table.toString());
            }
            for (String note : result.notes()) {
                report.warn("Left behind (no config any more, already ignored today): " + note);
            }
            report.line("Copied at the first start with content_source: artisan, in one transaction.");
            return result;
        } catch (RuntimeException e) {
            logger.log(java.util.logging.Level.WARNING, "Could not plan the database copy", e);
            report.warn("Could not read the msquests_* tables: " + e.getMessage());
            return null;
        }
    }

    private static String header(boolean confirm) {
        return "MSQuests content migration " + (confirm ? "(confirmed)" : "(dry run)") + " — "
                + LocalDateTime.now().withNano(0) + System.lineSeparator() + System.lineSeparator();
    }
}

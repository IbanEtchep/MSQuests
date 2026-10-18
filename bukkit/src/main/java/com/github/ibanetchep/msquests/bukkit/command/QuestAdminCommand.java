package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.migration.ContentMigration;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.LegacyDataCopier;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Command("msquests")
@CommandPermission("msquests.admin")
public class QuestAdminCommand {

    private final BukkitQuestsPlugin plugin;
    private final AtomicBoolean migrationRunning = new AtomicBoolean();

    public QuestAdminCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @Description("Reloads the quest configs")
    public void reload(CommandSender sender) {
        plugin.loadConfig();
        plugin.getTranslator().load();

        if (plugin.getGlobalConfig().contentSource() != plugin.getContentSource()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>content_source changed to "
                    + plugin.getGlobalConfig().contentSource().name().toLowerCase()
                    + ": it applies at the next restart. Reloading from "
                    + plugin.getContentSource().name().toLowerCase() + ".</yellow>"));
        }

        plugin.reloadQuestContent()
                .thenRun(() -> {
                    sender.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_RELOAD).toComponent());
                })
                .exceptionally(e -> {
                    // The previous configs stay active: a refused reload changes nothing.
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<red>Quest reload refused, the previous configs are still active. Check console for details.</red>"));
                    plugin.getLogger().log(Level.SEVERE, "Failed during quest reload", e);
                    return null;
                });
    }

    @Subcommand("migrate")
    @Description("Converts quests/*.yml into quest_group Documents (dry run unless --confirm)")
    public void migrate(CommandSender sender, @Switch("dry-run") boolean dryRun, @Switch("confirm") boolean confirm) {
        if (dryRun && confirm) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Pick one: --dry-run or --confirm.</red>"));
            return;
        }
        if (!migrationRunning.compareAndSet(false, true)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>A migration is already running.</red>"));
            return;
        }
        DbAccess db = plugin.getDbAccess();
        ContentMigration migration = new ContentMigration(plugin.getDataFolder().toPath(),
                plugin.getSuite().questsProject().toPath(),
                plugin.getGlobalConfig().language(), plugin.getContentSource(),
                db != null ? new LegacyDataCopier(db.getJdbi()) : null, plugin.getLogger(), plugin::langText);

        CompletableFuture.supplyAsync(() -> {
            try {
                return migration.run(confirm);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).whenComplete((outcome, error) -> {
            migrationRunning.set(false);
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Quest migration failed", error);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<red>Migration failed, nothing was switched. Check console for details.</red>"));
                return;
            }
            // The runtime serves what it read at load: the new Documents reach the editor on a reload.
            if (outcome.confirmed() && outcome.documentsToWrite() > 0) {
                plugin.getSuite().reloadProjects();
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize(migrationSummary(outcome)));
        });
    }

    private static String migrationSummary(ContentMigration.Outcome outcome) {
        StringBuilder text = new StringBuilder("<gold>Quest migration " + (outcome.confirmed() ? "done" : "(dry run)")
                + ":</gold> " + outcome.groups() + " group(s), " + outcome.quests() + " quest(s), "
                + outcome.documentsToWrite() + " Document(s) " + (outcome.confirmed() ? "written" : "to write") + ", "
                + (outcome.warnings() == 0 ? "<green>no warning</green>" : "<yellow>" + outcome.warnings() + " warning(s)</yellow>")
                + ".");
        LegacyDataCopier.Result db = outcome.database();
        if (db != null && db.alreadyCopied()) {
            text.append("<newline>Saved progress: already copied into artisan_quests_*.");
        } else if (db != null) {
            text.append("<newline>Saved progress to copy at the switch: ").append(db.summary()).append(".");
        }
        text.append("<newline>Report: <white>").append(outcome.report()).append("</white>");
        text.append(outcome.confirmed()
                ? "<newline>Next: content_source: artisan in config.yml, then restart."
                : "<newline>Next: /msquests migrate --confirm to write the Documents.");
        return text.toString();
    }

    @Subcommand("stats")
    @Description("Counts the loaded groups, quests and progress, to compare before and after a migration")
    public void stats(CommandSender sender) {
        int groups = plugin.getQuestConfigRegistry().getQuestGroupConfigs().size();
        int quests = plugin.getQuestConfigRegistry().getQuestGroupConfigs().values().stream()
                .mapToInt(g -> g.getQuestConfigs().size()).sum();
        int actors = 0;
        int instances = 0;
        int inProgress = 0;
        int startedObjectives = 0;
        for (QuestActor actor : plugin.getQuestActorRegistry().getActors().values()) {
            actors++;
            for (Quest quest : actor.getQuests().values()) {
                instances++;
                if (quest.isActive()) inProgress++;
                startedObjectives += (int) quest.getObjectives().stream().filter(o -> o.getProgress() > 0).count();
            }
        }
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Quests (" + plugin.getContentSource().name()
                .toLowerCase() + "):</gold> " + groups + " group(s), " + quests + " quest(s); " + actors
                + " loaded actor(s) holding " + instances + " instance(s) this period, " + inProgress
                + " in progress, " + startedObjectives + " objective(s) with progress."));
    }

    @Subcommand("actor <actor type> <actor> list <group>")
    public void listActorQuests(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            @Default("1") @Range(min = 1) @Named("page") int page
    ) {
        int lastPage = actor.getQuestsByGroupCount(group) / 10 + 1;

        if(page > lastPage) {
            sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS)
                    .placeholder("page", String.valueOf(page))
                    .placeholder("last_page", String.valueOf(lastPage))
                    .toComponent());
            return;
        }

        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_BODY)
                .applyPlaceholderResolver(group)
                .listPlaceholder(
                        "quests",
                        actor.getQuestsByGroup(group, page),
                        quest -> {
                            StringBuilder objectivesBuilder = new StringBuilder();
                            for (QuestObjective obj : quest.getObjectives()) {
                                String rendered = MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_OBJECTIVE_TEMPLATE)
                                        .applyPlaceholderResolver(obj)
                                        .toStringRaw();
                                objectivesBuilder.append(rendered);
                            }

                            return MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_QUEST_TEMPLATE)
                                    .applyPlaceholderResolver(quest)
                                    .placeholder("objectives", objectivesBuilder.toString())
                                    .toStringRaw();
                        }
                )
                .placeholder("group_key", group.getKey())
                .placeholder("page", String.valueOf(page))
                .placeholder("next_page", String.valueOf(page + 1))
                .placeholder("previous_page", String.valueOf(page - 1))
                .placeholder("last_page", String.valueOf(lastPage))
                .placeholder("actor_type", actorType)
                .placeholder("actor", actor.getName())
                .toComponent()
        );
    }

    @Subcommand("actor <actor type> <actor> startRandom <group> <amount>")
    public void distributeGroupQuestsRandom(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            @Default("1") @Range(min = 1) @Named("amount") int amount
    ) {
        int startedCount = plugin.getQuestLifecycleService().distributeQuests(actor, group, QuestDistributionStrategy.RANDOM, amount);

        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_DISTRIBUTED_GROUP)
                .applyPlaceholderResolver(group)
                .applyPlaceholderResolver(actor)
                .placeholder("amount", String.valueOf(startedCount))
                .toComponent());
    }

    @Subcommand("actor <actor type> <actor> startNext <group> <amount>")
    public void distributeGroupNextQuests(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            @Default("1") @Range(min = 1) @Named("amount") int amount
    ) {
        int startedCount = plugin.getQuestLifecycleService().distributeQuests(actor, group, QuestDistributionStrategy.SEQUENTIAL, amount);

        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_DISTRIBUTED_GROUP)
                .applyPlaceholderResolver(group)
                .applyPlaceholderResolver(actor)
                .placeholder("amount", String.valueOf(startedCount))
                .toComponent());
    }

    @Subcommand("actor <actor type> <actor> start <group> <quest config>")
    public void startActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            QuestConfig questConfig
    ) {
        QuestStartResult result = plugin.getQuestLifecycleService().startQuest(actor, questConfig, QuestDistributionStrategy.NONE, null);
        sender.reply(MessageBuilder.translatable(result).toComponent());
    }

    @Subcommand("actor <actor type> <actor> rotate <group> <quest>")
    public void rotateActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            Quest quest
    ) {
        QuestRotateResult result = plugin.getQuestLifecycleService().rotateQuest(actor, quest);
        sender.reply(MessageBuilder.translatable(result).toComponent());
    }

    @Subcommand("actor <actor type> <actor> complete <group> <quest>")
    public void completeActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            Quest quest
    ) {
        plugin.getQuestLifecycleService().completeQuest(quest);
        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_FORCE_COMPLETED)
                .applyPlaceholderResolver(quest)
                .toComponent());
    }
}

package com.github.ibanetchep.artisanattribution;

import com.github.ibanetchep.artisanattribution.config.AttributionConfig;
import com.github.ibanetchep.artisanattribution.config.AttributionSource;
import com.github.ibanetchep.artisanattribution.config.PromptPolicy;
import com.github.ibanetchep.artisanattribution.storage.AnswerResult;
import com.github.ibanetchep.artisanattribution.storage.AttributionRepository;
import com.github.ibanetchep.artisanattribution.storage.PlayerAttribution;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.datasource.DataReloadEvent;
import net.artisanmc.modules.api.scheduler.TaskScheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The attribution flow: who is asked on join, what an answer does, and the in-memory view the
 * menus, placeholders and commands read.
 *
 * <p>Threads: the state of online players is loaded before they join (pre-login, async) and read
 * from memory by everything that runs on a game thread (fetchers, placeholders, actions). The
 * database is only touched from {@link TaskScheduler#runAsync}.
 */
public final class AttributionService {

    public static final String BYPASS_PERMISSION = "artisanattribution.bypass";
    public static final String SOURCES_DATA_SOURCE = "attribution:sources";

    private final ArtisanAPI api;
    private final TaskScheduler scheduler;
    private final AttributionRepository repository;
    private final Messages messages;
    private final Logger logger;

    private volatile AttributionConfig config;

    /** Online players only, filled at pre-login and dropped on quit. */
    private final Map<UUID, PlayerAttribution> players = new ConcurrentHashMap<>();
    /** Players whose answer is being written: a second click is ignored meanwhile. */
    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();
    /** The menu ref last reported missing, so a missing menu is logged once, not at every join. */
    private volatile @Nullable String reportedMissingMenu;

    public AttributionService(ArtisanAPI api, TaskScheduler scheduler, AttributionRepository repository,
                              Messages messages, Logger logger, AttributionConfig config) {
        this.api = api;
        this.scheduler = scheduler;
        this.repository = repository;
        this.messages = messages;
        this.logger = logger;
        this.config = config;
    }

    public AttributionConfig config() {
        return config;
    }

    public void applyConfig(AttributionConfig config) {
        this.config = config;
        this.reportedMissingMenu = null;
        signalDataReload();
    }

    // ------------------------------------------------------------------ players

    /** Blocking: from the async pre-login. A failure leaves the player unknown, so not asked this session. */
    public void load(UUID playerId) {
        try {
            players.put(playerId, repository.find(playerId));
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not read the attribution of " + playerId
                    + "; they will not be asked until they join again", e);
        }
    }

    public void forget(UUID playerId) {
        players.remove(playerId);
        pending.remove(playerId);
    }

    public @Nullable PlayerAttribution state(UUID playerId) {
        return players.get(playerId);
    }

    /** On the player's thread, from the join event. */
    public void onJoin(Player player) {
        PlayerAttribution state = players.get(player.getUniqueId());
        if (state == null) return;
        AttributionConfig current = config;
        if (!PromptPolicy.shouldPromptOnJoin(current.prompt(), state, player.hasPlayedBefore(),
                player.hasPermission(BYPASS_PERMISSION))) {
            return;
        }
        Runnable prompt = () -> promptOnJoin(player);
        long delay = current.prompt().delayTicks();
        if (delay > 0) scheduler.runForLater(player, delay, prompt);
        else scheduler.runFor(player, prompt);
    }

    private void promptOnJoin(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerAttribution state = players.get(playerId);
        if (!player.isOnline() || state == null || state.answered()) return;
        if (!openMenu(player)) return;

        long now = System.currentTimeMillis();
        players.computeIfPresent(playerId, (id, s) -> s.withPrompt(now));
        scheduler.runAsync(() -> {
            try {
                repository.markPrompted(playerId, now);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Could not record that " + player.getName() + " was asked", e);
            }
        });
    }

    /** Opens the configured menu; false, logged once per menu ref, when Artisan has no such menu. */
    public boolean openMenu(Player player) {
        String menu = config.menu();
        if (api.getMenus().open(player, menu)) return true;
        if (!menu.equals(reportedMissingMenu)) {
            reportedMissingMenu = menu;
            logger.warning("The attribution menu '" + menu + "' does not exist in Artisan: nobody is asked. "
                    + "Fix 'menu' in config.yml, or restore the menu in the editor.");
        }
        return false;
    }

    // ------------------------------------------------------------------ answers

    /**
     * The player picked {@code sourceId}, on their thread (the {@code attribution:choose} action).
     * The answer is written first; only once stored do the source's actions run, so a reward is
     * given once, even with a double click or two servers of a network.
     */
    public void answer(Player player, String sourceId) {
        UUID playerId = player.getUniqueId();
        AttributionConfig current = config;
        AttributionSource source = current.source(sourceId);
        if (source == null) {
            logger.warning(player.getName() + " picked the source '" + sourceId + "', which config.yml does not define");
            messages.send(player, "answer.unknown_source");
            return;
        }
        PlayerAttribution state = players.get(playerId);
        if (state != null && state.answered()) {
            messages.send(player, "answer.already", Map.of("source", sourceName(player, state.source())));
            return;
        }
        if (!pending.add(playerId)) return;

        long now = System.currentTimeMillis();
        scheduler.runAsync(() -> {
            AnswerResult result;
            try {
                result = repository.answer(playerId, sourceId, now);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not save the answer of " + player.getName(), e);
                pending.remove(playerId);
                scheduler.runFor(player, () -> messages.send(player, "answer.error"));
                return;
            }
            if (result == AnswerResult.SAVED) {
                players.computeIfPresent(playerId, (id, s) -> s.withAnswer(sourceId, now));
                counts.merge(sourceId, 1, Integer::sum);
                scheduler.runFor(player, () -> afterAnswer(player, source, current));
            } else {
                refreshAndReportAlreadyAnswered(player);
            }
        });
    }

    private void afterAnswer(Player player, AttributionSource source, AttributionConfig current) {
        pending.remove(player.getUniqueId());
        signalDataReload();
        if (!player.isOnline()) return;
        if (current.prompt().closeOnAnswer()) api.getMenus().close(player);

        String name = sourceName(player, source.id());
        messages.send(player, "answer.thanks", Map.of("source", name));
        Map<String, Object> bindings = Map.of("source", source.id(), "source_name", name);
        api.getActions().run(player, source.actions(), bindings);
        api.getActions().run(player, current.onAnswer(), bindings);
    }

    private void refreshAndReportAlreadyAnswered(Player player) {
        UUID playerId = player.getUniqueId();
        try {
            PlayerAttribution stored = repository.find(playerId);
            players.computeIfPresent(playerId, (id, s) -> stored);
            scheduler.runFor(player, () -> {
                pending.remove(playerId);
                if (player.isOnline()) {
                    messages.send(player, "answer.already", Map.of("source", sourceName(player, stored.source())));
                }
            });
        } catch (SQLException e) {
            pending.remove(playerId);
            logger.log(Level.WARNING, "Could not read the attribution of " + player.getName(), e);
        }
    }

    /** Clears the answer of a player, online or not. Completes with false when they had none. */
    public CompletableFuture<Boolean> reset(UUID playerId) {
        CompletableFuture<Boolean> done = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                boolean cleared = repository.reset(playerId);
                if (cleared) {
                    players.computeIfPresent(playerId, (id, s) -> s.cleared());
                    refreshCounts();
                }
                done.complete(cleared);
            } catch (SQLException e) {
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    /** Answers per source id, read from the database (the other servers of a network included). */
    public CompletableFuture<Map<String, Integer>> stats() {
        CompletableFuture<Map<String, Integer>> done = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                Map<String, Integer> fresh = repository.countBySource();
                replaceCounts(fresh);
                done.complete(fresh);
            } catch (SQLException e) {
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    /** Blocking. */
    public void refreshCounts() throws SQLException {
        replaceCounts(repository.countBySource());
    }

    private void replaceCounts(Map<String, Integer> fresh) {
        counts.keySet().retainAll(fresh.keySet());
        counts.putAll(fresh);
        signalDataReload();
    }

    public int count(String sourceId) {
        return counts.getOrDefault(sourceId, 0);
    }

    // ------------------------------------------------------------------ texts

    /** The source's name in the viewer's language; the raw id for a source config.yml no longer has. */
    public String sourceName(CommandSender viewer, @Nullable String sourceId) {
        if (sourceId == null) return "";
        AttributionSource source = config.source(sourceId);
        if (source == null) return sourceId;
        return source.name().resolve(lang(viewer), api.getTranslations().getDefaultLang());
    }

    public String lang(@Nullable CommandSender viewer) {
        return viewer != null ? api.getTranslations().langOf(viewer) : api.getTranslations().getDefaultLang();
    }

    public String defaultLang() {
        return api.getTranslations().getDefaultLang();
    }

    /** Menus showing {@code attribution:sources} fetch it again. */
    public void signalDataReload() {
        api.getEvents().publish(new DataReloadEvent(SOURCES_DATA_SOURCE));
    }
}

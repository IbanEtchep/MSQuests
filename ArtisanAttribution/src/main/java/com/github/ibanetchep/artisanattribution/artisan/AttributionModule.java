package com.github.ibanetchep.artisanattribution.artisan;

import com.github.ibanetchep.artisanattribution.AttributionService;
import com.github.ibanetchep.artisanattribution.config.AttributionConfig;
import com.github.ibanetchep.artisanattribution.config.AttributionSource;
import com.github.ibanetchep.artisanattribution.storage.PlayerAttribution;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.action.ActionContext;
import net.artisanmc.modules.api.action.ActionDeclaration;
import net.artisanmc.modules.api.action.ActionRuntime;
import net.artisanmc.modules.api.datasource.DataSourceDeclaration;
import net.artisanmc.modules.api.datasource.DataSourceField;
import net.artisanmc.modules.api.datasource.Stability;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;
import net.artisanmc.modules.api.template.TemplateDeclaration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArtisanAttribution inside Artisan:
 * <ul>
 *   <li>the menu template {@code attribution}, copied once into a new project {@code attribution}
 *       on the first start, and offered to every project in the editor;</li>
 *   <li>the data source {@code attribution:sources}: the sources of config.yml, one row each;</li>
 *   <li>the action {@code attribution:choose}, which a button runs with the source id;</li>
 *   <li>the placeholders {@code %attribution_source%}, {@code %attribution_source_name%} and
 *       {@code %attribution_answered%}, also given to PlaceholderAPI.</li>
 * </ul>
 */
public final class AttributionModule implements ArtisanModule {

    public static final String ID = "attribution";
    static final String CHOOSE = "attribution:choose";
    static final String PLACEHOLDERS = "attribution";

    /** The shipped menu: raise {@link #TEMPLATE_VERSION} whenever its files change. */
    static final String TEMPLATE = "attribution";
    static final int TEMPLATE_VERSION = 1;
    static final String TEMPLATE_ROOT = "artisan-templates/attribution";
    static final List<String> TEMPLATE_FILES = List.of("menus/attribution.yaml");
    static final String SEED_PROJECT = "attribution";

    private final AttributionService service;
    private final Logger logger;
    private @Nullable ArtisanAPI api;

    public AttributionModule(AttributionService service, Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getSchemaVersion() {
        return "attribution/v1";
    }

    @Override
    public void onEnable(ArtisanAPI api) {
        this.api = api;
        registerTemplate(api);

        api.getActionRegistry().register(new ActionDeclaration(CHOOSE, List.of(
                new ModelField("source", ModelFieldType.string(), true)
                        .withHelp("Id of a source of config.yml (sources:), e.g. discord. "
                                + "In the shipped menu: {source.id}.")
        ), this::choose));

        // STATIC with a key: the rows are config.yml, the same for everyone, so a menu may also
        // pin one source on a chosen slot. Only `chosen` varies with the viewer.
        api.getDataSources().register(new DataSourceDeclaration(
                AttributionService.SOURCES_DATA_SOURCE,
                (player, params) -> sourceRows(player),
                List.of(
                        new DataSourceField("id", ModelFieldType.string(), "What attribution:choose takes"),
                        new DataSourceField("name", ModelFieldType.string(), "In the viewer's language"),
                        new DataSourceField("description", ModelFieldType.string(), "In the viewer's language, may be empty"),
                        new DataSourceField("material", ModelFieldType.string(), null),
                        new DataSourceField("count", ModelFieldType.integer(), "Players who picked it"),
                        new DataSourceField("chosen", ModelFieldType.bool(), "Is the viewer's answer")),
                Stability.STATIC,
                "id"));

        try {
            api.getPlaceholders().register(PLACEHOLDERS, (player, key) -> placeholder(player, key), true);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warning("The %" + PLACEHOLDERS + "_*% placeholders are not available: " + e.getMessage());
        }
    }

    /** The menu, offered in the editor and copied once into the project {@code attribution}. */
    private void registerTemplate(ArtisanAPI api) {
        boolean french = api.getTranslations().getDefaultLang().startsWith("fr");
        try {
            api.getTemplates().register(TemplateDeclaration
                    .fromResources(getClass().getClassLoader(), TEMPLATE_ROOT, TEMPLATE, TEMPLATE_VERSION, TEMPLATE_FILES)
                    .title(french ? "Menu d'attribution" : "Attribution menu")
                    .seed(SEED_PROJECT)
                    .build());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(Level.WARNING, "The attribution menu template is not available", e);
        }
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.getPlaceholders().unregister(PLACEHOLDERS);
        }
        api = null;
    }

    private ActionContext choose(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        String sourceId = runtime.text(ctx, args.get("source")).trim();
        if (sourceId.isEmpty()) {
            runtime.warn(CHOOSE + ": no source given");
            return ctx;
        }
        service.answer(ctx.getPlayer(), sourceId);
        return ctx;
    }

    /** Memory only: runs on the viewer's thread at every render. */
    private List<Map<String, Object>> sourceRows(@Nullable Player viewer) {
        AttributionConfig config = service.config();
        String lang = service.lang(viewer);
        String defaultLang = service.defaultLang();
        PlayerAttribution state = viewer != null ? service.state(viewer.getUniqueId()) : null;
        String chosen = state != null ? state.source() : null;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (AttributionSource source : config.orderedSources()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", source.id());
            row.put("name", source.name().resolve(lang, defaultLang));
            row.put("description", source.description().resolve(lang, defaultLang));
            row.put("material", source.material());
            row.put("count", service.count(source.id()));
            row.put("chosen", source.id().equals(chosen));
            rows.add(row);
        }
        return rows;
    }

    /** Null (the token stays as written) for an unknown key, an offline player, or one not loaded. */
    private @Nullable String placeholder(@Nullable Player player, String key) {
        if (player == null) return null;
        PlayerAttribution state = service.state(player.getUniqueId());
        if (state == null) return null;
        return switch (key) {
            case "source" -> state.source() != null ? state.source() : "";
            case "source_name" -> service.sourceName(player, state.source());
            case "answered" -> String.valueOf(state.answered());
            default -> null;
        };
    }
}

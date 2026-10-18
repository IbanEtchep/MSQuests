package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.suite.ActionArg;
import com.github.ibanetchep.msquests.suite.ActionCall;
import com.github.ibanetchep.msquests.suite.ActionSpec;
import net.artisanmc.modules.api.action.ActionContext;
import net.artisanmc.modules.api.action.ActionDeclaration;
import net.artisanmc.modules.api.action.ActionRuntime;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** The {@code quests:*} actions ({@link ActionSpec}) as Artisan's registry takes them. */
final class ArtisanActions {

    private ArtisanActions() {
    }

    static ActionDeclaration declaration(ActionSpec spec) {
        return new ActionDeclaration(spec.getName(), spec.getArgs().stream().map(ArtisanActions::field).toList(),
                spec.getPrimary(), (ctx, args, runtime) -> {
                    spec.getHandler().run(new Call(ctx, args, runtime));
                    return ctx;
                });
    }

    private static ModelField field(ActionArg arg) {
        ModelFieldType type = switch (arg.getType()) {
            case STRING -> ModelFieldType.string();
            case LTEXT -> ModelFieldType.ltext();
            case INTEGER -> ModelFieldType.integer();
            case BOOL -> ModelFieldType.bool();
            case ENUM -> ModelFieldType.enumOf(arg.getValues());
            case DOCUMENT_REF -> ModelFieldType.documentRef(arg.getDocumentKind());
        };
        ModelField field = new ModelField(arg.getName(), type, arg.getRequired());
        if (arg.getDefault() != null) field = field.withDefault(arg.getDefault());
        if (arg.getHelp() != null) field = field.withHelp(arg.getHelp());
        if (arg.getMin() != null) field = field.withRange(arg.getMin(), null);
        return field;
    }

    private record Call(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) implements ActionCall {
        @Override
        public @NotNull Player getPlayer() {
            return ctx.getPlayer();
        }

        @Override
        public @NotNull Map<String, Object> getArgs() {
            return args;
        }

        @Override
        public @NotNull Map<String, Object> getContextArgs() {
            return ctx.getArgs();
        }

        @Override
        public @NotNull String text(@Nullable Object value) {
            return runtime.text(ctx, value);
        }

        @Override
        public void warn(@NotNull String message) {
            runtime.warn(message);
        }
    }
}

package com.github.ibanetchep.msquests.core.quest.config.condition;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A list of Artisan conditions in their stored form (ADR {@code structured-action-wire}):
 * one-key maps {@code {<condition>: <args>}}, {@code or}/{@code and}/{@code not} nodes, all of
 * them required (implicit AND). The core never reads them: a {@link ConditionChecker} does.
 */
public record Conditions(List<Object> conditions) {

    public static final Conditions NONE = new Conditions(List.of());

    public Conditions {
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }

    /** Conditions from their wire value: a list, a single condition, or nothing. */
    public static Conditions of(@Nullable Object wire) {
        if (wire == null) return NONE;
        if (wire instanceof List<?> list) return list.isEmpty() ? NONE : new Conditions(List.copyOf(list));
        if (wire instanceof Map<?, ?>) return new Conditions(List.of(wire));
        throw new IllegalArgumentException("expected a list of conditions, got " + wire);
    }

    public boolean isEmpty() {
        return conditions.isEmpty();
    }
}

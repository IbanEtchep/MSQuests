package com.github.ibanetchep.msquests.core.quest.config.action;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * An Artisan action chain in its stored form (ADR {@code structured-action-wire}): a list of
 * steps, each a one-key map {@code {<action>: <args>}} or an {@code if/do/else} node. The core
 * never reads the steps: it only hands them, with a {@link ChainContext}, to the
 * {@link ChainRunner} of the platform, which runs them through Artisan.
 */
public record ActionChain(List<Object> steps) {

    public static final ActionChain EMPTY = new ActionChain(List.of());

    public ActionChain {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    /** A chain from its wire value: a list of steps, a single step, or nothing. */
    public static ActionChain of(@Nullable Object wire) {
        if (wire == null) return EMPTY;
        if (wire instanceof List<?> list) return list.isEmpty() ? EMPTY : new ActionChain(List.copyOf(list));
        if (wire instanceof Map<?, ?> || wire instanceof String) return new ActionChain(List.of(wire));
        throw new IllegalArgumentException("expected a list of actions, got " + wire);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }
}

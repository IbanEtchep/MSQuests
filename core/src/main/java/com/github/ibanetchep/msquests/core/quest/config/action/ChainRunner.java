package com.github.ibanetchep.msquests.core.quest.config.action;

/**
 * Runs an {@link ActionChain} for the members of an actor: the platform resolves who is online
 * and runs the chain once for each of them, with the bindings of the {@link ChainContext}. Safe
 * to call from any thread; the chain itself runs where the platform's action engine runs it.
 */
@FunctionalInterface
public interface ChainRunner {

    /** Runs nothing: tests, and a platform without an action engine. */
    ChainRunner NOOP = (chain, context) -> {};

    void run(ActionChain chain, ChainContext context);
}

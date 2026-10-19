package com.github.ibanetchep.artisanattribution.storage;

/** Outcome of {@link AttributionRepository#answer}. */
public enum AnswerResult {
    /** The answer is stored. */
    SAVED,
    /** The player had answered already (from another server, or a double click): nothing changed. */
    ALREADY_ANSWERED
}

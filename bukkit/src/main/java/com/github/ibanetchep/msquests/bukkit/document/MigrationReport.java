package com.github.ibanetchep.msquests.bukkit.document;

import java.util.ArrayList;
import java.util.List;

/** The text {@code /msquests migrate} writes to {@code migration/report-*.txt}. */
public final class MigrationReport {

    private final List<String> lines = new ArrayList<>();
    private int warnings;

    public MigrationReport section(String title) {
        if (!lines.isEmpty()) lines.add("");
        lines.add("== " + title + " ==");
        return this;
    }

    public MigrationReport line(String text) {
        lines.add(text);
        return this;
    }

    /** A line the admin must read before switching: something was approximated, left out or refused. */
    public MigrationReport warn(String text) {
        warnings++;
        lines.add("! " + text);
        return this;
    }

    public int warnings() {
        return warnings;
    }

    public String render() {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}

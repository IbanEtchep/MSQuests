package com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaceholderObjectiveConfigTest {

    private PlaceholderObjectiveConfig config(String value) {
        QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                "obj_placeholder", "placeholder",
                Map.of("placeholder", "levels_level", "value", value)
        );
        return new PlaceholderObjectiveConfig(dto);
    }

    // ── Numeric: >= ──────────────────────────────────────────────────────────

    @Test
    void greaterOrEqualReturnsTrueWhenExact() {
        assertTrue(config(">= 10").evaluateCondition("10"));
    }

    @Test
    void greaterOrEqualReturnsTrueWhenHigher() {
        assertTrue(config(">= 10").evaluateCondition("15"));
    }

    @Test
    void greaterOrEqualReturnsFalseWhenLower() {
        assertFalse(config(">= 10").evaluateCondition("9"));
    }

    // ── Numeric: <= ──────────────────────────────────────────────────────────

    @Test
    void lessOrEqualReturnsTrueWhenExact() {
        assertTrue(config("<= 5").evaluateCondition("5"));
    }

    @Test
    void lessOrEqualReturnsTrueWhenLower() {
        assertTrue(config("<= 5").evaluateCondition("3"));
    }

    @Test
    void lessOrEqualReturnsFalseWhenHigher() {
        assertFalse(config("<= 5").evaluateCondition("6"));
    }

    // ── Numeric: > ───────────────────────────────────────────────────────────

    @Test
    void strictGreaterReturnsFalseWhenEqual() {
        assertFalse(config("> 10").evaluateCondition("10"));
    }

    @Test
    void strictGreaterReturnsTrueWhenHigher() {
        assertTrue(config("> 10").evaluateCondition("11"));
    }

    // ── Numeric: < ───────────────────────────────────────────────────────────

    @Test
    void strictLessReturnsFalseWhenEqual() {
        assertFalse(config("< 5").evaluateCondition("5"));
    }

    @Test
    void strictLessReturnsTrueWhenLower() {
        assertTrue(config("< 5").evaluateCondition("4"));
    }

    // ── Numeric: == ──────────────────────────────────────────────────────────

    @Test
    void numericEqualityReturnsTrueWhenSame() {
        assertTrue(config("== 42").evaluateCondition("42"));
    }

    @Test
    void numericEqualityReturnsFalseWhenDifferent() {
        assertFalse(config("== 42").evaluateCondition("43"));
    }

    // ── Numeric: != ──────────────────────────────────────────────────────────

    @Test
    void numericNotEqualReturnsTrueWhenDifferent() {
        assertTrue(config("!= 0").evaluateCondition("1"));
    }

    @Test
    void numericNotEqualReturnsFalseWhenSame() {
        assertFalse(config("!= 0").evaluateCondition("0"));
    }

    // ── Decimal numbers ───────────────────────────────────────────────────────

    @Test
    void supportsDecimalComparison() {
        assertTrue(config(">= 1.5").evaluateCondition("2.0"));
        assertFalse(config(">= 1.5").evaluateCondition("1.4"));
    }

    // ── String comparisons ────────────────────────────────────────────────────

    @Test
    void stringEqualityReturnsTrueWhenSame() {
        assertTrue(config("== diamond").evaluateCondition("diamond"));
    }

    @Test
    void stringEqualityReturnsFalseWhenDifferent() {
        assertFalse(config("== diamond").evaluateCondition("gold"));
    }

    @Test
    void stringNotEqualReturnsTrueWhenDifferent() {
        assertTrue(config("!= none").evaluateCondition("diamond"));
    }

    @Test
    void stringNotEqualReturnsFalseWhenSame() {
        assertFalse(config("!= none").evaluateCondition("none"));
    }

    // ── No operator defaults to equality ─────────────────────────────────────

    @Test
    void noOperatorDefaultsToStringEquality() {
        assertTrue(config("hello").evaluateCondition("hello"));
        assertFalse(config("hello").evaluateCondition("world"));
    }

    // ── Whitespace tolerance ──────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            ">=10,  10",
            ">= 10, 10",
            ">=  10,10"
    })
    void handlesVariousWhitespaceAroundOperator(String value, String resolved) {
        assertTrue(config(value).evaluateCondition(resolved));
    }

    // ── Parameterized sweep ───────────────────────────────────────────────────

    @ParameterizedTest(name = "value=''{0}'' resolved=''{1}'' expect={2}")
    @CsvSource({
            ">= 10, 10,  true",
            ">= 10, 9,   false",
            "<= 5,  5,   true",
            "<= 5,  6,   false",
            "> 3,   4,   true",
            "> 3,   3,   false",
            "< 3,   2,   true",
            "< 3,   3,   false",
            "== 7,  7,   true",
            "== 7,  8,   false",
            "!= 0,  1,   true",
            "!= 0,  0,   false"
    })
    void numericConditionsSweep(String condition, String resolved, boolean expected) {
        PlaceholderObjectiveConfig cfg = config(condition);
        if (expected) {
            assertTrue(cfg.evaluateCondition(resolved.trim()));
        } else {
            assertFalse(cfg.evaluateCondition(resolved.trim()));
        }
    }
}

package com.github.ibanetchep.msquests.core.quest.config.group;

import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A group's period is asked for on every menu row, placeholder and refresh tick: the cron is
 * parsed once with the group and the window is only recomputed when a reset passes.
 */
class QuestGroupConfigPeriodTest {

    @Test
    void theSameExpressionIsParsedOnce() {
        assertSame(CronUtils.schedule("0 0 * * *"), CronUtils.schedule("0 0 * * *"));
        assertEquals(CronUtils.getNextExecution("0 0 * * *", Instant.EPOCH),
                CronUtils.getNextExecution(CronUtils.schedule("0 0 * * *"), Instant.EPOCH));
    }

    @Test
    void anInvalidCronIsRefusedWhenTheGroupIsBuilt() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestGroupConfig.Builder("g", "G", "", "player").resetCron("every day").build());
    }

    @Test
    void theCurrentPeriodRunsFromTheLastResetToTheNext() {
        QuestGroupConfig group = new QuestGroupConfig.Builder("daily", "Daily", "", "player")
                .resetCron("0 0 * * *")
                .build();
        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now();
        Instant midnight = LocalDate.now(zone).atStartOfDay(zone).toInstant();

        QuestGroupConfig.Period period = group.getCurrentPeriod();
        assertEquals(midnight, period.start());
        assertEquals(midnight.plus(1, ChronoUnit.DAYS), period.end());
        assertTrue(period.contains(now));
        assertFalse(period.contains(midnight.minusSeconds(1)));
        assertEquals(period.start(), group.getPeriodStart());
        assertEquals(period.end(), group.getPeriodEnd());
        assertEquals(period.end(), group.getNextReset());
        assertEquals(period.start(), group.getPreviousReset());

        // A quest created last period expires at this period's start.
        assertEquals(midnight, group.getNextResetAfter(midnight.minusSeconds(60)));
        assertEquals(midnight, group.getPreviousResetBefore(now));
        // The window is reused while now stays inside it.
        assertEquals(period, group.getCurrentPeriod());
    }

    @Test
    void withoutACronTheBoundsAreTheGroupDates() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");
        QuestGroupConfig group = new QuestGroupConfig.Builder("event", "Event", "", "player")
                .startAt(start).endAt(end)
                .build();
        assertEquals(new QuestGroupConfig.Period(start, end), group.getCurrentPeriod());
        assertNull(group.getNextReset());
        assertNull(group.getNextResetAfter(start));
        assertEquals(new QuestGroupConfig.Period(null, null),
                new QuestGroupConfig.Builder("free", "Free", "", "player").build().getCurrentPeriod());
    }

    @Test
    void anEndDateWinsOverTheCronForThePeriodEnd() {
        Instant end = Instant.parse("2030-01-01T00:00:00Z");
        QuestGroupConfig group = new QuestGroupConfig.Builder("season", "Season", "", "player")
                .resetCron("0 0 * * *").endAt(end)
                .build();
        assertEquals(end, group.getCurrentPeriod().end());
        assertEquals(group.getPreviousReset(), group.getCurrentPeriod().start());
    }
}

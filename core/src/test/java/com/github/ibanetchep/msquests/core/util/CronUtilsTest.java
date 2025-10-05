package com.github.ibanetchep.msquests.core.util;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

class CronUtilsTest {

    @Test
    void testGetNextExecutionWithMinuteCron() {
        String cronExpression = "0 * * * *";
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(now));
    }

    @Test
    void testGetNextExecutionWithHourlyCron() {
        String cronExpression = "0 * * * *";
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(now));
    }

    @Test
    void testGetNextExecutionWithDailyCron() {
        String cronExpression = "0 0 * * *";
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(now));
    }

    @Test
    void testGetNextExecutionWithWeeklyCron() {
        String cronExpression = "0 0 * * 1";
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(now));
    }

    @Test
    void testGetNextExecutionWithComplexCron() {
        String cronExpression = "30 10 * * *";
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(now));
    }

    @Test
    void testGetNextExecutionWithNullCronExpression() {
        String cronExpression = null;
        Instant now = Instant.now();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, now);

        assertNull(nextExecution);
    }

    @Test
    void testGetNextExecutionWithNullFrom() {
        String cronExpression = "0 0 * * *";
        Instant from = null;
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, from);

        assertNull(nextExecution);
    }

    @Test
    void testGetPreviousExecutionWithMinuteCron() {
        String cronExpression = "0 * * * *";
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNotNull(previousExecution);
        assertTrue(previousExecution.isBefore(now));
    }

    @Test
    void testGetPreviousExecutionWithHourlyCron() {
        String cronExpression = "0 * * * *";
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNotNull(previousExecution);
        assertTrue(previousExecution.isBefore(now));
    }

    @Test
    void testGetPreviousExecutionWithDailyCron() {
        String cronExpression = "0 0 * * *";
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNotNull(previousExecution);
        assertTrue(previousExecution.isBefore(now));
    }

    @Test
    void testGetPreviousExecutionWithWeeklyCron() {
        String cronExpression = "0 0 * * 1";
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNotNull(previousExecution);
        assertTrue(previousExecution.isBefore(now));
    }

    @Test
    void testGetPreviousExecutionWithComplexCron() {
        String cronExpression = "30 10 * * *";
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNotNull(previousExecution);
        assertTrue(previousExecution.isBefore(now));
    }

    @Test
    void testGetPreviousExecutionWithNullCronExpression() {
        String cronExpression = null;
        Instant now = Instant.now();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, now);

        assertNull(previousExecution);
    }

    @Test
    void testGetPreviousExecutionWithNullFrom() {
        String cronExpression = "0 0 * * *";
        Instant from = null;
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, from);

        assertNull(previousExecution);
    }

    @Test
    void testGetNextExecutionWithSpecificDate() {
        String cronExpression = "0 0 * * *";
        ZonedDateTime specificDate = ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneId.systemDefault());
        Instant from = specificDate.toInstant();
        Instant nextExecution = CronUtils.getNextExecution(cronExpression, from);

        assertNotNull(nextExecution);
        ZonedDateTime nextExecutionZoned = nextExecution.atZone(ZoneId.systemDefault());
        assertEquals(0, nextExecutionZoned.getHour());
        assertEquals(0, nextExecutionZoned.getMinute());
        assertEquals(specificDate.toLocalDate().plusDays(1), nextExecutionZoned.toLocalDate());
    }

    @Test
    void testGetPreviousExecutionWithSpecificDate() {
        String cronExpression = "0 0 * * *";
        ZonedDateTime specificDate = ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneId.systemDefault()); // 1er janvier 2023 à 10h
        Instant from = specificDate.toInstant();
        Instant previousExecution = CronUtils.getPreviousExecution(cronExpression, from);

        assertNotNull(previousExecution);
        ZonedDateTime previousExecutionZoned = previousExecution.atZone(ZoneId.systemDefault());
        assertEquals(0, previousExecutionZoned.getHour());
        assertEquals(0, previousExecutionZoned.getMinute());
        assertEquals(specificDate.toLocalDate(), previousExecutionZoned.toLocalDate());
    }

}

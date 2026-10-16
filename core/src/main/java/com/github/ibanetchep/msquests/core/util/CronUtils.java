package com.github.ibanetchep.msquests.core.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class CronUtils {
    public static Instant getNextExecution(String cronExpression, Instant from) {
        return getNextExecution(cronExpression, from, null);
    }

    public static Instant getPreviousExecution(String cronExpression, Instant from) {
        return getPreviousExecution(cronExpression, from, null);
    }

    /** @param zone the zone the cron is read in; null reads it in the server's zone, as before groups could name one */
    public static Instant getNextExecution(String cronExpression, Instant from, ZoneId zone) {
        if (cronExpression == null || from == null) {
            return null;
        }

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(cronExpression);

        ZonedDateTime fromZoned = from.atZone(zone != null ? zone : ZoneId.systemDefault());
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(fromZoned);

        return nextExecution.map(ZonedDateTime::toInstant).orElse(null);
    }

    public static Instant getPreviousExecution(String cronExpression, Instant from, ZoneId zone) {
        if (cronExpression == null || from == null) {
            return null;
        }

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(cronExpression);

        ZonedDateTime fromZoned = from.atZone(zone != null ? zone : ZoneId.systemDefault());
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Optional<ZonedDateTime> previousExecution = executionTime.lastExecution(fromZoned);

        return previousExecution.map(ZonedDateTime::toInstant).orElse(null);
    }
}

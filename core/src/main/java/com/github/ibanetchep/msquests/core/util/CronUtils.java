package com.github.ibanetchep.msquests.core.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UNIX cron evaluation. Parsing an expression builds a {@link CronDefinition}, a
 * {@link CronParser} and an {@link ExecutionTime}, which is far more expensive than the
 * evaluation itself, so each expression is parsed once and the schedule is kept: a group
 * asks for its period on every menu render, every placeholder and every refresh tick.
 */
public class CronUtils {

    private static final CronDefinition DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
    private static final CronParser PARSER = new CronParser(DEFINITION);
    private static final Map<String, ExecutionTime> SCHEDULES = new ConcurrentHashMap<>();

    /**
     * The schedule of an expression, parsed once per distinct expression.
     * @throws IllegalArgumentException when the expression is not a valid UNIX cron
     */
    public static ExecutionTime schedule(String cronExpression) {
        return SCHEDULES.computeIfAbsent(cronExpression, expression -> {
            Cron cron = PARSER.parse(expression);
            return ExecutionTime.forCron(cron);
        });
    }

    public static Instant getNextExecution(String cronExpression, Instant from) {
        if (cronExpression == null || from == null) {
            return null;
        }
        return getNextExecution(schedule(cronExpression), from);
    }

    public static Instant getPreviousExecution(String cronExpression, Instant from) {
        if (cronExpression == null || from == null) {
            return null;
        }
        return getPreviousExecution(schedule(cronExpression), from);
    }

    public static Instant getNextExecution(ExecutionTime schedule, Instant from) {
        if (schedule == null || from == null) {
            return null;
        }
        ZonedDateTime fromZoned = from.atZone(ZoneId.systemDefault());
        return schedule.nextExecution(fromZoned).map(ZonedDateTime::toInstant).orElse(null);
    }

    public static Instant getPreviousExecution(ExecutionTime schedule, Instant from) {
        if (schedule == null || from == null) {
            return null;
        }
        ZonedDateTime fromZoned = from.atZone(ZoneId.systemDefault());
        return schedule.lastExecution(fromZoned).map(ZonedDateTime::toInstant).orElse(null);
    }
}

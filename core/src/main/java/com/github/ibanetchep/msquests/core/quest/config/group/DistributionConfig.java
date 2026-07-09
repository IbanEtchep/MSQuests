package com.github.ibanetchep.msquests.core.quest.config.group;

import java.util.Collections;
import java.util.Set;

public class DistributionConfig {

    private final QuestDistributionStrategy strategy;
    private final int amount;
    private final Set<DistributionTrigger> triggers;

    public DistributionConfig(QuestDistributionStrategy strategy, int amount, Set<DistributionTrigger> triggers) {
        this.strategy = strategy;
        this.amount = amount;
        this.triggers = triggers;
    }

    public QuestDistributionStrategy getStrategy() {
        return strategy;
    }

    public int getAmount() {
        return amount;
    }

    public Set<DistributionTrigger> getTriggers() {
        return Collections.unmodifiableSet(triggers);
    }

    public boolean hasTrigger(DistributionTrigger trigger) {
        return triggers.contains(trigger);
    }

}

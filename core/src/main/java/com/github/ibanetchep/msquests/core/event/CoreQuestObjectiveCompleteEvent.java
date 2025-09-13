package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.QuestObjective;

public class CoreQuestObjectiveCompleteEvent extends CoreEvent {

    private final QuestObjective<?> objective;

    public CoreQuestObjectiveCompleteEvent(QuestObjective<?> objective) {
        this.objective = objective;
    }

    public QuestObjective<?> getObjective() {
        return objective;
    }
}

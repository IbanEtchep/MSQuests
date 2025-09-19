package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

public class ActorPlaceholderResolver implements PlaceholderResolver<QuestActor> {

    @Override
    public String resolve(String template, QuestActor actor) {
        return template
                .replace("%actor_name%", actor.getName())
                .replace("%actor_type%", actor.getActorType());
    }
}

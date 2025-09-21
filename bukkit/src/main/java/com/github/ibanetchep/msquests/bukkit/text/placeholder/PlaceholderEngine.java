package com.github.ibanetchep.msquests.bukkit.text.placeholder;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver.*;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderEngine {

    private static final PlaceholderEngine INSTANCE = new PlaceholderEngine();

    private final Map<Class<?>, List<PlaceholderResolver<?>>> resolvers = new HashMap<>();

    private PlaceholderEngine() {
        registerDefaults();
    }

    public static PlaceholderEngine getInstance() {
        return INSTANCE;
    }

    private void registerDefaults() {
        registerResolver(QuestConfig.class, new QuestConfigPlaceholderResolver());
        registerResolver(Quest.class, new QuestPlaceholderResolver());
        registerResolver(QuestGroupConfig.class, new GroupPlaceholderResolver());
        registerResolver(QuestObjective.class, new ObjectivePlaceholderResolver());
        registerResolver(QuestActor.class, new ActorPlaceholderResolver());
        registerResolver(Player.class, new PlayerPlaceholderResolver());
    }

    public <T> void registerResolver(Class<? super T> type, PlaceholderResolver<? super T> resolver) {
        resolvers.computeIfAbsent(type, k -> new ArrayList<>()).add(resolver);
    }

    @SuppressWarnings("unchecked")
    public <T> String apply(String template, T object) {
        if (template == null || object == null) return template;
        String result = template;

        Class<?> clazz = object.getClass();
        boolean resolved = false;

        while (clazz != null && !resolved) {
            // Verify the class
            List<PlaceholderResolver<?>> list = resolvers.get(clazz);
            if (list != null) {
                for (PlaceholderResolver<?> resolver : list) {
                    result = ((PlaceholderResolver<T>) resolver).resolve(result, object);
                }
                break;
            }

            // Verify parent classes
            for (Class<?> iface : clazz.getInterfaces()) {
                list = resolvers.get(iface);
                if (list != null) {
                    for (PlaceholderResolver<?> resolver : list) {
                        result = ((PlaceholderResolver<T>) resolver).resolve(result, object);
                    }
                    resolved = true;
                    break;
                }
            }

            clazz = clazz.getSuperclass();
        }

        return result;
    }
}

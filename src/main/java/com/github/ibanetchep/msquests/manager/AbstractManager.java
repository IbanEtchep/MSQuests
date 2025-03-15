package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract manager class for handling objects of type T.
 *
 * @param <T> the type of objects managed by this manager.
 */
public abstract class AbstractManager<T> {

    protected final MSQuestsPlugin plugin;
    protected final Map<UUID, T> objects = new ConcurrentHashMap<>();

    public AbstractManager(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public T get(UUID uniqueId) {
        return objects.get(uniqueId);
    }

    public void put(UUID uniqueId, T object) {
        objects.put(uniqueId, object);
    }

    public void remove(UUID uniqueId) {
        objects.remove(uniqueId);
    }

    public boolean contains(UUID uniqueId) {
        return objects.containsKey(uniqueId);
    }

    public Map<UUID, T> getAll() {
        return Collections.unmodifiableMap(objects);
    }

    public abstract Map<UUID, T> loadAllFromDatabase();

    public abstract T loadFromDatabase(UUID uniqueId);

    public abstract void saveToDatabase(T object);

    public abstract void deleteFromDatabase(UUID uniqueId);

    public abstract void sync(T object);
}

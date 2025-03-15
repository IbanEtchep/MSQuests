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
public abstract class AbstractManager<K, T> {

    protected final MSQuestsPlugin plugin;
    protected final Map<K, T> objects = new ConcurrentHashMap<>();

    public AbstractManager(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public T get(K id) {
        return objects.get(id);
    }

    public void put(K id, T object) {
        objects.put(id, object);
    }

    public void remove(K id) {
        objects.remove(id);
    }

    public boolean contains(K id) {
        return objects.containsKey(id);
    }

    public Map<K, T> getAll() {
        return Collections.unmodifiableMap(objects);
    }

    public abstract Map<UUID, T> loadAllFromDatabase();

    public abstract T loadFromDatabase(K id);

    public abstract void saveToDatabase(T object);

    public abstract void deleteFromDatabase(K id);

    public abstract void sync(T object);
}

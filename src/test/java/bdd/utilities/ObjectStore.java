package bdd.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides a thread-safe store for managing objects across different parts of the application.
 * This class uses a singleton pattern to ensure that only one instance of the store exists
 * throughout the application lifecycle, providing a centralized point for accessing and
 * managing shared objects identified by unique keys.
 */
public class ObjectStore {

    private static final Map<String, Object> objectsMap = new ConcurrentHashMap<>(20,0.9f,8);

    private static final ObjectStore instance = new ObjectStore();

    private ObjectStore() {}


    /**
     * Retrieves the single instance of the ObjectStore.
     *
     * @return The singleton instance of ObjectStore.
     */
    public static ObjectStore getInstance() {
        return instance;
    }


    /**
     * Stores an object in the store, associated with a specific key.
     *
     * @param key The key with which the specified object is to be associated.
     * @param value The object to be stored.
     */
    public void putObject(String key, Object value) {
        objectsMap.put(key, value);
    }


    /**
     * Retrieves an object from the store by its key.
     *
     * @param key The key whose associated object is to be returned.
     * @return The object associated with the specified key, or {@code null} if no object
     *         is mapped under the key.
     */
    public Object getObject(String key) {
        return objectsMap.get(key);
    }


    /**
     * Removes an object from the store that is associated with a specific key.
     *
     * @param key The key whose associated object is to be removed.
     */
    public void removeObject(String key) {
        objectsMap.remove(key);
    }
}

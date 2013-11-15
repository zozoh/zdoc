package org.nutz.cache;

import java.util.HashMap;
import java.util.Map;

public class ZCache<T> {

    private Map<String, T> map;

    public ZCache() {
        map = new HashMap<String, T>();
    }

    public T get(String key) {
        return map.get(key);
    }

    public ZCache<T> set(String key, T val) {
        map.put(key, val);
        return this;
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public ZCache<T> clear() {
        map.clear();
        return this;
    }

    public T remove(String key) {
        return map.remove(key);
    }

}

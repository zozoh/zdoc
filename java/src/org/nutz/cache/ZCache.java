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

    public boolean match(String key) {
        return map.containsKey(key);
    }

    public T remove(String key) {
        return map.remove(key);
    }

}

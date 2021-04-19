package com.ggar.gdrive.framework.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractContainer<K,V> implements Container<K,V> {

    protected final Map<K,V> container;

    public AbstractContainer() {
        this(new ConcurrentHashMap<K,V>());
    }

    public AbstractContainer(Map<K, V> map) {
        this.container = map;
    }

    @Override
    public V register(K key, V value) {
        return (V) container.put(key, value);
    }

    @Override
    public V unregister(K key) {
        return (V) (contains(key) ? container.put(key, null) : null);
    }

    @Override
    public V unregister(K key, V entity) {
        return (V) (contains(key) ? container.remove(key) : null);
    }

    @Override
    public Boolean contains(K key) {
        return container.containsKey(key);
    }

    @Override
    public V get(final K key) {
        return container.get(key);
    }

    @Override
    public List<K> keys() {
        return new ArrayList<K>(){{
            Iterator<K> it = container.keySet().iterator();
            while (it.hasNext()) {
                add(it.next());
            }
        }};
    }
}

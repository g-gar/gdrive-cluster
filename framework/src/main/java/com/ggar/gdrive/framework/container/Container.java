package com.ggar.gdrive.framework.container;

import java.util.List;

public interface Container<K,V> {
    V register(K key, V value);
    V unregister(K key, V value);
    V unregister(K key);
    V get(K key);
    Boolean contains(K key);
    List<K> keys();
}

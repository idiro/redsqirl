package com.redsqirl.useful;

import java.io.Serializable;
import java.util.Map;

public final class RedSqirlEntry<K, V> implements Map.Entry<K, V>, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5213931787540484493L;
	
	private final K key;
    private V value;

    public RedSqirlEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
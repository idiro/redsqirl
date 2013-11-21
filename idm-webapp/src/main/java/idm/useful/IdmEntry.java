package idm.useful;

import java.io.Serializable;
import java.util.Map;

public final class IdmEntry<K, V> implements Map.Entry<K, V>, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5213931787540484493L;
	
	private final K key;
    private V value;

    public IdmEntry(K key, V value) {
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
package org.alfresco.support.jsondiffer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DoubleKeyMap<K1, K2, V extends Comparable<V>> implements Map<Entry<K1, K2>, V> {

	private final Map<Entry<K1, K2>, V> map;
	private final Map<K1, V> k1map;
	private final Map<K2, V> k2map;
	
	public DoubleKeyMap() {
		this.map = new HashMap<>();
		this.k1map = new HashMap<>();
		this.k2map = new HashMap<>();
	}
	
	public DoubleKeyMap(int initialCapacity) {
		this.map = new HashMap<>(initialCapacity);
		this.k1map = new HashMap<>(initialCapacity);
		this.k2map = new HashMap<>(initialCapacity);
	}
	
	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	@Override
	public int size() {
		return this.map.size();
	}
	
	@Override
	public Collection<V> values() {
		return this.map.values();
	}
	
	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (key instanceof Entry) {
			return this.map.containsKey(key);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public boolean containsKey1(K1 key1, K2 key2) {
		return this.map.containsKey(new SimpleMapEntry<K1, K2>(key1, key2));
	}
	
	public boolean containsKey1(K1 key) {
		return this.k1map.containsKey(key);
	}
	
	public boolean containsKey2(K2 key) {
		return this.k2map.containsKey(key);
	}
	
	@Override
	public V get(Object key) {
		if (key instanceof Entry) {
			return this.map.get(key);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public V getByKeys(K1 key1, K2 key2) {
		return this.map.get(new SimpleMapEntry<K1, K2>(key1, key2));
	}

	public V getByKey1(K1 key) {
		return this.k1map.get(key);
	}

	public V getByKey2(K2 key) {
		return this.k2map.get(key);
	}
	
	@Override
	public void clear() {
		this.map.clear();
		this.k1map.clear();
		this.k2map.clear();
	}
	
	@Override
	public V remove(Object key) {
		return this.map.remove(key);
	}
	
	public V removeByKeys(K1 key1, K2 key2) {
		return this.map.remove(new SimpleMapEntry<K1, K2>(key1, key2));
	}
	
	public V removeByKey1(K1 key) {
		return this.k1map.remove(key);
	}
	
	public V removeByKey2(K2 key) {
		return this.k2map.remove(key);
	}
	
	@Override
	public V put(Entry<K1, K2> key, V value) {
		this.k1map.put(key.getKey(), value);
		this.k2map.put(key.getValue(), value);
		return this.map.put(key, value);
	}
	
	public V put(K1 key1, K2 key2, V value) {
		this.k1map.put(key1, value);
		this.k2map.put(key2, value);
		return this.map.put(new SimpleMapEntry<K1, K2>(key1, key2), value);
	}
	
	@Override
	public void putAll(Map<? extends Entry<K1, K2>, ? extends V> m) {
		for (Entry<? extends Entry<K1, K2>, ? extends V> entry : m.entrySet()) {
			this.map.put(entry.getKey(), entry.getValue());
			this.k1map.put(entry.getKey().getKey(), entry.getValue());
			this.k2map.put(entry.getKey().getValue(), entry.getValue());
		}
	}
	
	public List<Entry<Entry<K1, K2>, V>> entryList() {
		List<Entry<Entry<K1, K2>, V>> list = new LinkedList<>(this.entrySet());
		Collections.sort(list, new Comparator<Entry<Entry<K1, K2>, V>>() {
			@Override
			public int compare(Entry<Entry<K1, K2>, V> o1, Entry<Entry<K1, K2>, V> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return list;
	}
	
	@Override
	public Set<Entry<Entry<K1, K2>, V>> entrySet() {
		return this.map.entrySet();
	}
	
	@Override
	public Set<Entry<K1, K2>> keySet() {
		return this.map.keySet();
	}

	public Set<K1> key1Set() {
		return this.k1map.keySet();
	}

	public Set<K2> key2Set() {
		return this.k2map.keySet();
	}
	
	@Override
	public String toString() {
		return this.map.toString();
	}
	
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DoubleKeyMap))
			return false;

		DoubleKeyMap<K1, K2, V> map = (DoubleKeyMap<K1, K2, V>)obj;
		return this.map.equals(map.map);
	}

}

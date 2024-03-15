package com.playsawdust.glow;

import java.util.LinkedHashMap;

/**
 * Trivial LRU cache implemented on top of LinkedHashMap, so I stop re-implementing it.
 */
public class Cache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	
	private int targetSize;
	
	public Cache(int targetSize) {
		super(16, 0.75f, true);
		this.targetSize = targetSize;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return this.size() > targetSize;
	}
}

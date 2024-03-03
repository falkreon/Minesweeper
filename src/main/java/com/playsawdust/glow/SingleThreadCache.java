package com.playsawdust.glow;

import java.util.HashMap;
import java.util.Map;

public class SingleThreadCache<K, V> {
	private int targetSize;
	private long evictTime;
	private long lastTime;
	private boolean enableFullEvict = false;
	private HashMap<K, Entry<V>> data = new HashMap<>();
	
	public SingleThreadCache(int targetSize) {
		this.targetSize = targetSize;
	}
	
	/**
	 * Does not actually check for evictions, just supplies this cache with a timer
	 */
	public void tick(long time) {
		lastTime = time;
		enableFullEvict = true;
	}
	
	private void evict(long currentTime, int evictCount) {
		//Iterator<>
		for(Map.Entry<K, Entry<V>> entry : data.entrySet()) {
			Entry<V> ev = entry.getValue();
			long deltaTime = currentTime - ev.lastAccess;
			if (deltaTime > evictTime) {
				
			}
		}
	}

	private static class Entry<V> {
		public long lastAccess;
		public V value;
	}
}

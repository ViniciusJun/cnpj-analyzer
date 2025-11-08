package com.novasemp.cnpj.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static CacheManager instance;
    private Map<String, CacheEntry> cache = new HashMap<>();
    private long defaultTtl = TimeUnit.MINUTES.toMillis(30); // 30 minutos
    
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    
    public void put(String key, Object value) {
        put(key, value, defaultTtl);
    }
    
    public void put(String key, Object value, long ttl) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl));
    }
    
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.isValid()) {
            return entry.getValue();
        }
        cache.remove(key);
        return null;
    }
    
    public void clear() {
        cache.clear();
    }
    
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
    
    private static class CacheEntry {
        private Object value;
        private long expiresAt;
        
        public CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
        
        public Object getValue() { return value; }
        public boolean isValid() { return System.currentTimeMillis() < expiresAt; }
    }
}
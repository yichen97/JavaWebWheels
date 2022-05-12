package com.example.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 利用LinkedList 实现，只需按照逻辑重写 removeEldestEntry方法。
 */
public class LRUCache implements Cache{
    private Map<String, String> map;
    private int size;

    LRUCache(){}

    public LRUCache(int capacity){
        this.size = capacity;
        map = new LinkedHashMap(capacity, 0.75F, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return map.size() > size;
            }
        };
    }

    @Override
    public boolean isExit(String key) {
        return this.map.containsKey(key);
    }

    @Override
    public String get(String key) {
        String value = map.get(key);
        map.remove(key);
        map.put(key, value);
        return value;
    }

    @Override
    public int add(String key, String value) {
        map.put(key, value);
        return 1;
    }

    @Override
    public int remove(String key) {
        map.remove(key);
        return 1;
    }

    @Override
    public String printMap(){
        String str = "现在缓存中的顺序是： \n";
        for(Map.Entry<String, String> e : map.entrySet()){
            str += "Key: " + e.getKey() + "-" + "Value: " + e.getValue() + "\n";
        }
        return str;
    }


}

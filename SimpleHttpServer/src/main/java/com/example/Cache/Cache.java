package com.example.Cache;

public interface Cache {
    //缓存中是否存在该Key
    public boolean isExit(String key);

    //通过key取得缓存值
    public String get(String key);

    //添加缓存
    public int add(String key, String value);

    //删除缓存
    public int remove(String key);

    //打印缓存，测试用
    public String printMap();
}

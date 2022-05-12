import com.example.Cache.Cache;
import com.example.Cache.LRUCache;

import java.util.Map;

public class TestLRU {
    public static void main(String[] args) {
        Cache cache = new LRUCache(3);
        cache.add("1", "a");
        cache.add("2", "b");
        cache.add("3", "c");
        System.out.println(cache.printMap());
        System.out.println("键1 对应的值是否存在 ：" + cache.isExit("1"));
        System.out.println("[查询操作] 键2 对应的值是 ：" + cache.get("2"));
        System.out.println(cache.printMap());
        System.out.println("删除键3 对应的缓存");
        cache.remove("3");
        System.out.println(cache.printMap());
        cache.add("4", "d");
        cache.add("5", "e");
        cache.add("6", "f");
        System.out.println(cache.printMap());
    }
}

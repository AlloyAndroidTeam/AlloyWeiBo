package com.alloyteam.weibo.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DataManager {
	public static Map<String, List<Weibo>> listMap = Collections
			.synchronizedMap(new WeakHashMap<String, List<Weibo>>());
    public static List<Weibo> get(String id) {
        try {
                if (!listMap.containsKey(id))
                        return null;
                return listMap.get(id);
        } catch (NullPointerException ex) {
                return null;
        }
    }
    
    public static void set(String id, List<Weibo> list){
        try {
            listMap.put(id, list);
	    } catch (Throwable th) {
	            th.printStackTrace();
	    }
    }
}

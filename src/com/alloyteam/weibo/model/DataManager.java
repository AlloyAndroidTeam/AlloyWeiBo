package com.alloyteam.weibo.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DataManager {
	public static Map<String, List<Weibo2>> listMap = Collections
			.synchronizedMap(new WeakHashMap<String, List<Weibo2>>());
    public static List<Weibo2> get(String id) {
        try {
                if (!listMap.containsKey(id))
                        return null;
                return listMap.get(id);
        } catch (NullPointerException ex) {
                return null;
        }
    }
    
    public static void set(String id, List<Weibo2> list){
        try {
            listMap.put(id, list);
	    } catch (Throwable th) {
	            th.printStackTrace();
	    }
    }
}

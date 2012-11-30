
/**
 * 微博听众管理model
 * 分不同微博
 */


package com.alloyteam.weibo.model;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;



public class Listeners {
	private static HashMap<String,HashMap<String, JSONObject>> hashList = 
			new HashMap<String,HashMap<String, JSONObject>>(); //key为微博type
	
	/**
	 * 添加数据到列表中保存起来
	 * @param type
	 * @param page 即startIndex
	 * @param object
	 */
	public static void add(int type, int page, JSONObject json){
		HashMap<String, JSONObject> h;
		h = hashList.get(getWeiboType(type));
		if (h == null){
			h = new  HashMap<String, JSONObject>();			
			hashList.put(getWeiboType(type), h);
		}
		if (h.get(getPageKey(page)) == null){
			h.put(getPageKey(page), json);
		}
	}
	/**
	 * 把微博类型整型专为字符串key
	 * @param tpye
	 * @return
	 */
	private static String getWeiboType(int type){
		return "weibo_" + type;
	}
	
	private static String getPageKey(int page){
		return "page_" + page;
	}
	
	/**
	 * 根据微博类型、听众name，返回指定的name的信息
	 * @param type
	 * @param name
	 * @return
	 */
	public static JSONObject get(int type, int page){
		HashMap<String, JSONObject> h;
		h = hashList.get(getWeiboType(type));
		if (h == null){
			return null;
		}
		return h.get(getPageKey(page));		
	}
	
}

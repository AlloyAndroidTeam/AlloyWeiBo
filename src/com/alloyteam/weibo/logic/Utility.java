/**
 * @author azraellong
 * @date 2012-11-16
 */
package com.alloyteam.weibo.logic;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.os.Bundle;

/**
 * @author azraellong
 *
 */
public class Utility {
	
	public static Bundle parseUrl(String url){
		return parseUrl(url, "?");
	}
	public static Bundle parseUrl(String url, String queryStart){
		Bundle values = new Bundle();
		int index = url.indexOf(queryStart);
		if(index == -1){
			return values;
		}
		url = url.substring(index + 1);
		String [] arr = url.split("&");
		for (String str : arr) {
			String [] kv = str.split("=");
			String key, value;
			if(kv.length == 1){
				key = kv[0];
				value = "";
			}else if(kv.length > 1){
				key = kv[0];
				value = kv[1];
			}else{
				continue;
			}
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			values.putString(key, value);
		}
		return values;
	}
	
}

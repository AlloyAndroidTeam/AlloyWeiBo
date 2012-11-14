/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.alloyteam.net.HttpConnection;

/**
 * @author azraellong
 * 
 */
public class ApiManager {

	public static int SINA = 1;

	public static int TENCENT = 2;

	public static Map<String, String> SINA_API_LIST;

	/**
	 * 调用微博 api
	 * 
	 */
	public static void call(String apiName) {
		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("test", "qq"));
		new HttpConnection().post("/api/xxx", data,
				new HttpConnection.HttpConnectionListener() {
					@Override
					public void onResponse(boolean success, String result) {
						Assert.assertEquals(true, success);
					}
				});
	}

}

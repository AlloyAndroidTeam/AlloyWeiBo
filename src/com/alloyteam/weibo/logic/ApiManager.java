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

import android.os.Handler;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 * 
 */
public class ApiManager {


	
	/**
	 * 调用微博 api
	 * 
	 */
	public static void call(Account account, String apiUrl, ArrayList<NameValuePair> params, Handler handler) {
//		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
//		data.add(new BasicNameValuePair("test", "qq"));
//		new HttpConnection().post("/api/xxx", data,
//				new HttpConnection.HttpConnectionListener() {
//					@Override
//					public void onResponse(boolean success, String result) {
//						Assert.assertEquals(true, success);
//					}
//				});
	}
	

}

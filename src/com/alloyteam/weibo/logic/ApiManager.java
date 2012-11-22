/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alloyteam.weibo.AuthActivity;
import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo;

/**
 * @author azraellong
 * 
 */
public class ApiManager {
	
	public static String TAG = "ApiManager";
	public interface IApiListener {

		public void onComplete(JSONObject result);

		public void onJSONException(JSONException exception);

		public void onFailure(String msg);

	}

	private static Context apiContext;

	public static void init(Context context) {
		apiContext = context;
	}

	// private static ArrayList<RequestObject> requestQueue;

	/**
	 * 同步调用微博 api
	 * 
	 * @param account
	 * @param url
	 * @param params
	 * @param method
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject request(Account account, String url,
			Bundle params, String method) throws JSONException, IOException {

		params = fillParams(account, params);

		String result;
		JSONObject jsonObject;
		try {
			result = Utility.request(url, method, params);

			try {
				jsonObject = new JSONObject(result);
			} catch (JSONException e) {
				e.printStackTrace();
				throw e;
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return jsonObject;
	}

	/**
	 * 异步调用微博 api
	 * 
	 * @param account
	 * @param url
	 * @param params
	 * @param method
	 * @param listener
	 */
	public static void requestAsync(final Account account, final String url,
			final Bundle params, final String method,
			final IApiListener listener) {
		
		if (!account.isValid() /*|| true*/) {
			// if (requestQueue == null) {
			// requestQueue = new ArrayList<RequestObject>();
			// }
			// RequestObject requestObject = new RequestObject();
			// requestObject.account = account;
			// requestObject.url = url;
			// requestObject.params = params;
			// requestObject.method = method;
			// requestObject.listener = listener;
			// requestQueue.add(requestObject);

			Intent intent = new Intent(apiContext, AuthActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("uid", account.uid);
			intent.putExtra("type", account.type);
			Toast.makeText(apiContext, "该帐号(" + account.uid + ")绑定已失效，请重新绑定",
					Toast.LENGTH_SHORT).show();
			apiContext.startActivity(intent);
			return;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String result = "no data";
				try {
					Bundle data = msg.getData();
					result = data.getString("result");
					boolean success = !result.equals("fail");
					if (success) {
						JSONObject jsonObject = new JSONObject(result);
						if (listener != null) {
							listener.onComplete(jsonObject);
						}
					} else {
						if (listener != null) {
							listener.onFailure("http error");
						}
					}

				} catch (JSONException e) {
					if (listener != null) {
						listener.onJSONException(e);
					}
					Log.d(TAG, result);
					e.printStackTrace();
				}
			}

		};
		new Thread() {
			@Override
			public void run() {
				Bundle dataParams = fillParams(account, params);
				String result = "fail";
				try {
					result = Utility.request(url, method, dataParams);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message message = Message.obtain(handler, 1);
				Bundle data = new Bundle();
				data.putString("result", result);
				message.setData(data);
				handler.sendMessage(message);
			}
		}.start();

	}

	/**
	 * 填充授权信息
	 * 
	 * @param account
	 * @param bundle
	 * @return
	 */
	private static Bundle fillParams(Account account, Bundle bundle) {
		if (bundle == null) {
			bundle = new Bundle();
		}
		bundle.putString("oauth_version", "2.a");
		bundle.putString("scope", "all");
		bundle.putString("clientip", "127.0.0.1");
		bundle.putString("oauth_consumer_key", Constants.Tencent.APP_KEY);
		bundle.putString("access_token", account.accessToken);
		bundle.putString("openid", account.openId);
		return bundle;
	}
	
	
	/**
	 * post带文件
	 */
	public static void postAsync(final Account account, final String url,
			final Bundle params, final String filePath,
			final IApiListener listener) {
		
		final Handler handler = new Handler() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				try {
					Bundle data = msg.getData();
					String result = data.getString("result");
					boolean success = !"fail".equals(result);
					if (success) {
						JSONObject jsonObject = new JSONObject(result);
						if (listener != null) {
							listener.onComplete(jsonObject);
						}
					} else {
						if (listener != null) {
							listener.onFailure("http error");
						}
					}

				} catch (JSONException e) {
					if (listener != null) {
						listener.onJSONException(e);
					}
					e.printStackTrace();
				}
			}

		};
		new Thread() {
			@Override
			public void run() {
				Bundle dataParams = fillParams(account, params);
				String result = "fail";
				
				try {
					result = Utility.postWithFile(url, dataParams,  filePath);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Message message = Message.obtain(handler, 1);
				Bundle data = new Bundle();
				data.putString("result", result);
				message.setData(data);
				handler.sendMessage(message);
			}
		}.start();

	}
	public static List<Weibo> JsonArrayToWeiboList(JSONArray info) throws JSONException{
		List<Weibo> tmpList=new ArrayList<Weibo>();
		for (int i = 0; i < info.length(); ++i) {
			JSONObject item = info.getJSONObject(i);
			int status=item.getInt("status");
			if(status!=0){
				continue;
			}
			tmpList.add(JsonToWeibo(item));
		}
		return tmpList;
	}
	public static Weibo JsonToWeibo(JSONObject item) throws JSONException{
		String text = item.getString("text");
		String name = item.getString("name");						
		String avatarUrl = item.getString("head");
		int type = item.getInt("type");
		Weibo weibo = new Weibo();
		weibo.text=text;
		weibo.name=name;
		weibo.avatarUrl=avatarUrl;
		long timestamp = item.getLong("timestamp");
		weibo.type = type;
		weibo.timestamp = timestamp;
		weibo.id=item.getString("id");
		if (type == 2) {
			JSONObject source = item
					.getJSONObject("source");
			String text2 = source.getString("text");
			String name2 = source.getString("name");
			String avatarUrl2 = source
					.getString("head");
			weibo.text2 = text2;
			weibo.avatarUrl2 = avatarUrl2;
			weibo.name2 = name2;
			if (source.get("image") != JSONObject.NULL) {
				Log.d("my", "image");
				JSONArray images = source
						.getJSONArray("image");
				weibo.imageUrl = images.getString(0);
			}
			weibo.count = item.getInt("count");
		} else {
			if (item.get("image") != JSONObject.NULL) {
				Log.d("my", "image");
				JSONArray images = item
						.getJSONArray("image");
				weibo.imageUrl = images.getString(0);
			}
		}
		return weibo;
	}
	/**
	 * 获取主页时间线
	 */
	public static void getHomeLine(Account account,int pageflag,long timeStamp,final GetListListener listener){
		Bundle params = new Bundle();
		params.putInt("pageflag", pageflag);
		params.putLong("pagetime", timeStamp);
		params.putInt("reqnum", 10);
		params.putInt("type", 0);
		params.putInt("contenttype", 0);
		params.putString("format", "json");
		params.putLong("t", System.currentTimeMillis());
		ApiManager.IApiListener httpListener=new ApiManager.IApiListener() {

			@Override
			public void onJSONException(JSONException exception) {
				listener.onError(0);
			}

			public void onFailure(String msg) {
				listener.onError(1);
			}

			@Override
			public void onComplete(JSONObject result) {
				try {
					if (result.get("data") == JSONObject.NULL){
						listener.onSuccess(null);
					}
					else{
						List<Weibo> tmpList=new ArrayList<Weibo>();
						JSONObject data = result.getJSONObject("data");
						JSONArray info = data.getJSONArray("info");
						tmpList=JsonArrayToWeiboList(info);
						listener.onSuccess(tmpList);
					}
					
				} catch (JSONException je) {
					Log.d("json", "error");
					listener.onError(0);
				}
			}
		};
		requestAsync(account, Constants.Tencent.HOME_TIMELINE,
				params, "GET", httpListener);
	}
	
	public static interface GetListListener{
		void onSuccess(List<Weibo> list);
		void onError(int type);
	}
	/**
	 * 获取主页时间线
	 */
	public static void getCommentList(Account account,String id,int pageflag,long timeStamp,final GetListListener listener){
		Bundle params = new Bundle();
		params.putInt("pageflag", pageflag);
		params.putLong("pagetime", timeStamp);
		params.putInt("reqnum", 10);
		params.putString("rootid", id);
		params.putInt("twitterid", 0);
		params.putString("format", "json");
		params.putLong("t", System.currentTimeMillis());
		ApiManager.IApiListener httpListener=new ApiManager.IApiListener() {

			@Override
			public void onJSONException(JSONException exception) {
				listener.onError(0);
			}

			public void onFailure(String msg) {
				listener.onError(1);
			}

			@Override
			public void onComplete(JSONObject result) {
				try {
					if (result.get("data") == JSONObject.NULL){
						listener.onSuccess(null);
					}
					else{
						List<Weibo> tmpList=new ArrayList<Weibo>();
						JSONObject data = result.getJSONObject("data");
						JSONArray info = data.getJSONArray("info");
						tmpList=JsonArrayToWeiboList(info);
						listener.onSuccess(tmpList);
					}
					
				} catch (JSONException je) {
					Log.d("json", "error");
					listener.onError(0);
				}
			}
		};
		requestAsync(account, Constants.Tencent.COMMENT_LIST,
				params, "GET", httpListener);
	}
	
	
	
	/**
	 *	发布微博,统一接口
	 */ 
	public static void add(Account account, String content,final ApiManager.IApiListener listener) throws Exception {
		     	 
         //微博类型, 1: 新浪, 2: 腾讯
		switch(account.type){
			case 1:
				addToTencent(account, content, null, listener);
				break;
			case 2:
				addToTencent(account, content, null, listener);
				break;
		}
	}
	/**
	 *	发布微博,统一接口
	 *  带图片
	 */
	public static void add(Account account, String content, String picFilePath, 
			final ApiManager.IApiListener listener) throws Exception {
    	Log.v(TAG, "add:" + account.type); 
		 //微博类型, 1: 新浪, 2: 腾讯
		switch(account.type){
			case 1:
				addToTencent(account, content, picFilePath, listener);
				break;
			case 2:
				addToTencent(account, content, picFilePath, listener);
				break;
		}         
	}
	/**
	 *	回复微博,统一接口
	 */ 
	public static void readd(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {
		     	 
         //微博类型, 1: 新浪, 2: 腾讯
		switch(account.type){
			case 1:
				readdToTencent(account, tid, content, listener);
				break;
			case 2:
				readdToTencent(account, tid, content, listener);
				break;
		}
	}
	/**
	 *	转发微博,统一接口
	 */ 
	public static void reply(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {
		     	 
         //微博类型, 1: 新浪, 2: 腾讯
		switch(account.type){
			case 1:
				replyToTencent(account, tid, content, listener);
				break;
			case 2:
				replyToTencent(account, tid, content, listener);
				break;
		}
	}
	/**
	 *	评论微博,统一接口
	 */ 
	public static void comment(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {
		     	 
         //微博类型, 1: 新浪, 2: 腾讯
		switch(account.type){
			case 1:
				commentToTencent(account, tid, content, listener);
				break;
			case 2:
				commentToTencent(account, tid, content, listener);
				break;
		}
	}
	//********************腾讯相关方法****************************************
	/**
	 * 发布微博，发布到腾讯，不对外
	 */
	private static void addToTencent(Account account, String content, String picFilePath, 
			final ApiManager.IApiListener listener) throws Exception {
    	
		
        Bundle params = new Bundle(); 
        params.putString("format", "json");
        params.putString("content", content);     
        params.putString("longitude", "");
        params.putString("syncflag", "1");   
        params.putString("compatibleflag", "0"); 
        
        if (picFilePath != null){
        	ApiManager.postAsync(account, Constants.Tencent.T_ADD_PIC, params, picFilePath, listener);
        	Log.v(TAG, "addToTencent width pic");
        }else{
        	ApiManager.requestAsync(account, Constants.Tencent.T_ADD, params, "POST", listener); 
        	Log.v(TAG, "addToTencent");
        }
	}
	
	/**
	 * 评论微博，文字
	 */
	private static void replyToTencent(Account account, String tid, String content, 
			final ApiManager.IApiListener listener) throws Exception {
		Log.v(TAG, "replyToTencent");
		Bundle params = new Bundle(); 
        params.putString("format", "json");
        params.putString("reid", tid);
        params.putString("content", content);     
        params.putString("longitude", "");
        params.putString("syncflag", "1");   
        params.putString("compatibleflag", "0");         
        ApiManager.requestAsync(account, Constants.Tencent.T_REPLY,
					params, "POST", listener);         
	}
	 /**
	 * 转发微博，文字
	 */
	private static void readdToTencent(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {
		Log.v(TAG, "readdToTencent");
		Bundle params = new Bundle(); 
        params.putString("format", "json");
        params.putString("reid", tid);
        params.putString("content", content);     
        params.putString("longitude", "");
        params.putString("syncflag", "1");   
        params.putString("compatibleflag", "0");          
         ApiManager.requestAsync(account, Constants.Tencent.T_READD,
					params, "POST", listener);         
	}
	 /**
	 * 回复微博，文字
	 */
	private static void commentToTencent(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {
		Log.v(TAG, "readdToTencent");
		Bundle params = new Bundle(); 
        params.putString("format", "json");
        params.putString("reid", tid);
        params.putString("content", content);     
        params.putString("longitude", "");
        params.putString("syncflag", "1");   
        params.putString("compatibleflag", "0");          
         ApiManager.requestAsync(account, Constants.Tencent.T_COMMENT,
					params, "POST", listener);         
	}

 
	
	
	
}

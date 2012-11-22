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
	private static final int WHAT_DID_LOAD_DATA = 0;
	private static final int WHAT_DID_REFRESH = 2;
	private static final int WHAT_DID_MORE = 1;
	
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
	/**
	 * 获取主页时间线
	 */
	public static void getHomeLine(Account account,int pageflag,long timeStamp,final GetHomeLineListener listener){
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
					List<Weibo> tmpList=new ArrayList<Weibo>();
					if (result.get("data") == JSONObject.NULL){
						listener.onSuccess(null);
					}
					else{
						JSONObject data = result.getJSONObject("data");
						JSONArray info = data.getJSONArray("info");
						for (int i = 0; i < info.length(); ++i) {
							JSONObject item = info.getJSONObject(i);
							int status=item.getInt("status");
							if(status!=0){
								continue;
							}
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
							tmpList.add(weibo);
						}
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
	
	public static interface GetHomeLineListener{
		void onSuccess(List<Weibo> list);
		void onError(int type);
	}
}

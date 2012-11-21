/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.io.IOException;
import java.util.List;

import org.apache.http.NameValuePair;
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
}

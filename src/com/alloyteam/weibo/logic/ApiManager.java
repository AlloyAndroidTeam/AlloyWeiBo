/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 * 
 */
public class ApiManager {

	public interface IApiListener {

		public void onComplete(JSONObject result);

		public void onJSONException(JSONException exception);

		public void onFailure(String msg);

	}

	/**
	 * 调用微博 api
	 * 
	 * @throws JSONException
	 * @throws IOException
	 * 
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

	public static void requestAsync(final Account account, final String url,
			final Bundle params, final String method,
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

}

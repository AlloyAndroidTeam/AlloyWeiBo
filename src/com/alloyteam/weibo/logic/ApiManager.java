/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
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
import com.alloyteam.weibo.model.UserInfo;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.model.Weibo2;

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

	public interface IApiResultListener {
		public void onSuccess(ApiResult result);

		public void onError(int errorCode);
	}

	public static class ApiResult {
		public ArrayList<Weibo2> weiboList;
		public Weibo2 weibo;
		public UserInfo userInfo;

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
	 * @deprecated
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

		if (!account.isValid() /* || true */) {

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
					Log.e("api",e.toString());
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
		bundle.putString("access_token", account.accessToken);
		if (account.type == Constants.TENCENT) {
			bundle.putString("oauth_version", "2.a");
			bundle.putString("scope", "all");
			bundle.putString("clientip", "127.0.0.1");
			bundle.putString("oauth_consumer_key", Constants.Tencent.APP_KEY);
			bundle.putString("openid", account.openId);
		}
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
					result = Utility.postWithFile(url, dataParams, filePath);
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
	 * 
	 * @param account
	 * @param pageCount
	 *            每页返回条数
	 * @param pageFlag
	 *            分页标识（0：第一页，1：向下翻页，2：向上翻页）
	 * @param lastId
	 *            用于翻页，和pageFlag配合使用（第一页：填0，向上翻页：填上一次请求返回的第一条记录id，向下翻页：
	 *            填上一次请求返回的最后一条记录id）
	 * @param listener
	 *            api回调
	 */
	public static void getHomeLine(final Account account, int pageCount,
			int pageFlag, long timestamp, String lastId, final IApiResultListener listener) {
		Bundle params = new Bundle();
		String url;
		params.putLong("t", System.currentTimeMillis());
		if (account.type == Constants.TENCENT) {
			url = Constants.Tencent.HOME_TIMELINE;
			params.putInt("pageflag", pageFlag);
			params.putLong("pagetime", timestamp/1000);
			params.putInt("reqnum", pageCount);
			params.putInt("type", 0);
			params.putInt("contenttype", 0);
			params.putString("format", "json");
		} else if (account.type == Constants.SINA) {
			url = Constants.Sina.HOME_TIMELINE;
			params.putInt("count", pageCount);
			params.putInt("type", 0);
			params.putInt("trim_user", 0);
			params.putInt("feature", 0);
			long id=Long.parseLong(lastId)-1;
			if (pageFlag == 1) {
				params.putLong("max_id", id);
			} else if (pageFlag == 2) {
				params.putLong("since_id", id);
			}
		} else {
			return;
		}
		ApiManager.IApiListener httpListener = new ApiManager.IApiListener() {
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
					JSONArray statuses = null;
					if (account.type == Constants.TENCENT) {
						if (result.get("data") == JSONObject.NULL) {
							statuses = null;
						} else {
							statuses = result.getJSONObject("data")
									.getJSONArray("info");
						}
					} else if (account.type == Constants.SINA) {
						statuses = result.getJSONArray("statuses");
					}
					ApiResult apiResult = JsonArrayToWeiboList(statuses,
							account.type);
					listener.onSuccess(apiResult);
				} catch (JSONException je) {
					Log.e("json", je.toString());
					je.printStackTrace();
					listener.onError(0);
				}
			}
		};
		requestAsync(account, url, params, "GET", httpListener);
	}

	/**
	 * 把返回的微博信息封装成weibo信息数组返回
	 * 
	 * @param statuses
	 * @param type
	 *            微博类型, 新浪/腾讯
	 * @return
	 * @throws JSONException
	 */
	private static ApiResult JsonArrayToWeiboList(JSONArray statuses, int type)
			throws JSONException {
		if (statuses == null) {
			return null;
		}
		ArrayList<Weibo2> weiboList = new ArrayList<Weibo2>();
		for (int i = 0, len = statuses.length(); i < len; i++) {
			JSONObject item = statuses.getJSONObject(i);
			Weibo2 weibo = JsonObjectToWeibo(item, type);
			if (weibo != null) {
				weiboList.add(weibo);
			}
		}
		ApiResult apiResult = new ApiResult();
		apiResult.weiboList = weiboList;
		return apiResult;
	}

	private static Weibo2 JsonObjectToWeibo(JSONObject status, int type) throws JSONException {
		Weibo2 weibo = new Weibo2();
		if (type == Constants.TENCENT) {
			weibo.status = status.getInt("status");
			if (weibo.status > 2) {// 大于2的都是已删除的
				weibo.status = Weibo2.WEIBO_STATUS_DELETE;
				return null;
			}
			weibo.uid = status.getString("name");
			weibo.nick = status.getString("nick");
			weibo.avatarUrl = status.getString("head") + "/50";

			weibo.id = status.getString("id");
			weibo.text = status.getString("text");
			weibo.timestamp = status.getLong("timestamp") * 1000;

			weibo.rebroadcastCount = status.getInt("count");
			weibo.commentCount = status.getInt("mcount");
			
			weibo.type = status.getInt("type");
			
			JSONObject source = null;
			try{
				source = status.getJSONObject("source");
			}catch(Exception e){
			}
			if (source != null) {
				weibo.source = JsonObjectToWeibo(source, type);
			} else {
				// 处理图片
				if (status.get("image") != JSONObject.NULL) {
					JSONArray images = status.getJSONArray("image");
					String image = images.getString(0);
					weibo.imageThumbUrl = image + "/160";
					weibo.imageMiddleUrl = image + "/460";
					weibo.imageUrl = image + "/2000";
				}
			}
		} else if (type == Constants.SINA) {
			try{
				if(status.get("deleted") != JSONObject.NULL){
//					weibo.status = Weibo2.WEIBO_STATUS_DELETE;
					return null;
				}
			}catch(Exception e){
				weibo.status = Weibo2.WEIBO_STATUS_NORMAL;
			}
			JSONObject user = status.getJSONObject("user");

			weibo.uid = user.getString("id");
			weibo.nick = user.getString("screen_name");
			weibo.avatarUrl = user.getString("avatar_large");

			weibo.id = status.getString("id");
			weibo.text = status.getString("text");
			String createAt = status.getString("created_at");
			try {
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.ENGLISH);
				Date date = format.parse(createAt);
				weibo.timestamp = date.getTime();
			} catch (ParseException e) {
				weibo.timestamp = 0;
				e.printStackTrace();
			}
			try{
				weibo.rebroadcastCount = status.getInt("reposts_count");
				weibo.commentCount = status.getInt("comments_count");
			}catch(Exception e){}
			JSONObject source = null;
			try{
				source = status.getJSONObject("retweeted_status");
				weibo.type = Weibo2.WEIBO_TYPE_REBROADCAST;
			}catch(Exception e){
				weibo.type = Weibo2.WEIBO_TYPE_ORIGINAL;
			}
			try{
				source = status.getJSONObject("status");
				weibo.type = Weibo2.WEIBO_TYPE_COMMENT;
			}catch(Exception e){
				weibo.type = Weibo2.WEIBO_TYPE_ORIGINAL;
			}
			if (source != null) {
				weibo.source = JsonObjectToWeibo(source, type);
			} else {
				// 处理图片
				try{
					weibo.imageThumbUrl = status.getString("thumbnail_pic");
					weibo.imageMiddleUrl = status.getString("bmiddle_pic");
					weibo.imageUrl = status.getString("original_pic");
				}catch(Exception e){
					
				}
			}
		}

		return weibo;
	}

	
	/**
	 * 获取评论列表
	 */
	public static void  getCommentList(final Account account, int pageCount,
			int pageFlag, String weiboId, long timestamp, String lastId, final IApiResultListener listener){
		Bundle params = new Bundle();
		String url;
		params.putLong("t", System.currentTimeMillis());
		if (account.type == Constants.TENCENT) {
			url = Constants.Tencent.COMMENT_LIST;
			
			params.putInt("reqnum", pageCount);
			params.putInt("pageflag", pageFlag);
			params.putLong("pagetime", timestamp/1000);
			params.putString("twitterid", lastId);
			params.putString("rootid", weiboId);
			params.putString("format", "json");

		} else if (account.type == Constants.SINA) {
			url = Constants.Sina.COMMENT_LIST;
			params.putInt("count", pageCount);
			params.putString("id", weiboId);
			if (pageFlag == 1) {
				params.putString("max_id", lastId);
			} else if (pageFlag == 2) {
				params.putString("since_id", lastId);
			}
		} else {
			return;
		}
		ApiManager.IApiListener httpListener = new ApiManager.IApiListener() {
			@Override
			public void onJSONException(JSONException exception) {
				Log.d("json","comment:err1");
				listener.onError(0);
			}

			public void onFailure(String msg) {
				Log.d("json","comment:err");
				listener.onError(1);
			}

			@Override
			public void onComplete(JSONObject result) {
				try {
					Log.d("json","comment:"+result.toString());
					JSONArray statuses = null;
					if (account.type == Constants.TENCENT) {
						if (result.get("data") == JSONObject.NULL) {
							statuses = null;
						} else {
							statuses = result.getJSONObject("data")
									.getJSONArray("info");
						}
					} else if (account.type == Constants.SINA) {
						statuses = result.getJSONArray("comments");
					}
					ApiResult apiResult = JsonArrayToWeiboList(statuses,
							account.type);
					listener.onSuccess(apiResult);
				} catch (JSONException je) {
					Log.e("json", "error");
					je.printStackTrace();
					listener.onError(0);
				}
			}
		};
		requestAsync(account, url, params, "GET", httpListener);
	}
	
	public static void getUserInfo(final Account account, final IApiResultListener resultListener){
		Bundle params = new Bundle();
		String url;
		params.putLong("t", System.currentTimeMillis());
		if (account.type == Constants.TENCENT) {
			url = Constants.Tencent.USER_INFO;

			params.putString("format", "json");

		} else if (account.type == Constants.SINA) {
			url = Constants.Sina.USER_INFO;
			params.putString("uid", account.uid);

		} else {
			return;
		}
		ApiManager.IApiListener httpListener = new ApiManager.IApiListener() {
			@Override
			public void onJSONException(JSONException exception) {
				Log.d("json","getUserInfo:err1");
				resultListener.onError(0);
			}

			public void onFailure(String msg) {
				Log.d("json","getUserInfo:err");
				resultListener.onError(1);
			}

			@Override
			public void onComplete(JSONObject result) {
				try {
					Log.d("json","userinfo:"+result.toString());
					JSONObject userData  = null;
					if (account.type == Constants.TENCENT) {
						if (result.get("data") == JSONObject.NULL) {
							userData = null;
						} else {
							userData = result.getJSONObject("data");
						}
					} else if (account.type == Constants.SINA) {
						userData = result;
					}
					ApiResult apiResult = new ApiResult();
					apiResult.userInfo = JsonObjectToUserInfo(userData,
							account.type);
					resultListener.onSuccess(apiResult);
				} catch (JSONException je) {
					Log.e("json", "error");
					je.printStackTrace();
					resultListener.onError(0);
				}
			}
		};
		requestAsync(account, url, params, "GET", httpListener);
	}
	
	private static UserInfo JsonObjectToUserInfo(JSONObject jsonObject, int type) throws JSONException{
		UserInfo userInfo = new UserInfo();
		if(type == Constants.SINA){
			userInfo.uid = jsonObject.getString("id");
			userInfo.nick = jsonObject.getString("name");
			userInfo.avatar = jsonObject.getString("avatar_large");
		}else if(type == Constants.TENCENT){
			userInfo.uid = jsonObject.getString("name");
			userInfo.nick = jsonObject.getString("nick");
			userInfo.avatar = jsonObject.getString("head");
		}
		return userInfo;
	}
	
	/**
	 * @deprecated
	 * @param info
	 * @return
	 * @throws JSONException
	 */
	public static List<Weibo> JsonArrayToWeiboList(JSONArray info)
			throws JSONException {
		List<Weibo> tmpList = new ArrayList<Weibo>();
		for (int i = 0; i < info.length(); ++i) {
			JSONObject item = info.getJSONObject(i);
			int status = item.getInt("status");
			if (status != 0) {
				continue;
			}
			tmpList.add(JsonToWeibo(item));
		}
		return tmpList;
	}

	/**
	 * @deprecated
	 * @param item
	 * @return
	 * @throws JSONException
	 */
	public static Weibo JsonToWeibo(JSONObject item) throws JSONException {
		String text = item.getString("text");
		String name = item.getString("name");
		String avatarUrl = item.getString("head");
		int type = item.getInt("type");
		Weibo weibo = new Weibo();
		weibo.text = text;
		weibo.name = name;
		weibo.avatarUrl = avatarUrl;
		long timestamp = item.getLong("timestamp");
		weibo.type = type;
		weibo.timestamp = timestamp;
		weibo.id = item.getString("id");
		if (type == 1||type==3) {
			if (item.get("image") != JSONObject.NULL) {
				Log.d("my", "image");
				JSONArray images = item
						.getJSONArray("image");
				weibo.imageUrl = images.getString(0);
			}
		} else {
			JSONObject source = item.getJSONObject("source");
			String text2 = source.getString("text");
			String name2 = source.getString("name");
			String avatarUrl2 = source.getString("head");
			weibo.text2 = text2;
			weibo.avatarUrl2 = avatarUrl2;
			weibo.name2 = name2;
			if (source.get("image") != JSONObject.NULL) {
				Log.d("my", "image");
				JSONArray images = source.getJSONArray("image");
				weibo.imageUrl = images.getString(0);
			}
			weibo.count = item.getInt("count");
		}
		return weibo;
	}

	/**
	 * 获取主页时间线
	 * 
	 * @deprecated
	 */
	public static void getHomeLine(Account account, int pageflag,
			long timeStamp, final GetListListener listener) {

		Bundle params = new Bundle();

		params.putInt("pageflag", pageflag);
		params.putLong("pagetime", timeStamp);
		params.putInt("reqnum", 10);
		params.putInt("type", 0);
		params.putInt("contenttype", 0);
		params.putString("format", "json");
		params.putLong("t", System.currentTimeMillis());
		ApiManager.IApiListener httpListener = new ApiManager.IApiListener() {

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
					if (result.get("data") == JSONObject.NULL) {
						listener.onSuccess(null);
					} else {
						List<Weibo> tmpList = new ArrayList<Weibo>();
						JSONObject data = result.getJSONObject("data");
						JSONArray info = data.getJSONArray("info");
						tmpList = JsonArrayToWeiboList(info);
						listener.onSuccess(tmpList);
					}

				} catch (JSONException je) {
					Log.d("json", "error");
					listener.onError(0);
				}
			}
		};
		requestAsync(account, Constants.Tencent.HOME_TIMELINE, params, "GET",
				httpListener);
	}

	/**
	 * @deprecated
	 * 
	 */
	public static interface GetListListener {
		void onSuccess(List<Weibo> list);

		void onError(int type);
	}

	/**
	 * 获取主页时间线
	 * @deprecated
	 */
	public static void getCommentList(Account account, String id, int pageflag,
			long timeStamp, final GetListListener listener) {
		Bundle params = new Bundle();
		params.putInt("pageflag", pageflag);
		params.putLong("pagetime", timeStamp);
		params.putInt("reqnum", 10);
		params.putString("rootid", id);
		params.putInt("twitterid", 0);
		params.putString("format", "json");
		params.putLong("t", System.currentTimeMillis());
		ApiManager.IApiListener httpListener = new ApiManager.IApiListener() {

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
					Log.d("json", result.toString());
					if (result.get("data") == JSONObject.NULL) {
						listener.onSuccess(null);
					} else {
						List<Weibo> tmpList = new ArrayList<Weibo>();
						JSONObject data = result.getJSONObject("data");
						JSONArray info = data.getJSONArray("info");
						tmpList = JsonArrayToWeiboList(info);
						listener.onSuccess(tmpList);
					}

				} catch (JSONException je) {
					Log.d("json", "error");
					listener.onError(0);
				}
			}
		};
		requestAsync(account, Constants.Tencent.COMMENT_LIST, params, "GET",
				httpListener);
	}

	/**
	 * 发布微博,统一接口
	 */
	public static void add(Account account, String content,
			final ApiManager.IApiListener listener) throws Exception {

		// 微博类型, 1: 新浪, 2: 腾讯
		switch (account.type) {
		case 1:
			addToTencent(account, content, null, listener);
			break;
		case 2:
			addToTencent(account, content, null, listener);
			break;
		}
	}

	/**
	 * 发布微博,统一接口 带图片
	 */
	public static void add(Account account, String content, String picFilePath,
			final ApiManager.IApiListener listener) throws Exception {
		Log.v(TAG, "add:" + account.type);
		// 微博类型, 1: 新浪, 2: 腾讯
		switch (account.type) {
		case 1:
			addToTencent(account, content, picFilePath, listener);
			break;
		case 2:
			addToTencent(account, content, picFilePath, listener);
			break;
		}
	}

	/**
	 * 回复微博,统一接口
	 */
	public static void readd(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {

		// 微博类型, 1: 新浪, 2: 腾讯
		switch (account.type) {
		case 1:
			readdToTencent(account, tid, content, listener);
			break;
		case 2:
			readdToTencent(account, tid, content, listener);
			break;
		}
	}

	/**
	 * 转发微博,统一接口
	 */
	public static void reply(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {

		// 微博类型, 1: 新浪, 2: 腾讯
		switch (account.type) {
		case 1:
			replyToTencent(account, tid, content, listener);
			break;
		case 2:
			replyToTencent(account, tid, content, listener);
			break;
		}
	}

	/**
	 * 评论微博,统一接口
	 */
	public static void comment(Account account, String tid, String content,
			final ApiManager.IApiListener listener) throws Exception {

		// 微博类型, 1: 新浪, 2: 腾讯
		switch (account.type) {
		case 1:
			commentToTencent(account, tid, content, listener);
			break;
		case 2:
			commentToTencent(account, tid, content, listener);
			break;
		}
	}

	// ********************腾讯相关方法****************************************
	/**
	 * 发布微博，发布到腾讯，不对外
	 */
	private static void addToTencent(Account account, String content,
			String picFilePath, final ApiManager.IApiListener listener)
			throws Exception {

		Bundle params = new Bundle();
		params.putString("format", "json");
		params.putString("content", content);
		params.putString("longitude", "");
		params.putString("syncflag", "1");
		params.putString("compatibleflag", "0");

		if (picFilePath != null) {
			ApiManager.postAsync(account, Constants.Tencent.T_ADD_PIC, params,
					picFilePath, listener);
			Log.v(TAG, "addToTencent width pic");
		} else {
			ApiManager.requestAsync(account, Constants.Tencent.T_ADD, params,
					"POST", listener);
			Log.v(TAG, "addToTencent");
		}
	}

	/**
	 * 评论微博，文字
	 */
	private static void replyToTencent(Account account, String tid,
			String content, final ApiManager.IApiListener listener)
			throws Exception {
		Log.v(TAG, "replyToTencent");
		Bundle params = new Bundle();
		params.putString("format", "json");
		params.putString("reid", tid);
		params.putString("content", content);
		params.putString("longitude", "");
		params.putString("syncflag", "1");
		params.putString("compatibleflag", "0");
		ApiManager.requestAsync(account, Constants.Tencent.T_REPLY, params,
				"POST", listener);
	}

	/**
	 * 转发微博，文字
	 */
	private static void readdToTencent(Account account, String tid,
			String content, final ApiManager.IApiListener listener)
			throws Exception {
		Log.v(TAG, "readdToTencent");
		Bundle params = new Bundle();
		params.putString("format", "json");
		params.putString("reid", tid);
		params.putString("content", content);
		params.putString("longitude", "");
		params.putString("syncflag", "1");
		params.putString("compatibleflag", "0");
		ApiManager.requestAsync(account, Constants.Tencent.T_READD, params,
				"POST", listener);
	}

	/**
	 * 回复微博，文字
	 */
	private static void commentToTencent(Account account, String tid,
			String content, final ApiManager.IApiListener listener)
			throws Exception {
		Log.v(TAG, "readdToTencent");
		Bundle params = new Bundle();
		params.putString("format", "json");
		params.putString("reid", tid);
		params.putString("content", content);
		params.putString("longitude", "");
		params.putString("syncflag", "1");
		params.putString("compatibleflag", "0");
		ApiManager.requestAsync(account, Constants.Tencent.T_COMMENT, params,
				"POST", listener);
	}

}

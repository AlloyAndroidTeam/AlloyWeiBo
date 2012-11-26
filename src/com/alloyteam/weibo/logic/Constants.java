/**
 * @author azraellong
 * @date 2012-11-14
 */
package com.alloyteam.weibo.logic;

import android.os.Bundle;

/**
 * @author azraellong
 * 
 */
public class Constants {
	public static final int SINA = 1;

	public static final int TENCENT = 2;

	private static String[] providers = new String[] { "新浪微博", "腾讯微博" };

	public static String[] getProviders() {
		return providers;
	}

	public static String getProvider(int type) {
		return providers[type - 1];
	}

	public static String getAuthUrl(int type) {
		String url = "";
		Bundle params = new Bundle();
		switch (type) {
		case TENCENT:
			url = Tencent.OAUTH_GET_ACCESS_TOKEN;
			params.putString("client_id", Tencent.APP_KEY);
			params.putString("response_type", "token");
			params.putString("redirect_uri", Tencent.REDIRECT_URL);
			break;
		case SINA:
			url = Sina.OAUTH_GET_ACCESS_TOKEN;
			params.putString("client_id", Sina.APP_KEY);
			params.putString("response_type", "token");
			params.putString("redirect_uri", Sina.REDIRECT_URL);
			break;
		default:
			return url;
		}
		if (url.indexOf("?") == -1) {
			url = url + "?";
		} else {
			url = url + "&";
		}
		url += Utility.toQueryString(params);
		return url;
	}

	public static String getRedirectUrl(int type){
		String url = "";
		switch (type) {
		case TENCENT:
			url = Tencent.REDIRECT_URL;
			break;
		case SINA:
			url = Sina.REDIRECT_URL;
			break;
		default:
			break;
		}
		return url;
	}
	
	public class Sina {
		public static final String APP_KEY = "3464815309";
		
		public static final String REDIRECT_URL = "http://isynchro.imatlas.com";
		
		public static final String API_ROOT = "https://api.weibo.com/2";

		public static final String OAUTH_GET_ACCESS_TOKEN = "https://api.weibo.com/oauth2/authorize?display=mobile&forcelogin=true";

		public static final String HOME_TIMELINE = API_ROOT
				+ "/statuses/home_timeline.json";
		public static final String COMMENT_LIST = API_ROOT
				+ "/comments/show.json";//获取评论列表
		public static final String DELETE = API_ROOT
				+ "/comments/destroy.json";//获取评论列表
		public static final String REBOARDCAST_LIST = API_ROOT
				+ "/statuses/repost_timeline.json";//获取转播列表
	}

	public class Tencent {
		public static final String APP_KEY = "801269361";

		public static final String REDIRECT_URL = "http://isynchro.imatlas.com";

		public static final String API_ROOT = "http://open.t.qq.com/api";

		public static final String OAUTH_GET_ACCESS_TOKEN = "https://open.t.qq.com/cgi-bin/oauth2/authorize";

		public static final String HOME_TIMELINE = API_ROOT
				+ "/statuses/home_timeline";

		public static final String T_COMMENT_LIST = API_ROOT
				+ "/t/re_list";//获取评论和转播列表
		public static final String T_DELETE = API_ROOT
				+ "/t/del";//删除微博
		public static final String T_ADD = API_ROOT + "/t/add"; //写微博
		public static final String T_ADD_PIC = API_ROOT + "/t/add_pic";//写微博pic
		public static final String T_READD = API_ROOT + "/t/re_add";//转发
		public static final String T_REPLY = API_ROOT + "/t/reply";//回复一条微博（即对话）
		public static final String T_COMMENT = API_ROOT + "/t/comment";//评论一条微博
		public static final String T_GETintimate_friends = API_ROOT + "/friends/get_intimate_friends";//获取最近联系人列表
		

	}

}

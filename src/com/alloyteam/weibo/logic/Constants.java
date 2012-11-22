/**
 * @author azraellong
 * @date 2012-11-14
 */
package com.alloyteam.weibo.logic;

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

	public class Sina {
		public static final String API_ROOT = "https://api.weibo.com/2";

		public static final String OAUTH_GET_ACCESS_TOKEN = "https://api.weibo.com/oauth2/authorize";

		public static final String HOME_TIMELINE = API_ROOT
				+ "/statuses/home_timeline";
	}

	public class Tencent {
		public static final String APP_KEY = "801269361";

		public static final String REDIRECT_URL = "http://isynchro.imatlas.com";

		public static final String API_ROOT = "http://open.t.qq.com/api";

		public static final String OAUTH_GET_ACCESS_TOKEN = "https://open.t.qq.com/cgi-bin/oauth2/authorize";

		public static final String HOME_TIMELINE = API_ROOT
				+ "/statuses/home_timeline";

		public static final String T_ADD = API_ROOT + "/t/add"; //写微博
		public static final String T_ADD_PIC = API_ROOT + "/t/add_pic";//写微博pic
		public static final String T_READD = API_ROOT + "/t/re_add";//转发
		public static final String T_REPLY = API_ROOT + "/t/reply";//回复一条微博（即对话）
		public static final String T_COMMENT = API_ROOT + "/t/comment";//评论一条微博
		

	}

}

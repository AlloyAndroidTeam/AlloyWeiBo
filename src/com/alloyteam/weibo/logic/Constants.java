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
	
	public class Sina{
		public static final String API_ROOT = "https://api.weibo.com/2"; 
		
		public static final String OAUTH_GET_ACCESS_TOKEN = "https://api.weibo.com/oauth2/authorize";
		
		
		public static final String HOME_TIMELINE = API_ROOT + "/statuses/home_timeline";
	}
	
	public class Tencent{
		public static final String APP_KEY = "801269361";
		
		public static final String REDIRECT_URL = "http://isynchro.imatlas.com";
		
		public static final String API_ROOT = "https://open.t.qq.com/api"; 
		
		public static final String OAUTH_GET_ACCESS_TOKEN = "https://open.t.qq.com/cgi-bin/oauth2/authorize";
		
		
		public static final String HOME_TIMELINE = API_ROOT + "/statuses/home_timeline";
	}
	
	
}

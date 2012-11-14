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
	
	public class SinaApi{
		public static final String API_ROOT = "https://api.weibo.com/2"; 
		
		public static final String OAUTH_GET_CODE = "https://api.weibo.com/oauth2/authorize";
		
		public static final String OAUTH_GET_ACCESS_TOKEN = "https://api.weibo.com/oauth2/access_token";
		
		public static final String HOME_TIMELINE = API_ROOT + "/statuses/home_timeline";
	}
	
	public class TencentApi{
		public static final String API_ROOT = "http://open.t.qq.com/api"; 
		
		public static final String OAUTH_GET_CODE = "https://open.t.qq.com/cgi-bin/oauth2/authorize";
		
		public static final String OAUTH_GET_ACCESS_TOKEN = "https://open.t.qq.com/cgi-bin/oauth2/access_token";
		
		public static final String HOME_TIMELINE = API_ROOT + "/statuses/home_timeline";
	}
	
}

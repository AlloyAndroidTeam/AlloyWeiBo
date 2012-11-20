/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.Utility;

/**
 * @author azraellong
 * 
 */
public class Account {

	/**
	 * 在数据库中的id
	 */
	public int id;
	
	/**
	 * 帐号id
	 */
	public String uid;

	/**
	 * 昵称
	 */
	public String nick;

	/**
	 * 头像url
	 */
	public String avator;

	/**
	 * 微博类型, 1: 新浪, 2: 腾讯
	 */
	public int type;

	public String openId;

	public String openKey;

	/**
	 * token
	 */
	public String accessToken;

	/**
	 * 腾讯微博才有的, 用于续期 accessToken
	 */
	public String refreshToken;

	/**
	 * token 授权时间
	 */
	public Date authTime;

	/**
	 * token 过期时间
	 */
	public Date invalidTime;

	/**
	 * isDefault 是否默认帐号
	 */
	public boolean isDefault;

	@Override
	public String toString() {
		return "Account [uid=" + uid + ", nick=" + nick + ", avator=" + avator
				+ ", type=" + type + ", openId=" + openId + ", openKey="
				+ openKey + ", accessToken=" + accessToken + ", refreshToken="
				+ refreshToken + ", authTime=" + authTime + ", invalidTime="
				+ invalidTime + ", isDefault=" + isDefault + "]";
	}
	
	/**
	 * 获取主页时间线
	 * pageflag 分页标识（0：第一页，1：向下翻页，2：向上翻页）
	 * pagetime 本页起始时间（第一页：填0，向上翻页：填上一次请求返回的第一条记录时间，向下翻页：填上一次请求返回的最后一条记录时间）
	 * reqnum 每次请求记录的条数（1-70条）
	 * type 拉取类型
	 * contenttype 内容过滤
	 * oauth_consumer_key=APP_KEY&access_token=ACCESSTOKEN&openid=OPENID&clientip=CLIENTIP&oauth_version=2.a&scope=all
	 */
	public void getHomeLine(final int pageflag, final int pagetime, final int reqnum, final int type, final int contenttype, final String format, final HttpConnection.HttpConnectionListener listener){
		/*if(isInvalid()){
			refresh(new RefreshCallback(){
				public void onRefreshCallback(){
					getHomeLine(pageflag,pagetime,reqnum,type,contenttype,format,listener);
				}
			});
			Log.d("my","invalid");
			return;
		}*/
    	String url = Constants.Tencent.HOME_TIMELINE +
    			"?oauth_consumer_key="+Constants.Tencent.APP_KEY+"&access_token="+accessToken+"&openid="+openId+"&clientip=127.0.0.1&oauth_version=2.a&scope=all";
    	url+="&pageflag="+pageflag+"&pagetime="+pagetime+"&reqnum="+reqnum+"&type="+type+"contenttype="+contenttype+"&format="+format+"&t="+System.currentTimeMillis();
    	HttpConnection connection=new HttpConnection();
    	connection.get(url, listener);
    	connection.run();
    	Log.d("my",url);
	}
	
//	public void refresh(final RefreshCallback callback){
//		String url="https://open.t.qq.com/cgi-bin/oauth2/access_token?client_id="+Constants.Tencent.APP_KEY+"&grant_type=refresh_token&refresh_token="+refreshToken;
//		HttpConnection.HttpConnectionListener listener = new HttpConnection.HttpConnectionListener(){
//			public void onResponse(boolean isSuccess, String result){
//				if(isSuccess){
//					//access_token=ACCESS_TOKEN&expires_in=60&refresh_token=REFRESH_TOKEN&name=NAME;
//					Bundle values = Utility.parseString(result, "#");
//					Log.d("my",result);
//			    	accessToken = values.getString("access_token");
//			    	refreshToken = values.getString("refresh_token");
//			    	nick = values.getString("name");
//			    	Log.d("my","expire:"+values.getString("expires_in"));
//			    	invalidTime = new Date(System.currentTimeMillis()+Integer.parseInt(values.getString("expires_in")));
//			    	invalidTime.setSeconds(values.getInt("expires_in"));
//			    	callback.onRefreshCallback();
//				}
//			}
//		};
//    	HttpConnection connection=new HttpConnection();
//    	connection.get(url, listener);
//    	connection.run();
//    	Log.d("my",url);
//	}
	/**
	 * 判断是否过期
	 */
	public boolean isValid(){
		long time=System.currentTimeMillis();
		Log.d("my",String.valueOf(time));
		Log.d("my", String.valueOf(invalidTime.getTime()));
		if(time>invalidTime.getTime()){
			return false;
		}
		return true;
	}
	
	public static interface RefreshCallback{
		public void onRefreshCallback();	
	}
	
}

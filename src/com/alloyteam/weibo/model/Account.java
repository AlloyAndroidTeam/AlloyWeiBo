/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.model;

import java.util.Date;

import android.util.Log;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.weibo.logic.Constants;

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
	public void getHomeLine(int pageflag, int pagetime, int reqnum, int type, int contenttype, String format, HttpConnection.HttpConnectionListener listener){
    	String url = Constants.Tencent.HOME_TIMELINE +
    			"?oauth_consumer_key="+Constants.Tencent.APP_KEY+"&access_token="+accessToken+"&openid="+openId+"&clientip=127.0.0.1&oauth_version=2.a&scope=all";
    	url+="&pageflag="+pageflag+"&pagetime="+pagetime+"&reqnum="+reqnum+"&type="+type+"contenttype="+contenttype+"&format="+format+"&t="+System.currentTimeMillis();
    	HttpConnection connection=new HttpConnection();
    	connection.get(url, listener);
    	connection.run();
    	Log.d("my",url);
	}
}

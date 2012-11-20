/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.net.HttpConnection.HttpConnectionListener;
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

	/* 判断两个帐号是否是同一个, 只判断了 uid, 和 type
	 */
	public boolean equals(Account o) {
		return this.uid.equals(o.uid) && this.type == o.type;
	}
	
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
	
	/**
	 * 发送微博，文字
	 */
	public void add(String format, String content,
			String clientip, final HttpConnection.HttpConnectionListener listener) throws Exception {
		 
    	
		 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         nameValuePairs.add(new BasicNameValuePair("format", format));
         nameValuePairs.add(new BasicNameValuePair("content", content));
         
         nameValuePairs.add(new BasicNameValuePair("clientip", clientip));
         nameValuePairs.add(new BasicNameValuePair("longitude", ""));
         nameValuePairs.add(new BasicNameValuePair("syncflag", "1"));
         nameValuePairs.add(new BasicNameValuePair("oauth_consumer_key", Constants.Tencent.APP_KEY));
         nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
         nameValuePairs.add(new BasicNameValuePair("openid", openId));         
         nameValuePairs.add(new BasicNameValuePair("oauth_version", "2.a"));
         nameValuePairs.add(new BasicNameValuePair("scope", "all")); 
         
         
         HttpConnection connection=new HttpConnection();
     	 connection.post(Constants.Tencent.T_ADD, nameValuePairs, listener);
     	 connection.run(); 
    	 
	}
	
	/**
	 * 发表一条带图片的微博
	 * 
	 * @param oAuth
	 * @param format 返回数据的格式 是（json或xml）
	 * @param content  微博内容
	 * @param clientip 用户IP(以分析用户所在地)
	 * @param jing 经度（可以填空）
	 * @param wei 纬度（可以填空）
	 * @param picpath 可以是本地图片路径 或 网络地址
	 * @param syncflag  微博同步到空间分享标记（可选，0-同步，1-不同步，默认为0）  
	 * @return
	 * @throws Exception
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%BE%AE%E5%8D%9A%E7%9B%B8%E5%85%B3/%E5%8F%91%E8%A1%A8%E4%B8%80%E6%9D%A1%E5%B8%A6%E5%9B%BE%E7%89%87%E7%9A%84%E5%BE%AE%E5%8D%9A">腾讯微博开放平台上关于此条API的文档1-本地图片</a>
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%BE%AE%E5%8D%9A%E7%9B%B8%E5%85%B3/%E7%94%A8%E5%9B%BE%E7%89%87URL%E5%8F%91%E8%A1%A8%E5%B8%A6%E5%9B%BE%E7%89%87%E7%9A%84%E5%BE%AE%E5%8D%9A">腾讯微博开放平台上关于此条API的文档2-网络图片</a>
	 */
	public void addPic(String format, String content,
			String clientip, String jing, String wei, String picpath,String syncflag,
			 final HttpConnection.HttpConnectionListener listener)
			throws Exception {
		/*
		QArrayList paramsList = new QArrayList();
		paramsList.add(new BasicNameValuePair("format", format));
		paramsList.add(new BasicNameValuePair("content", content));
		paramsList.add(new BasicNameValuePair("clientip", clientip));
		paramsList.add(new BasicNameValuePair("jing", jing));
		paramsList.add(new BasicNameValuePair("wei", wei));
        paramsList.add(new BasicNameValuePair("syncflag", syncflag));
		
		if(new File(picpath).exists()){
			//
			QArrayList pic = new QArrayList();
			pic.add(new BasicNameValuePair("pic", picpath));
			return requestAPI.postFile(tAddPicUrl, paramsList, pic,
					oAuth);
		}else{
			paramsList.add(new BasicNameValuePair("pic_url", picpath));
			return requestAPI.postContent(tAddPicUrlUrl, paramsList, oAuth);
		}
		*/
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("format", format));
        nameValuePairs.add(new BasicNameValuePair("content", content));
        
        nameValuePairs.add(new BasicNameValuePair("clientip", clientip));
        nameValuePairs.add(new BasicNameValuePair("longitude", ""));
        nameValuePairs.add(new BasicNameValuePair("syncflag", "1"));
        nameValuePairs.add(new BasicNameValuePair("oauth_consumer_key", Constants.Tencent.APP_KEY));
        nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
        nameValuePairs.add(new BasicNameValuePair("openid", openId));         
        nameValuePairs.add(new BasicNameValuePair("oauth_version", "2.a"));
        nameValuePairs.add(new BasicNameValuePair("scope", "all")); 
        nameValuePairs.add(new BasicNameValuePair("compatibleflag", "0")); 
        
        
        //pic        
        HttpConnection connection=new HttpConnection();
    	connection.post(Constants.Tencent.T_ADD_PIC, nameValuePairs, listener);
    	connection.run(); 
	}
}

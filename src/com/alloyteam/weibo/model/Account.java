/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.model;

import java.util.Date;

import android.util.Log;

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

	

	/**
	 * token
	 */
	public String accessToken;
	
	/**
	 * 腾讯微博才有
	 */
	public String openId;
	/**
	 * 腾讯微博才有
	 */
	public String openKey;
	
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
	
	 
}

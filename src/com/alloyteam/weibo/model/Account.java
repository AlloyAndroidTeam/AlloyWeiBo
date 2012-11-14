/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.model;

import java.util.Date;

/**
 * @author azraellong
 * 
 */
public class Account {

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

}

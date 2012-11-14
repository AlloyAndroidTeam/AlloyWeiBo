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
	 * 帐号名字
	 */
	public String name;
	
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
	
	
	
}

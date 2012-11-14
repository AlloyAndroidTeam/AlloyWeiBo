/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Handler;

import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 * 
 */
public class AccountManager {

	public static int SINA = 1;

	public static int TENCENT = 2;

	/**
	 * 打开授权页进行授权
	 */
	public static void auth(int type) {
		if (type == AccountManager.SINA) {
			authSina();
		}
	}

	/**
	 * 删除授权
	 */
	public static void removeAuth() {

	}

	/**
	 * 获取当前已经授权的帐号列表
	 * 
	 * @return
	 */
	public static ArrayList<Account> getAccounts() {
		return null;
	}

	/****************************** 私有函数 ***************************/

	private static void authSina() {

	}

}

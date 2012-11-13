/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.Calendar;

import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 *
 */
public class AccountManager {
	
	private static AccountManager accountManager;
	
	/**
	 * 防止直接实例化 AccountManager
	 */
	private AccountManager() {
		
	}
	
	/**
	 * 返回一个唯一的 AccountManager 实例
	 * @return
	 */
	public static AccountManager getInstance(){
		if(accountManager == null){
			accountManager = new AccountManager();
		}
		return accountManager;
	}
	
	/**
	 * 打开授权页进行授权
	 */
	public void auth(){
		
	}
	
	/**
	 * 删除授权
	 */
	public void removeAuth(){
		
	}
	
	/**
	 * 获取当前已经授权的帐号列表
	 * @return
	 */
	public ArrayList<Account> getAccounts(){
		return null;
	}
	
}

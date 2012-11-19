package com.alloyteam.weibo;

import java.util.ArrayList;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.DBHelper;
import com.alloyteam.weibo.model.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author pxz
 *
 */
public class SplashActivity extends Activity {
	/**
	 * 显示欢迎页的时间
	 */
	private static final int SPLASH_DELAY_TIME_IN_MILLION = 2000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// 在这里初始化 dbhelper
		DBHelper.init(getApplicationContext());
		final Intent i = new Intent();
		if(hasAccount()){
			i.setClass(this, MainActivity.class);
		}else{
			i.setClass(this, AccountManagerActivity.class);
		}
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				startActivity(i);
				finish();
			}
			
		}, SPLASH_DELAY_TIME_IN_MILLION);
	}
	
	/**
	 * @return 是否有默认帐号
	 */
	private boolean hasAccount(){
		ArrayList<Account> list = AccountManager.getAccounts();
		if(list != null && list.size() > 0){
			return true;
		}
		return false;
	}

}

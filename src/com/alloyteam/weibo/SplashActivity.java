package com.alloyteam.weibo;

import java.util.ArrayList;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.DBHelper;
import com.alloyteam.weibo.model.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

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
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		// 在这里初始化 dbhelper
		Context context = getApplicationContext();
		DBHelper.init(context);
		AccountManager.init(context);
		ApiManager.init(context);
		
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
		int count = AccountManager.getAccountCount();
		return count > 0;
	}

}

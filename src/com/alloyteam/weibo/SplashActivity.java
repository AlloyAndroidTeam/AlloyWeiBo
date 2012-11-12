package com.alloyteam.weibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author pxz
 *
 */
public class SplashActivity extends Activity {
	private static final int SPLASH_DELAY_TIME_IN_MILLION = 2000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
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
		// TODO 
		// check the DB if has bind some account
		return true;
	}

}

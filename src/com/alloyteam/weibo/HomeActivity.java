package com.alloyteam.weibo;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author pxz
 *
 */
public class HomeActivity extends Activity {
	public static final String TAG = "HomeActivity";
	
	private Handler mainHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
		}
	};
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i;
			switch(v.getId()){
			case R.id.btn_account_manager:
				i = new Intent(HomeActivity.this,AccountManagerActivity.class);
				startActivity(i);
				break;
			case R.id.btn_group:
				// TODO 微博分组暂不处理
				//Intent i = new Intent(HomeActivity.this,AccountManager.class);
				//startActivity(i);
				break;
			case R.id.btn_post:
				i = new Intent(HomeActivity.this,PostActivity.class);
				startActivity(i);
				break;
			case View.NO_ID:// fall through
			default:
			}
		}
	};

	private OnItemClickListener timelineClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO
			// get extra info and transfer to TwittDetailActivity
			//Intent i = new Intent(HomeActivity.this,TwittDetailActivity.class);
			//long twittId = balabala();
			//i.putExtra(TWITT_ID, twittId)
			//startActivity(i);
		}
	};
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_home);
		findViewById(R.id.btn_account_manager).setOnClickListener(listener);
		findViewById(R.id.btn_group).setOnClickListener(listener);
		findViewById(R.id.btn_post).setOnClickListener(listener);
		((ListView)findViewById(R.id.lv_main_timeline)).setOnItemClickListener(timelineClickListener );
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		// check and update list(if need)
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
	}
}

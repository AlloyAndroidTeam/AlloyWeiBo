package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.model.Account;

/**
 * @author pxz
 * 
 */
public class MainActivity extends TabActivity implements OnClickListener {

	public static final String TAG = "MainActivity";

	Button accountSwitchBtn;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// String uid = intent.getStringExtra("uid");
			// int type = intent.getIntExtra("type", 0);
			// Log.v(TAG, "onReceive: " + uid + " added.");
			String action = intent.getAction();
			if ("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE".equals(action)) {
				Account account = AccountManager.getDefaultAccount();
				if (account != null) {
					accountSwitchBtn.setText(getAccountDescption(account));
				} else {
					accountSwitchBtn.setText("绑定帐号");
				}
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.home_title);

		setupTabHost();

		Intent intent = new Intent(this, HomeActivity.class);
		setupTab(new TextView(this), "首页", R.drawable.tab_bg_home, intent);
		intent = new Intent(this, SettingActivity.class);
		setupTab(new TextView(this), "设置", R.drawable.tab_bg_home, intent);
		// TODO setup others
		accountSwitchBtn = (Button) findViewById(R.id.btnHomeTitleAccount);
		accountSwitchBtn.setOnClickListener(this);
		findViewById(R.id.btnHomeTitlePost).setOnClickListener(this);
		Account defaultAccount = AccountManager.getDefaultAccount();
		if (defaultAccount != null) {
			accountSwitchBtn.setText(getAccountDescption(defaultAccount));
		} else {
			accountSwitchBtn.setText("绑定帐号");
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE");
		// intentFilter.addAction("com.alloyteam.weibo.ACCOUNT_REMOVE");
		// intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		this.registerReceiver(broadcastReceiver, intentFilter);
		
		if(!AccountManager.hasAccount()){
			Intent i = new Intent(this, AccountManagerActivity.class);
			startActivity(i);
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			boolean isHomeCurrent = mTabHost.getCurrentTab() == 0;
			if(isHomeCurrent){
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setMessage("您确定要退出吗?")
					.setPositiveButton("退出",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.finish();
						}
					})
					.setNegativeButton("后台运行", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.moveTaskToBack(true);
						}
					});
				
				builder.show();
			}else{
				mTabHost.setCurrentTab(0);
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	private TabHost mTabHost;

	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
	}

	private void setupTab(final View view, final String tag, int drawable,
			Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag, drawable);
		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview)
				.setContent(intent);
		mTabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text,
			int drawable) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_item,
				null);
		TextView tv = (TextView) view.findViewById(R.id.tab_item_text);
		tv.setText(text);
		ImageView iv = (ImageView) view.findViewById(R.id.tab_item_icon);
		iv.setImageResource(drawable);
		return view;
	}

	/**
	 * Yukin:响应click
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent i;
		switch (v.getId()) {
		case R.id.btnHomeTitlePost: // @发表
			i = new Intent(this, PostActivity.class);
			startActivity(i);
			break;
		case R.id.btnHomeTitleAccount: // 帐号
			// i = new Intent(this, AccountManagerActivity.class);
			// startActivity(i);
			AlertDialog.Builder builder;
			ArrayList<Account> accounts = AccountManager.getAccounts();
			int count = accounts.size();
			if (count == 0) {
				i = new Intent(this, AccountManagerActivity.class);
				startActivity(i);
				break;
			}else if(count == 1){
				builder = new AlertDialog.Builder(this);
				builder.setMessage("您只绑定了一个帐号，继续添加帐号吗？")
					.setPositiveButton("继续添加", new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(MainActivity.this, AccountManagerActivity.class);
							startActivity(i);
						}
					}).show();
				break;
			}
			
			final AccountArrayAdapter adapter = new AccountArrayAdapter(this,
					0, accounts);

			builder = new AlertDialog.Builder(this);
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int position) {
					Account account = adapter.getItem(position);
					if (account != null) {
						AccountManager.switchDefaultAccount(account);
					}
				}
			});
			AlertDialog dialog = builder.create();
			dialog.setTitle("切换帐号");
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			

			break;
		}
	}

	private class AccountArrayAdapter extends ArrayAdapter<Account> {

		private Context context;
		private LayoutInflater layoutInflater;

		/**
		 * @param context
		 * @param textViewResourceId
		 * @param objects
		 */
		public AccountArrayAdapter(Context context, int textViewResourceId,
				List<Account> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.layoutInflater = LayoutInflater.from(context);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = this.layoutInflater.inflate(
						R.layout.account_switch_item, null);
			}
			TextView textView = (TextView) convertView
					.findViewById(R.id.currentAccountText);
			Account account = this.getItem(position);
			String text = getAccountDescption(account);
			if (account.isDefault) {
				text += " ∨";
			}
			textView.setText(text);

			return convertView;
		}

	}

	private String getAccountDescption(Account account) {
		String text = account.uid + "(" + Constants.getProvider(account.type)
				+ ")";

		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		this.unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
}

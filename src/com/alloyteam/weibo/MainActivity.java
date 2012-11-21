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
import android.util.Log;
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
//			String uid = intent.getStringExtra("uid");
//			int type = intent.getIntExtra("type", 0);
//			Log.v(TAG, "onReceive: " + uid + " added.");
			String action = intent.getAction();
			if ("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE".equals(action)) {
				Account account = AccountManager.getDefaultAccount();
				if(account != null){
					accountSwitchBtn.setText(getAccountDescption(account));
					accountSwitchBtn.setTag(0);
				}else{
					accountSwitchBtn.setText("绑定帐号");
					accountSwitchBtn.setTag(1);
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
			accountSwitchBtn.setTag(0);
		} else {
			accountSwitchBtn.setText("绑定帐号");
			accountSwitchBtn.setTag(1);
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE");
//		intentFilter.addAction("com.alloyteam.weibo.ACCOUNT_REMOVE");
//		intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		this.registerReceiver(broadcastReceiver, intentFilter);
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
			ArrayList<Account> accounts = AccountManager.getAccounts();
			if(accounts.size() == 1){
				break;
			}
			Object tag = accountSwitchBtn.getTag();
			if (tag.equals(1)) {
				i = new Intent(this, AccountManagerActivity.class);
				startActivity(i);
				break;
			}
			final AccountArrayAdapter adapter = new AccountArrayAdapter(this,
					0, accounts);
			AlertDialog dialog = new AlertDialog.Builder(this).setAdapter(
					adapter, new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Account account = adapter.getItem(which);
							if (account != null) {
								AccountManager.switchDefaultAccount(account);
							}
						}
					}).create();
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

			textView.setText(getAccountDescption(account));
			return convertView;
		}

	}

	private String getAccountDescption(Account account) {
		String text = account.uid + "(" + Constants.getProvider(account.type)
				+ ")";
		// if(account.isDefault){
		// text += " 当前帐号";
		// }
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

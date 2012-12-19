package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.DBHelper;
import com.alloyteam.weibo.logic.ApiManager.ApiResult;
import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo2;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;
import com.alloyteam.weibo.model.UserInfo;

/**
 * @author pxz
 * 
 */
public class MainActivity extends Activity implements OnPullDownListener, OnClickListener, OnItemClickListener {

	public static final String TAG = "MainActivity";

	Button accountSwitchBtn;
	Button settingBtn;
	public ImageView bigImageView;
	static public ImageLoader imageLoader;
	public boolean isMove=false;
	public ListView mylist;
	private PullDownView mPullDownView;
	private static final int WHAT_DID_LOAD_DATA = 0;
	private static final int WHAT_DID_REFRESH = 2;
	private static final int WHAT_DID_MORE = 1;
	private WeiboListAdapter mAdapter;
	private List<Weibo2> list;
	private String upId;
	private String downId;
	private Account account;
	private long upTimeStamp=0;
	private long downTimeStamp=0;
	

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
			}else if("com.alloyteam.weibo.ACCOUNT_UPDATE".equals(action)){
				String uid = intent.getStringExtra("uid");
				int type = intent.getIntExtra("type", 0);
				Account account = AccountManager.getAccount(uid, type);
				Account defaultAccount = AccountManager.getDefaultAccount();
				if (account.equals(defaultAccount)) {
					accountSwitchBtn.setText(getAccountDescption(account));
				}
			}
			if("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE".equals(action)){
				initHomeLine();
			}
			else if("com.alloyteam.weibo.WEIBO_ADDED".equals(action)){
				//mPullDownView.initHeaderViewAndFooterViewAndListView(context);
				onRefresh();
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
		Context context = getApplicationContext();
		DBHelper.init(context);
		AccountManager.init(context);
		ApiManager.init(context);
		
		accountSwitchBtn = (Button) findViewById(R.id.btnHomeTitleAccount);
		accountSwitchBtn.setOnClickListener(this);
		//settingBtn = (Button) findViewById(R.id.btnHomeTitleAccountSetting);
		//settingBtn.setOnClickListener(this);
		findViewById(R.id.btnHomeTitlePost).setOnClickListener(this);
		
		Account defaultAccount = AccountManager.getDefaultAccount();
		if (defaultAccount != null) {
			accountSwitchBtn.setText(getAccountDescption(defaultAccount));
		} else {
			accountSwitchBtn.setText("绑定帐号");
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE");
		intentFilter.addAction("com.alloyteam.weibo.WEIBO_ADDED");		
		 intentFilter.addAction("com.alloyteam.weibo.ACCOUNT_UPDATE");
		// intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		this.registerReceiver(broadcastReceiver, intentFilter);
		if(AccountManager.getAccountCount()==0){
			Intent i = new Intent(this, AccountManagerActivity.class);
			startActivity(i);
			return;
		}
		imageLoader=new ImageLoader(this);
		initHomeLine();
		
		Account account = AccountManager.getDefaultAccount();
		if(account != null){
			loadUserInfo();
		}
		
	}

	public void initHomeLine() {
		mPullDownView = (PullDownView) findViewById(R.id.pull_down_view);
		mPullDownView.setOnPullDownListener(this);
		list = new ArrayList<Weibo2>();
		mylist = mPullDownView.getListView();
		mAdapter = new WeiboListAdapter(
				this, list);
		mylist.setAdapter(mAdapter);
		mylist.setOnItemClickListener(this);
		mPullDownView.enableAutoFetchMore(true, 1);
		account = AccountManager.getDefaultAccount();
		if (account == null)
			return;
		loadData(WHAT_DID_LOAD_DATA);
	}
	
	public void pullCallback(int pageflag){
		if(pageflag==WHAT_DID_LOAD_DATA){
			mPullDownView.notifyDidLoad();
		}
		else if(pageflag==WHAT_DID_MORE){
			mPullDownView.notifyDidMore();
		}
		else{				
			mPullDownView.notifyDidRefresh();
		}		
	}
	
	public void loadData(final int pageflag){
		ApiManager.IApiResultListener listener = new ApiManager.IApiResultListener() {
			@Override
			public void onSuccess(ApiResult result) {
				if(result == null || result.weiboList == null){
					pullCallback(pageflag);
					return;
				}
				ArrayList<Weibo2> tmpList = result.weiboList;
				if(pageflag==WHAT_DID_LOAD_DATA){
					mPullDownView.notifyDidLoad();
					if(tmpList.size()>0){
						Weibo2 lastWeibo=tmpList.get(tmpList.size()-1);
						downId=lastWeibo.id;
						downTimeStamp=lastWeibo.timestamp;
						Weibo2 firstWeibo=tmpList.get(0);
						upId=firstWeibo.id;
						upTimeStamp=firstWeibo.timestamp;
						list.addAll(tmpList);							
					}
					DataManager.set(account.uid,list);
				}
				else if(pageflag==WHAT_DID_MORE){
					mPullDownView.notifyDidMore();								
					if(tmpList.size()>0){
						Weibo2 lastWeibo=tmpList.get(tmpList.size()-1);
						downId=lastWeibo.id;
						downTimeStamp=lastWeibo.timestamp;
						list.addAll(tmpList);
					}
				}
				else{				
					mPullDownView.notifyDidRefresh();
					if(tmpList.size()>0){
						Weibo2 firstWeibo=tmpList.get(0);
						upId=firstWeibo.id;
						upTimeStamp=firstWeibo.timestamp;
						list.addAll(0, tmpList);
					}
				}
				mAdapter.notifyDataSetChanged();
			}
			@Override
			public void onError(int errorCode) {
				pullCallback(errorCode);
			}
		};
		String Id;
		long timestamp;
		if(pageflag==WHAT_DID_REFRESH){
			Id=upId;
			timestamp=upTimeStamp;
		}
		else if(pageflag==WHAT_DID_MORE){
			Id=downId;
			timestamp=downTimeStamp;
		}
		else{
			Id="0";
			timestamp=0;
		}
		
		ApiManager.getHomeLine(account, null, 10, pageflag, timestamp, Id, listener);

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
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		MenuItem setting =  menu.findItem(R.id.menu_settings);
		setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(MainActivity.this, SettingActivity.class);
				startActivity(i);
				return false;
			}
		});
		MenuItem account = menu.findItem(R.id.menu_account);
		account.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(MainActivity.this, AccountManagerActivity.class);
				startActivity(i);
				return false;
			}
		});
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
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
		case R.id.btnHomeTitleAccountSetting:
			i = new Intent(this, AccountManagerActivity.class);
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
			builder.setPositiveButton("+ 绑定帐号", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int position) {
					Intent i = new Intent(MainActivity.this, AccountManagerActivity.class);
					startActivity(i);
				}
			});
			final AlertDialog dialog = builder.create();
			
			ListView listView = new ListView(this);
			listView.setBackgroundColor(Color.WHITE);
			listView.setAdapter(adapter);
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Account account = adapter.getItem(position);
					if (account != null) {
//						for(int i = 0, len = parent.getChildCount(); i < len;i ++){
//							View v = parent.getChildAt(i);
//							RadioButton radio = (RadioButton) v.findViewById(R.id.currentAccountRadio);
//							radio.setChecked(position == i);
//						}
						AccountManager.switchDefaultAccount(account);
					}
					dialog.dismiss();
				}
				
			} );
			
//			builder.setView(listView);
//			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int position) {
//					Account account = adapter.getItem(position);
//					if (account != null) {
//						AccountManager.switchDefaultAccount(account);
//					}
//				}
//			});
			
			dialog.setView(listView, 0, 0, 0, 0);
			dialog.setTitle("切换帐号");
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			

			break;
		}
	}

	private class AccountArrayAdapter extends ArrayAdapter<Account> {

		private Context context;
		private LayoutInflater layoutInflater;

		class ViewHolder{
			TextView textView;
			RadioButton radioButton;
		}
		
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
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = this.layoutInflater.inflate(
						R.layout.account_switch_item, null);
				viewHolder = new ViewHolder();
				viewHolder.textView = (TextView) convertView
						.findViewById(R.id.currentAccountText);
				viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.currentAccountRadio);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder)convertView.getTag();
			}
			Account account = this.getItem(position);
			String text = getAccountDescption(account);
			viewHolder.textView.setText(text);
			viewHolder.radioButton.setChecked(account.isDefault);

			return convertView;
		}

	}

	private String getAccountDescption(Account account) {
		String text = account.nick + "(" + Constants.getProvider(account.type)
				+ ")";

		return text;
	}
	
	
	private ApiManager.IApiResultListener userInfoListener = new ApiManager.IApiResultListener() {
		
		@Override
		public void onSuccess(ApiResult result) {
			// TODO Auto-generated method stub
			UserInfo userInfo = result.userInfo;
			if(userInfo != null){
				Account account = AccountManager.getAccount(userInfo.uid, userInfo.type);
				Log.d(TAG, "update nick, from:" + account.nick + ", to:" + userInfo.nick);
				account.nick = userInfo.nick;
				account.avatar = userInfo.avatar;
				AccountManager.updateAccount(account);
			}
		}
		
		@Override
		public void onError(int errorCode) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void loadUserInfo(){
		ArrayList<Account> accounts = AccountManager.getAccounts();
		for (Account account : accounts) {
			ApiManager.getUserInfo(account, userInfoListener);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, DetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("uid", account.uid);
		bundle.putString("myuid", account.uid);
		bundle.putInt("type", account.type);
		bundle.putInt("position", position);//+parent.getFirstVisiblePosition());
		intent.putExtras(bundle);
		startActivity(intent);		
	}

	@Override
	public void onRefresh() {
		loadData(WHAT_DID_REFRESH);
	}
	
	@Override
	public void onMore() {
		loadData(WHAT_DID_MORE);
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

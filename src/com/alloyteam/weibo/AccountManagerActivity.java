package com.alloyteam.weibo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.model.Account;

/**
 * @author pxz
 * 
 */
public class AccountManagerActivity extends Activity {

	static String TAG = "AccountManagerActivity";

	AccountListAdatper accountListAdatper;


	int currentDefaultAccountPosition = -1;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String uid = intent.getStringExtra("uid");
			int type = intent.getIntExtra("type", 0);
			String action = intent.getAction();

			if("com.alloyteam.weibo.NEW_ACCOUNT_ADD".equals(action)){
				Log.v(TAG, "onReceive: " + uid + " added.");
				Account account = AccountManager.getAccount(uid, type);
				accountListAdatper.add(account);
			}else if("com.alloyteam.weibo.ACCOUNT_UPDATE".equals(action)){
				//TODO 未测试
				Account account = AccountManager.getAccount(uid, type);
				int position = accountListAdatper.getPositionByAccount(account);
				accountListAdatper.accounts.set(position, account);
				accountListAdatper.notifyDataSetChanged();
			}else if("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE".equals(action)){
				Account newDefault = AccountManager.getDefaultAccount();
				if(newDefault != null){
					int position = accountListAdatper.getPositionByAccount(newDefault);
					if(position > -1){
						currentDefaultAccountPosition = position;
						accountListAdatper.accounts.set(position, newDefault);
						accountListAdatper.notifyDataSetChanged();
					}
				}
				
//				if(newDefault != null){
//					newDefault.isDefault = true;
//					AccountManager.switchDefaultAccount(newDefault);
//					accountListAdatper.notifyDataSetChanged();
//				}
			}
		}
	};

	OnClickListener onAddBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Activity context = AccountManagerActivity.this;
			String [] providers = Constants.getProviders();
//			ListView listView = new ListView(context);
//			listView.setAdapter(new ArrayAdapter<String>(context,
//					R.layout.account_manager_provider, R.id.providerDesc,
//					providers));

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("选择帐号类型");
			builder.setItems(providers, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int index) {
//					Log.v(TAG, index + " click");
					Activity context = AccountManagerActivity.this;
					int type = index + 1;
					Intent intent = new Intent(context, AuthActivity.class);
					intent.putExtra("type", type);
					context.startActivity(intent);
				}
			});
//			builder.setNegativeButton("取消", null);
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();

		}
	};

	OnItemClickListener onAccListItemClickListener = new OnItemClickListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (currentDefaultAccountPosition == position) {
				return;
			}
			if (currentDefaultAccountPosition != -1) {
				Account current = accountListAdatper
						.getItem(currentDefaultAccountPosition);
				current.isDefault = false;
			}
			Account account = accountListAdatper.getItem(position);
			account.isDefault = true;
			AccountManager.switchDefaultAccount(account);
//			accountListAdatper.notifyDataSetChanged();
		}

	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_setting);

		Button addButton = (Button) findViewById(R.id.addNewAccount);
		addButton.setOnClickListener(onAddBtnClickListener);

		ArrayList<Account> accounts = AccountManager.getAccounts();
		accountListAdatper = new AccountListAdatper(this, 0, accounts);
		ListView accountListView = (ListView) findViewById(R.id.accountList);
		accountListView.setAdapter(accountListAdatper);

		accountListView.setOnItemClickListener(onAccListItemClickListener);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		intentFilter.addAction("com.alloyteam.weibo.ACCOUNT_UPDATE");
		intentFilter.addAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE");
		this.registerReceiver(broadcastReceiver, intentFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (broadcastReceiver != null) {
			this.unregisterReceiver(broadcastReceiver);
		}
	}

	class AccountViewHolder {
		Button deleteButton;
		TextView description;
		TextView provider;
		TextView defaultSelect;
	}

	class AccountListAdatper extends ArrayAdapter<Account> {

		ArrayList<Account> accounts;

		LayoutInflater layoutInflater;

		/**
		 * @param context
		 * @param textViewResourceId
		 * @param objects
		 */
		public AccountListAdatper(Context context, int textViewResourceId,
				ArrayList<Account> objects) {
			super(context, textViewResourceId, objects);

			this.layoutInflater = LayoutInflater.from(context);

			this.accounts = objects;
		}
		
		public int getPositionByAccount(Account o){
			Account a;
			for (int i = 0, length = accounts.size(); i < length; i++) {
				a = accounts.get(i);
				if(a.equals(o)){
					return i;
				}
			}
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AccountViewHolder holder;
			if (convertView == null) {
				holder = new AccountViewHolder();

				convertView = layoutInflater.inflate(
						R.layout.account_manager_item, null);

				holder.description = (TextView) convertView
						.findViewById(R.id.descTextView);
				holder.deleteButton = (Button) convertView
						.findViewById(R.id.accountDelBtn);
				holder.provider = (TextView) convertView
						.findViewById(R.id.accountProviderDesc);
				holder.defaultSelect = (TextView) convertView
						.findViewById(R.id.defaultSelectTxt);

				convertView.setTag(holder);

			} else {

				holder = (AccountViewHolder) convertView.getTag();
			}

			final Account account = this.getItem(position);
			String desc = Constants.getProvider(account.type);

			if (account.isDefault) {
				currentDefaultAccountPosition = position;
				holder.defaultSelect.setVisibility(View.VISIBLE);
			} else {
				holder.defaultSelect.setVisibility(View.GONE);
			}
			holder.provider.setText(desc);
			desc = account.uid + "(" + account.nick + ")";
			holder.description.setText(desc);

			holder.deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String message = "您确定要解除绑定吗?";
					if (account.isDefault) {
						message = "该帐号是默认帐号，" + message;
					}
					new AlertDialog.Builder(AccountManagerActivity.this)
							.setMessage(message)
							.setPositiveButton(R.string.ensure_text,
									new AlertDialog.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											AccountManager.removeAccount(account);
											accountListAdatper.remove(account);
										}

									})
									.setNegativeButton("取消", null)
						.show();

				}
			});

			return convertView;
		}

	}
}

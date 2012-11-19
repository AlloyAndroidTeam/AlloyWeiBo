package com.alloyteam.weibo;

import java.util.ArrayList;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.DBHelper;
import com.alloyteam.weibo.model.Account;

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
import android.widget.RadioButton;

import android.widget.TextView;

/**
 * @author pxz
 * 
 */
public class AccountManagerActivity extends Activity {

	static String TAG = "AccountManagerActivity";

	AccountListAdatper accountListAdatper;

	String[] providers = new String[] { "新浪微博", "腾讯微博" };

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String uid = intent.getStringExtra("uid");
			int type = intent.getIntExtra("type", 0);
			Account account = AccountManager.getAccount(uid, type);
			accountListAdatper.add(account);
			Log.v(TAG, "onReceive: " + uid + " added.");
		}
	};
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_account_manager);

		Button addButton = (Button) findViewById(R.id.addNewAccount);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity context = AccountManagerActivity.this;
				ListView listView = new ListView(context);
				listView.setAdapter(new ArrayAdapter<String>(context,
						R.layout.account_manager_provider, R.id.providerDesc,
						providers));

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("选择帐号类型");
				builder.setItems(providers, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int index) {
						Log.v(TAG, index + " click");
						Activity context = AccountManagerActivity.this;
						int type = index + 1;
						Intent intent = new Intent(context, AuthActivity.class);
						intent.putExtra("type", type);
						context.startActivity(intent);
				    }
				});
				builder.setNegativeButton("取消",  null);
				AlertDialog dialog = builder.create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();

			}
		});

		ArrayList<Account> accounts = AccountManager.getAccounts();
		accountListAdatper = new AccountListAdatper(this, 0, accounts);
		ListView accountListView = (ListView) findViewById(R.id.accountList);
		accountListView.setAdapter(accountListAdatper);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		this.registerReceiver(broadcastReceiver, intentFilter);
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		this.unregisterReceiver(broadcastReceiver);
	}



	class AccountViewHolder {
		Button deleteButton;
		TextView description;
		TextView provider;
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AccountViewHolder holder;
			if (convertView == null) {
				holder = new AccountViewHolder();

				convertView = layoutInflater.inflate(
						R.layout.account_manager_item, null);

				holder.description = (TextView) convertView
						.findViewById(R.id.descTextView);
				holder.deleteButton = (Button) convertView.findViewById(R.id.accountDelBtn);
				holder.provider = (TextView) convertView.findViewById(R.id.accountProviderDesc);

				convertView.setTag(holder);

			} else {

				holder = (AccountViewHolder) convertView.getTag();
			}

			final Account account = this.getItem(position);
			String desc = "";
			if (account.type == Constants.TENCENT) {
				desc += "腾讯微博";
			} else if (account.type == Constants.SINA) {
				desc += "新浪微博";
			}
			holder.provider.setText(desc);
			desc = account.uid + "(" + account.nick + ")";
			holder.description.setText(desc);

			holder.deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(AccountManagerActivity.this)
							.setMessage("您确定要解除绑定吗?")
							.setPositiveButton(R.string.ensure_text,
									new AlertDialog.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											AccountManager
													.removeAccount(account);
											accountListAdatper.remove(account);
										}

									}).show();

				}
			});

			return convertView;
		}

	}
}

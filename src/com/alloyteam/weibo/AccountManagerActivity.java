package com.alloyteam.weibo;

import java.util.ArrayList;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.DBHelper;
import com.alloyteam.weibo.model.Account;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;

/**
 * @author pxz
 *
 */
public class AccountManagerActivity extends Activity {
	
	DBHelper dbHelper;
	
	AccountListAdatper accountListAdatper;
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_account_manager);
		
		dbHelper = new DBHelper(this);
		
		Button addButton = (Button) findViewById(R.id.addNewAccount);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		ArrayList<Account> accounts = AccountManager.getAccounts(dbHelper);
		accountListAdatper = new AccountListAdatper(this, 0, accounts);
		ListView accountListView = (ListView) findViewById(R.id.accountList);
		accountListView.setAdapter(accountListAdatper);
		
	}
	
	class AccountViewHolder{
		Button deleteButton;
		TextView description;
	}
	
	class AccountListAdatper extends ArrayAdapter<Account>{

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
				
				convertView = layoutInflater.inflate(R.layout.account_manager_item, parent);

				holder.description = (TextView) convertView
						.findViewById(R.id.descTextView);
				holder.deleteButton = (Button) convertView
						.findViewById(R.id.deleteAccountBtn);

				convertView.setTag(holder);

			} else {

				holder = (AccountViewHolder) convertView.getTag();
			}

			Account account = this.getItem(position);
			String desc = "";
			if(account.type == Constants.TENCENT){
				desc += "腾讯微博: ";
			}else if(account.type == Constants.SINA){
				desc += "新浪微博: ";
			}
			desc += account.nick + "(" + account.nick + ")";
			holder.description.setText(desc);
			
			
			return convertView;
		}
		
	}
}

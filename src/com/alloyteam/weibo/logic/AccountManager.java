/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 * 
 */
public class AccountManager {

	static final String TAG = "AccountManager";

	private static Account currentDefaultAccount;
	
	private static Context amContext;
	
	public static void init(Context context){
		amContext = context;
	}
	
	private static void broadcastAccountAction(String action, Account account){
		Intent intent = new Intent();
		intent.putExtra("id", account.id);
		intent.putExtra("uid", account.uid);
		intent.putExtra("type", account.type);
		intent.setAction(action);
		amContext.sendBroadcast(intent);
	}
	
	/**
	 * 添加帐号
	 */
	public static void addAccount(Account account) {
		Log.v(TAG, "addAccount: " + account);

		DBHelper dbHelper = DBHelper.getInstance();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("uid", account.uid);
		values.put("nick", account.nick);
		values.put("type", account.type);

		values.put("openId", account.openId);
		values.put("openKey", account.openKey);
		values.put("accessToken", account.accessToken);
		values.put("refreshToken", account.refreshToken);
		values.put("isDefault", account.isDefault ? 1 : 0);
		values.put("invalidTime", account.invalidTime.getTime());
		values.put("authTime", account.authTime.getTime());

		long result = db.insert(DBHelper.ACCOUNT_TABLE_NAME, null, values);
		account.id = (int) result;
		Log.v(TAG, "addAccount result: " + result);
		
		broadcastAccountAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD", account);
	}

	/**
	 * 删除帐号
	 */
	public static void removeAccount(Account account) {
		if(currentDefaultAccount != null && currentDefaultAccount.equals(account)){
			currentDefaultAccount = null;
		}
		DBHelper dbHelper = DBHelper.getInstance();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(DBHelper.ACCOUNT_TABLE_NAME, "uid=? and type=?",
				new String[] { account.uid, account.type + "" });
		Log.v(TAG, "removeAccount: " + account);
		broadcastAccountAction("com.alloyteam.weibo.ACCOUNT_REMOVE", account);
	}

	public static void updateAccount(Account account) {
		if(currentDefaultAccount != null && currentDefaultAccount.equals(account)){
			currentDefaultAccount = account;
		}
		if (exists(account.uid, account.type)) {
			SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("nick", account.nick);

			values.put("openId", account.openId);
			values.put("openKey", account.openKey);
			values.put("accessToken", account.accessToken);
			values.put("refreshToken", account.refreshToken);
			values.put("isDefault", account.isDefault ? 1 : 0);
			values.put("invalidTime", account.invalidTime.getTime());
			values.put("authTime", account.authTime.getTime());
			db.update(DBHelper.ACCOUNT_TABLE_NAME, values, "uid=? and type=?",
					new String[] { account.uid, account.type + "" });
			broadcastAccountAction("com.alloyteam.weibo.ACCOUNT_UPDATE", account);
		}
	}

	/**
	 * 根据 account.uid 读取 account
	 * 
	 * @param uid
	 */
	public static Account getAccount(String uid, int type) {
		DBHelper dbHelper = DBHelper.getInstance();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
				null, // Columns to return
				"uid=? and type=?", // SQL WHERE
				new String[] { uid, type + "" }, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				null, // SQL ORDER BY
				"1" // limit
				);
		Account account = null;
		if (cursor.moveToFirst()) {
			account = new Account();
			parseCursorToAccount(account, cursor);
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		Log.v(TAG, "getAccount: " + account);
		return account;
	}

	public static boolean exists(String uid, int type) {
		DBHelper dbHelper = DBHelper.getInstance();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
				null, // Columns to return
				"uid=? and type=?", // SQL WHERE
				new String[] { uid, type + "" }, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				null, // SQL ORDER BY
				"1" // limit
				);
		boolean result = false;
		if (cursor.moveToFirst()) {
			result = true;
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return result;
	}

	/**
	 * 获取默认帐号
	 */
	public static Account getDefaultAccount() {
		if(currentDefaultAccount == null){
			DBHelper dbHelper = DBHelper.getInstance();
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
					null, // Columns to return
					"isDefault=?", // SQL WHERE
					new String[] { "1" }, // Selection Args
					null, // SQL GROUP BY
					null, // SQL HAVING
					null, // SQL ORDER BY
					"1" // limit
					);
			if (cursor.moveToFirst()) {
				Account account = new Account();
				parseCursorToAccount(account, cursor);
				Log.v(TAG, "getDefaultAccount - default: " + account);
				currentDefaultAccount =  account;
			} else {
				cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
						null, // Columns to return
						null, // SQL WHERE
						null, // Selection Args
						null, // SQL GROUP BY
						null, // SQL HAVING
						null, // SQL ORDER BY
						"1" // limit
						);
				if (cursor.moveToFirst()) {
					Account account = new Account();
					parseCursorToAccount(account, cursor);
					Log.v(TAG, "getDefaultAccount - first: " + account);
					currentDefaultAccount = account;
				}
			}
		}
		return currentDefaultAccount;
	}
	
	public static void switchDefaultAccount(Account newDefault){
		if(currentDefaultAccount != null){
			currentDefaultAccount.isDefault = false;
			updateAccount(currentDefaultAccount);
		}
		newDefault.isDefault = true;
		currentDefaultAccount = newDefault;
		updateAccount(newDefault);
		broadcastAccountAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE", newDefault);
	}


	/**
	 * 返回绑定的帐号数目
	 * 
	 * @return
	 */
	public static int getAccountCount() {
		DBHelper dbHelper = DBHelper.getInstance();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
				null, // Columns to return
				null, // SQL WHERE
				null, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				null // SQL ORDER BY
				);
		return cursor.getCount();
	}

	/**
	 * 获取当前已经授权的帐号列表
	 * 
	 * @return
	 */
	public static ArrayList<Account> getAccounts() {
		DBHelper dbHelper = DBHelper.getInstance();
		ArrayList<Account> list = new ArrayList<Account>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE_NAME, // Table Name
				null, // Columns to return
				null, // SQL WHERE
				null, // Selection Args
				null, // SQL GROUP BY
				null, // SQL HAVING
				null // SQL ORDER BY
				);
		if (cursor.moveToFirst()) {
			do {
				Account account = new Account();
				parseCursorToAccount(account, cursor);

				list.add(account);
//				Log.v(TAG, "getAccounts: " + account);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	/****************************** 私有函数 ***************************/

	private static void parseCursorToAccount(Account account, Cursor cursor) {
		int index = 0;
		account.id = cursor.getInt(index++);
		account.uid = cursor.getString(index++);
		account.nick = cursor.getString(index++);
		account.type = cursor.getInt(index++);

		account.openId = cursor.getString(index++);
		account.openKey = cursor.getString(index++);
		account.accessToken = cursor.getString(index++);
		account.refreshToken = cursor.getString(index++);
		account.isDefault = cursor.getInt(index++) == 1;

		account.invalidTime = new Date(cursor.getLong(index++));
		account.authTime = new Date(cursor.getLong(index++));
	}

}

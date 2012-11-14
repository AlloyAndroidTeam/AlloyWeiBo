/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.weibo.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.alloyteam.weibo.AuthActivity;
import com.alloyteam.weibo.MainActivity;
import com.alloyteam.weibo.model.Account;

/**
 * @author azraellong
 * 
 */
public class AccountManager {

	public static Handler authHandler;

	/**
	 * 打开授权页进行授权
	 */
	public static void auth(Context context, int type, Handler handler) {
		if (type == Constants.TENCENT) {
			Intent intent = new Intent(context, AuthActivity.class);
			intent.putExtra("getCodeUrl", Constants.TencentApi.OAUTH_GET_CODE);
			intent.putExtra("getTokenUrl", Constants.TencentApi.OAUTH_GET_ACCESS_TOKEN);
			
			context.startActivity(intent);
		}
	}

	/**
	 * 删除授权
	 */
	public static void removeAuth() {
		
	}

	/**
	 * 获取当前已经授权的帐号列表
	 * 
	 * @return
	 */
	public static ArrayList<Account> getAccounts(DBHelper dbHelper) {
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
				int index = 0;
				Account account = new Account();
				account.uid = cursor.getString(index++);
				account.nick = cursor.getString(index++);
				account.type = cursor.getInt(index++);
				
				account.openId = cursor.getString(index++);
				account.openKey = cursor.getString(index++);
				account.accessToken = cursor.getString(index++);
				account.refreshToken = cursor.getString(index++);
				
				account.invalidTime = new Date(Date.parse(cursor.getString(index++)));
				account.authTime = new Date(Date.parse(cursor.getString(index++)));
				
				
				list.add(account);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
		
		
	}

	/****************************** 私有函数 ***************************/

	

}

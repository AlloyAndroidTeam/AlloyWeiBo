/**
 * @author azraellong
 * @date 2012-11-6
 */
package com.alloyteam.weibo.logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author azraellong
 * 
 */
public class DBHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 9;
	public static final String DB_NAME = "alloyweibo.db";
	public static final String ACCOUNT_TABLE_NAME = "account";

	public static DBHelper dbHelper;
	public static Context dbContext;
	
	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	private DBHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		
	}
	
	public static void init(Context context){
		dbContext = context;
	}
	
	public static DBHelper getInstance(){
		if(dbHelper == null){
			dbHelper = new DBHelper(dbContext);
		}
		return dbHelper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + ACCOUNT_TABLE_NAME
				+ "(id integer primary key autoincrement, "
				+ "uid varchar, "
				+ "nick varchar, "
				+ "type int, "
				+ "openId varchar, "
				+ "openKey varchar, "
				+ "accessToken varchar, "
				+ "refreshToken varchar, "
				+ "isDefault int, "
				+ "invalidTime varchar, " 
				+ "authTime varchar," 
				+ "avatar varchar)";
		db.execSQL(sql);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		//TODO 正式版可不能这么随意删除数据库
//		db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE_NAME);
		db.execSQL("ALTER TABLE " + ACCOUNT_TABLE_NAME + " ADD avatar varchar ");
//		onCreate(db);
		Log.v("dbhelper", "db upgrade");
	}
	
}

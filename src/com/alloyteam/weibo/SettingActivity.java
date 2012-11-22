/**
 * 
 */
package com.alloyteam.weibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * @author pxz
 *
 */
public class SettingActivity extends Activity {
	public static final String TAG = "SettingActivity";
	
	String [] settingItems = new String[] {"帐号绑定"};
	
	ListView.OnItemClickListener onSettingItemClickListener = new ListView.OnItemClickListener() {

		/* (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			switch (position) {
			case 0:
				Intent i = new Intent(SettingActivity.this, AccountManagerActivity.class);
//				i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
				break;

			default:
				break;
			}
			
		}
		
	};
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_setting);
		
		ListView settingListView = (ListView) findViewById(R.id.settingListView);
		settingListView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.setting_list_item, R.id.settingListItem,
				settingItems));
		settingListView.setOnItemClickListener(onSettingItemClickListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
	}

}

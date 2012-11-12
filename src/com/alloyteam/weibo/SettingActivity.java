/**
 * 
 */
package com.alloyteam.weibo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author pxz
 *
 */
public class SettingActivity extends Activity {
	public static final String TAG = "SettingActivity";
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_setting);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
	}

}

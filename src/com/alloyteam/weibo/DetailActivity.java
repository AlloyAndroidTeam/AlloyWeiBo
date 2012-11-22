package com.alloyteam.weibo;

import com.alloyteam.weibo.util.ImageLoader;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;

public class DetailActivity extends Activity {
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.weibo_item);
	}
}

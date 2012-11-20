package com.alloyteam.weibo;


import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/**
 * @author pxz
 *
 */
public class MainActivity extends TabActivity  implements OnClickListener{

	public static final String TAG = "MainActivity"; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.home_title);

		setupTabHost();
		
		Intent intent = new Intent(this, HomeActivity.class);
		setupTab(new TextView(this), "首页",R.drawable.tab_bg_home,intent);
		intent = new Intent(this, SettingActivity.class);
		setupTab(new TextView(this), "设置",R.drawable.tab_bg_home,intent);
		// TODO setup others
		findViewById(R.id.btnHomeTitleAccount).setOnClickListener(this);
		findViewById(R.id.btnHomeTitlePost).setOnClickListener(this);
		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	super.onNewIntent(intent);
    }

	private TabHost mTabHost;

	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
	}

	private void setupTab(final View view, final String tag, int drawable,Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag, drawable);
		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		mTabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text,int drawable) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_item, null);
		TextView tv = (TextView) view.findViewById(R.id.tab_item_text);
		tv.setText(text);
		ImageView iv = (ImageView)view.findViewById(R.id.tab_item_icon);
		iv.setImageResource(drawable);
		return view;
	}
	/**
	 * Yukin:响应click
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent i;
		switch(v.getId()){
    	case R.id.btnHomeTitlePost : //@发表
    		i = new Intent(this, PostActivity.class);
			startActivity(i);
    		break;
    	case R.id.btnHomeTitleAccount : //帐号
    		i = new Intent(this, AccountManagerActivity.class);
			startActivity(i); 
    		break; 
    	}
	}
}

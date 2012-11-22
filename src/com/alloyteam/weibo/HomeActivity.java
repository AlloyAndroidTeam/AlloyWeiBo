package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;

import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author pxz
 * 
 */
public class HomeActivity extends Activity implements OnPullDownListener, OnItemClickListener{
	public static final String TAG = "HomeActivity";
	public ListView mylist;
	public ImageView bigImageView;
	static public ImageLoader imageLoader;
	public boolean isMove=false;
	private PullDownView mPullDownView;
	private static final int WHAT_DID_LOAD_DATA = 0;
	private static final int WHAT_DID_REFRESH = 2;
	private static final int WHAT_DID_MORE = 1;
	private WeiboListAdapter mAdapter;
	private List<Weibo> list;
	private long upTimeStamp=0;
	private long downTimeStamp=0;
	private Account account;
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE".equals(action)){
				initHomeLine();
			}
			else if("com.alloyteam.weibo.WEIBO_ADDED".equals(action)){
				//mPullDownView.initHeaderViewAndFooterViewAndListView(context);
				onRefresh();
			}
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_home);
		imageLoader=new ImageLoader(this);
		initHomeLine();
		IntentFilter intentFilter = new IntentFilter();
		//intentFilter.addAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
		intentFilter.addAction("com.alloyteam.weibo.DEFAULT_ACCOUNT_CHANGE");
		intentFilter.addAction("com.alloyteam.weibo.WEIBO_ADDED");		
		this.registerReceiver(broadcastReceiver, intentFilter);
	}

	public void initHomeLine() {
		mPullDownView = (PullDownView) findViewById(R.id.pull_down_view);
		mPullDownView.setOnPullDownListener(this);
		list = new ArrayList<Weibo>();
		mylist = mPullDownView.getListView();
		mAdapter = new WeiboListAdapter(
				this, list);
		mylist.setAdapter(mAdapter);
		mylist.setOnItemClickListener(this);
		mPullDownView.enableAutoFetchMore(true, 1);
		account = AccountManager.getDefaultAccount();
		if (account == null)
			return;
		loadData(WHAT_DID_LOAD_DATA);
	}
	
	public void pullCallback(int pageflag){
		if(pageflag==WHAT_DID_LOAD_DATA){
			mPullDownView.notifyDidLoad();
		}
		else if(pageflag==WHAT_DID_MORE){
			mPullDownView.notifyDidMore();
		}
		else{				
			mPullDownView.notifyDidRefresh();
		}		
	}
	
	public void loadData(final int pageflag){
		ApiManager.GetHomeLineListener listener=new ApiManager.GetHomeLineListener(){

			@Override
			public void onSuccess(List<Weibo> tmpList) {
				if(tmpList==null){
					pullCallback(pageflag);
					return;
				}
				// TODO Auto-generated method stub
				if(pageflag==WHAT_DID_LOAD_DATA){
					mPullDownView.notifyDidLoad();
					list.addAll(tmpList);							
					if(list.size()>0){
						downTimeStamp=tmpList.get(tmpList.size()-1).timestamp;
						upTimeStamp=tmpList.get(0).timestamp;
					}
					DataManager.set(account.uid,list);
				}
				else if(pageflag==WHAT_DID_MORE){
					mPullDownView.notifyDidMore();								
					list.addAll(tmpList);
					if(tmpList.size()>0){
						downTimeStamp=tmpList.get(tmpList.size()-1).timestamp;
					}
				}
				else{				
					mPullDownView.notifyDidRefresh();
					list.addAll(0, tmpList);
					if(tmpList.size()>0){
						upTimeStamp=tmpList.get(0).timestamp;
					}
				}
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(int type) {
				// TODO Auto-generated method stub
				pullCallback(pageflag);
			}			
		};
		long timeStamp;
		if(pageflag==WHAT_DID_REFRESH){
			timeStamp=upTimeStamp;
		}
		else if(pageflag==WHAT_DID_MORE){
			timeStamp=downTimeStamp;
		}
		else{
			timeStamp=0;
		}
		ApiManager.getHomeLine(account, pageflag, timeStamp, listener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (broadcastReceiver != null) {
			this.unregisterReceiver(broadcastReceiver);
		}
	}

	public void showImage(String url, Bitmap bm){
		Intent intent = new Intent(this, ImageActivity.class);
		intent.putExtra("url", url);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// check and update list(if need)
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, DetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("uid", account.uid);
		bundle.putInt("position", position);
		intent.putExtras(bundle);
		startActivity(intent);		
	}

	@Override
	public void onRefresh() {
		loadData(WHAT_DID_REFRESH);
	}
	
	@Override
	public void onMore() {
		loadData(WHAT_DID_MORE);
	}
	
}

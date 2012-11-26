package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.UserInfo;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.model.Weibo2;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;

import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.ApiManager.ApiResult;

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
	public ImageView bigImageView;
	static public ImageLoader imageLoader;
	public boolean isMove=false;
	public ListView mylist;
	private PullDownView mPullDownView;
	private static final int WHAT_DID_LOAD_DATA = 0;
	private static final int WHAT_DID_REFRESH = 2;
	private static final int WHAT_DID_MORE = 1;
	private WeiboListAdapter mAdapter;
	private List<Weibo2> list;
	private String upId;
	private String downId;
	private Account account;
	private long upTimeStamp=0;
	private long downTimeStamp=0;
	
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
		list = new ArrayList<Weibo2>();
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
		ApiManager.IApiResultListener listener = new ApiManager.IApiResultListener() {
			@Override
			public void onSuccess(ApiResult result) {
				if(result == null || result.weiboList == null){
					pullCallback(pageflag);
					return;
				}
				ArrayList<Weibo2> tmpList = result.weiboList;
				if(pageflag==WHAT_DID_LOAD_DATA){
					mPullDownView.notifyDidLoad();
					if(tmpList.size()>0){
						Weibo2 lastWeibo=tmpList.get(tmpList.size()-1);
						downId=lastWeibo.id;
						downTimeStamp=lastWeibo.timestamp;
						Weibo2 firstWeibo=tmpList.get(0);
						upId=firstWeibo.id;
						upTimeStamp=firstWeibo.timestamp;
						list.addAll(tmpList);							
					}
					DataManager.set(account.uid,list);
				}
				else if(pageflag==WHAT_DID_MORE){
					mPullDownView.notifyDidMore();								
					if(tmpList.size()>0){
						Weibo2 lastWeibo=tmpList.get(tmpList.size()-1);
						downId=lastWeibo.id;
						downTimeStamp=lastWeibo.timestamp;
						list.addAll(tmpList);
					}
				}
				else{				
					mPullDownView.notifyDidRefresh();
					if(tmpList.size()>0){
						Weibo2 firstWeibo=tmpList.get(0);
						upId=firstWeibo.id;
						upTimeStamp=firstWeibo.timestamp;
						list.addAll(0, tmpList);
					}
				}
				mAdapter.notifyDataSetChanged();
			}
			@Override
			public void onError(int errorCode) {
				pullCallback(errorCode);
			}
		};
		String Id;
		long timestamp;
		if(pageflag==WHAT_DID_REFRESH){
			Id=upId;
			timestamp=upTimeStamp;
		}
		else if(pageflag==WHAT_DID_MORE){
			Id=downId;
			timestamp=downTimeStamp;
		}
		else{
			Id="0";
			timestamp=0;
		}
		
		ApiManager.getHomeLine(account, 10, pageflag, timestamp, Id, listener);

	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (broadcastReceiver != null) {
			this.unregisterReceiver(broadcastReceiver);
		}
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
		bundle.putInt("type", account.type);
		bundle.putInt("position", position);//+parent.getFirstVisiblePosition());
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

package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;

import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;

import android.app.Activity;
import android.content.Intent;
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
	
	private Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
		}
	};
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i;
			switch (v.getId()) {
			case R.id.btn_account_manager:
				i = new Intent(HomeActivity.this, AccountManagerActivity.class);
				startActivity(i);
				break;
			case R.id.btn_group:
				// TODO 微博分组暂不处理
				// Intent i = new
				// Intent(HomeActivity.this,AccountManager.class);
				// startActivity(i);
				break;
			case R.id.bigImage:
				bigImageView.setVisibility(View.GONE);
				break;
			case R.id.btn_post:
				i = new Intent(HomeActivity.this, PostActivity.class);
				startActivity(i);
				break;
			case View.NO_ID:// fall through
			default:
			}
		}
	};

	private OnItemClickListener timelineClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO
			// get extra info and transfer to TwittDetailActivity
			// Intent i = new
			// Intent(HomeActivity.this,TwittDetailActivity.class);
			// long twittId = balabala();
			// i.putExtra(TWITT_ID, twittId)
			// startActivity(i);
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_home);
		findViewById(R.id.btn_account_manager).setOnClickListener(listener);
		findViewById(R.id.btn_group).setOnClickListener(listener);
		findViewById(R.id.btn_post).setOnClickListener(listener);
		bigImageView=(ImageView) findViewById(R.id.bigImage);
		bigImageView.setOnClickListener(listener);
		imageLoader=new ImageLoader(this);
		initHomeLine();
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
	
	public void loadData(final int pageflag){
		Bundle params = new Bundle();
		params.putInt("pageflag", pageflag);
		if(pageflag==WHAT_DID_REFRESH){
			params.putLong("pagetime", upTimeStamp);
		}
		else if(pageflag==WHAT_DID_MORE){
			params.putLong("pagetime", downTimeStamp);
		}
		else{
			params.putLong("pagetime", 0);
		}
		params.putInt("reqnum", 10);
		params.putInt("type", 0);
		params.putInt("contenttype", 0);
		params.putString("format", "json");
		params.putLong("t", System.currentTimeMillis());

		ApiManager.requestAsync(account, Constants.Tencent.HOME_TIMELINE,
				params, "GET", new ApiManager.IApiListener() {

					@Override
					public void onJSONException(JSONException exception) {
					}

					public void onFailure(String msg) {

					}

					@Override
					public void onComplete(JSONObject result) {
						Log.d("my","complete");
						Log.d("json",result.toString());
						try {
							List<Weibo> tmpList=new ArrayList<Weibo>();
							if (result.get("data") == JSONObject.NULL){
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
							else{
								JSONObject data = result.getJSONObject("data");
								JSONArray info = data.getJSONArray("info");
								Log.d("json", "parse");
								long tmpDownStamp=0,tmpUpStamp=0;
								for (int i = 0; i < info.length(); ++i) {
									JSONObject item = info.getJSONObject(i);
									int status=item.getInt("status");
									if(status!=0){
										continue;
									}
									String text = item.getString("text");
									String name = item.getString("name");						
									String avatarUrl = item.getString("head")
											+ "/50";
									int type = item.getInt("type");
									Weibo weibo = new Weibo(name, text, avatarUrl);
									long timestamp = item.getLong("timestamp");
									weibo.type = type;
									weibo.timestamp = timestamp;
									if (type == 2) {
										JSONObject source = item
												.getJSONObject("source");
										String text2 = source.getString("text");
										String name2 = source.getString("name");
										String avatarUrl2 = source
												.getString("head") + "/50";
										weibo.mText2 = text2;
										weibo.mAvatarUrl2 = avatarUrl2;
										weibo.mName2 = name2;
										if (source.get("image") != JSONObject.NULL) {
											Log.d("my", "image");
											JSONArray images = source
													.getJSONArray("image");
											weibo.mImage = images.getString(0);
										}
										weibo.count = item.getInt("count");
									} else {
										if (item.get("image") != JSONObject.NULL) {
											Log.d("my", "image");
											JSONArray images = item
													.getJSONArray("image");
											weibo.mImage = images.getString(0);
										}
									}
									tmpList.add(weibo);
									if(i==0){
										tmpUpStamp=timestamp;
									}
									if(i==info.length()-1){
										tmpDownStamp=timestamp;
									}
								}
								if(pageflag==WHAT_DID_LOAD_DATA){
									mPullDownView.notifyDidLoad();
									list.addAll(tmpList);
									upTimeStamp=tmpUpStamp;
									downTimeStamp=tmpDownStamp;
								}
								else if(pageflag==WHAT_DID_MORE){
									mPullDownView.notifyDidMore();								
									list.addAll(tmpList);
									downTimeStamp=tmpDownStamp;
								}
								else{				
									mPullDownView.notifyDidRefresh();
									list.addAll(0, tmpList);
									upTimeStamp=tmpUpStamp;
								}
								mAdapter.notifyDataSetChanged();
							}
							
						} catch (JSONException je) {
							Log.d("json", "error");
						}
					}
				});
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
		Log.i(TAG, "onPause");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
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

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
	private static final int WHAT_DID_REFRESH = 1;
	private static final int WHAT_DID_MORE = 2;
	private WeiboListAdapter mAdapter;
	private List<Weibo> list;

	
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
		getHomeLine();
	}

	public void getHomeLine() {
		mPullDownView = (PullDownView) findViewById(R.id.pull_down_view);
		mPullDownView.setOnPullDownListener(this);
		list = new ArrayList<Weibo>();
		mylist = mPullDownView.getListView();
		mAdapter = new WeiboListAdapter(
				this, list);
		mylist.setAdapter(mAdapter);
		mylist.setOnItemClickListener(this);
		mPullDownView.enableAutoFetchMore(true, 1);
		Account account = AccountManager.getDefaultAccount();
		if (account == null)
			return;

		// final int pageflag, final int pagetime, final int reqnum, final int
		// type, final int contenttype, final String format

		Bundle params = new Bundle();
		params.putInt("pageflag", 0);
		params.putInt("pagetime", 0);
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
						try {
							JSONObject data = result.getJSONObject("data");
							JSONArray info = data.getJSONArray("info");
							Log.d("json", "parse");
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
								list.add(weibo);
							}					
							mAdapter.notifyDataSetChanged();
							mPullDownView.notifyDidLoad();
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

	private void loadData(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<String> strings = new ArrayList<String>();

				Message msg = mUIHandler.obtainMessage(WHAT_DID_LOAD_DATA);
				msg.obj = strings;
				msg.sendToTarget();
			}
		}).start();
	}


	@Override
	public void onRefresh() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				Message msg = mUIHandler.obtainMessage(WHAT_DID_REFRESH);
				msg.obj = "After refresh " + System.currentTimeMillis();
				msg.sendToTarget();
			}
		}).start();
	}
	
	@Override
	public void onMore() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				Message msg = mUIHandler.obtainMessage(WHAT_DID_MORE);
				msg.obj = "After more " + System.currentTimeMillis();
				msg.sendToTarget();
			}
		}).start();
	}
	
	private Handler mUIHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case WHAT_DID_LOAD_DATA:{
					if(msg.obj != null){
						List<String> strings = (List<String>) msg.obj;
						if(!strings.isEmpty()){
							mAdapter.notifyDataSetChanged();
						}
					}
					// 诉它数据加载完毕;
					mPullDownView.notifyDidLoad();
					break;
				}
				case WHAT_DID_REFRESH :{
					String body = (String) msg.obj;
					mAdapter.notifyDataSetChanged();
					// 告诉它更新完毕
					mPullDownView.notifyDidRefresh();
					break;
				}
				
				case WHAT_DID_MORE:{
					String body = (String) msg.obj;
					mAdapter.notifyDataSetChanged();
					// 告诉它获取更多完毕
					mPullDownView.notifyDidMore();
					break;
				}
			}
			
		}
		
	};
}

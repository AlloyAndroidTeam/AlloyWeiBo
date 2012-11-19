package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;

import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.logic.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author pxz
 * 
 */
public class HomeActivity extends Activity {
	public static final String TAG = "HomeActivity";
	public ListView mylist;
	public ImageView bigImageView;
	public ImageLoader imageLoader;
	public boolean isMove=false;

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
		getHomeLine();
		imageLoader=new ImageLoader(this);
		bigImageView.setOnTouchListener(new View.OnTouchListener() {
			float mx, my;

	        public boolean onTouch(View arg0, MotionEvent event) {
	        	//super.onTouchEvent(event);

	            float curX, curY;

	            switch (event.getAction()) {

	                case MotionEvent.ACTION_DOWN:
	                    mx = event.getX();
	                    my = event.getY();
	                    isMove=false;
	                    break;
	                case MotionEvent.ACTION_MOVE:
	                    curX = event.getX();
	                    curY = event.getY();
	                    int moveX=(int)(mx - curX),moveY=(int)(my - curY);
	                    bigImageView.scrollBy(moveX, moveY);
	                    mx = curX;
	                    my = curY;
	                    if(Math.abs(moveX)+Math.abs(moveY)>10)isMove=true;
	                    break;
	                case MotionEvent.ACTION_UP:
	                    curX = event.getX();
	                    curY = event.getY();
	                    bigImageView.scrollBy((int) (mx - curX), (int) (my - curY));
	                    if(!isMove)bigImageView.setVisibility(View.GONE);
	                    break;
	            }

	            return true;
	        }
	    });

	}

	public void getHomeLine() {
		mylist = (ListView) findViewById(R.id.lv_main_timeline);
		mylist.setOnItemClickListener(timelineClickListener);
		Account account = AccountManager.getDefaultAccount();
		final Activity context = this;
		if (account == null)
			return;
		account.getHomeLine(0, 0, 10, 0, 0, "json",
				new HttpConnection.HttpConnectionListener() {
					public void onResponse(boolean success, String result) {
						if (!success)
							return;
						try {
							List<Weibo> list = new ArrayList<Weibo>();
							JSONObject obj = new JSONObject(result);
							JSONObject data = obj.getJSONObject("data");
							JSONArray info = data.getJSONArray("info");
							Log.d("json", "parse");
							for (int i = 0; i < info.length(); ++i) {
								JSONObject item = info.getJSONObject(i);
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
							WeiboListAdapter ila = new WeiboListAdapter(
									context, list);
							mylist.setAdapter(ila);
							TextView text = (TextView) context
									.findViewById(R.id.tv_home_loading);
							text.setVisibility(View.GONE);
						} catch (JSONException je) {
							Log.d("json", "error");
						}
					}
				});
		mylist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View item, int position,
					long id) {
				//进入详情页
			}
		});
	}

	public void showImage(String url, Bitmap bm){
		Intent intent = new Intent(this, ImageActivity.class);
		/*bigImageView.setImageBitmap(bm);
		bigImageView.scrollTo(0, 0);
		bigImageView.setVisibility(View.VISIBLE);
		imageLoader.displayImage(url+"/2000", bigImageView, null);*/
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
}

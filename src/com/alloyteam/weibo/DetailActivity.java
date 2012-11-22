package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.util.WeiboListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DetailActivity extends Activity implements OnPullDownListener, OnClickListener {
	private String uid;
	private int position;
	public ListView mylist;
	private PullDownView mPullDownView;
	private static final int WHAT_DID_LOAD_DATA = 0;
	private static final int WHAT_DID_REFRESH = 2;
	private static final int WHAT_DID_MORE = 1;
	private WeiboListAdapter mAdapter;
	private List<Weibo> list;
	private long upTimeStamp=0;
	private long downTimeStamp=0;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_detail);
		ImageView imageView=(ImageView) findViewById(R.id.image);
		ImageView avatar=(ImageView) findViewById(R.id.avatar);
		TextView textText=(TextView)findViewById(R.id.text);
		TextView nameText=(TextView)findViewById(R.id.name);
		TextView dateText=(TextView)findViewById(R.id.date);
		Intent i=this.getIntent();
		Bundle b=i.getExtras();
		String uid=b.getString("uid");
		this.uid=uid;
		List<Weibo> list=DataManager.get(uid);
		int position = b.getInt("position");
		this.position=position;
		Weibo weibo=list.get(position);
		Log.d("my","id:"+weibo.id);
		String avatarUrl=weibo.avatarUrl;
		HomeActivity.imageLoader.displayImage(avatarUrl+"/50", avatar, null);
		String name=weibo.name;
		nameText.setText(name);
		String text=weibo.text;
		textText.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(text)));
		long date=weibo.timestamp;
		dateText.setText(Utility.formatDate(date*1000));
		if(weibo.type==1){
			ViewGroup source=(ViewGroup)findViewById(R.id.source);
			source.setVisibility(View.GONE);
			if(!weibo.imageUrl.equals("")){
				HomeActivity.imageLoader.displayImage(weibo.imageUrl+"/160", imageView, null);
			}
		}
		else{
			ImageView image2=(ImageView)findViewById(R.id.image2);
			if(!weibo.imageUrl.equals("")){
				HomeActivity.imageLoader.displayImage(weibo.imageUrl+"/160", image2, null);
			}
			TextView text2=(TextView)findViewById(R.id.text2);
			ImageView avatar2=(ImageView)findViewById(R.id.avatar2);
			HomeActivity.imageLoader.displayImage(weibo.avatarUrl2+"/50", avatar2, null);
			text2.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(weibo.text2)));
			TextView name2=(TextView)findViewById(R.id.name2);
			name2.setText(weibo.name2);			
		}
		TextView count=(TextView)findViewById(R.id.count);
		if(weibo.count>0){
		    count.setText("原文被转发"+weibo.count+"次");
		}
		else{
			count.setVisibility(View.GONE);
		}
		findViewById(R.id.re).setOnClickListener(this);
		findViewById(R.id.comment).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		Bundle bundle;
		switch (v.getId()) {
		case R.id.re:
			intent=new Intent(this, PostActivity.class);
			bundle = new Bundle();
			bundle.putString("uid", uid);
			bundle.putInt("type", 1);
			bundle.putInt("position", position);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.comment:
			intent=new Intent(this, PostActivity.class);
			bundle = new Bundle();
			bundle.putString("uid", uid);
			bundle.putInt("type", 2);
			bundle.putInt("position", position);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		default:
			break;
		}
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
	
	public void initList() {
		mPullDownView = (PullDownView) findViewById(R.id.pull_down_view);
		mPullDownView.setOnPullDownListener(this);
		list = new ArrayList<Weibo>();
		mylist = mPullDownView.getListView();
		mAdapter = new WeiboListAdapter(
				this, list);
		mylist.setAdapter(mAdapter);
		mPullDownView.enableAutoFetchMore(true, 1);
		account = AccountManager.getDefaultAccount();
		if (account == null)
			return;
		loadData(WHAT_DID_LOAD_DATA);
	}
		
	public void getCommentList(final int pageflag){
		ApiManager.GetListListener listener=new ApiManager.GetListListener(){

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
}

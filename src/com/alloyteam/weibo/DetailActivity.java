package com.alloyteam.weibo;

import java.util.ArrayList;
import java.util.List;

import com.alloyteam.weibo.AccountManagerActivity.AccountViewHolder;
import com.alloyteam.weibo.PullDownView.OnPullDownListener;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.logic.ApiManager.ApiResult;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo;
import com.alloyteam.weibo.model.Weibo2;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.WeiboListAdapter;

import com.alloyteam.weibo.model.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
	private CommentListAdatper mAdapter;
	private List<Weibo2> list;
	private long upTimeStamp=0;
	private long downTimeStamp=0;
	private int type=0;
	private Weibo2 weibo;
	private Account account;
	private String upId;
	private String downId;
	private ImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_detail);
		ImageView imageView=(ImageView) findViewById(R.id.image);
		ImageView avatar=(ImageView) findViewById(R.id.avatar);
		TextView textText=(TextView)findViewById(R.id.text);
		TextView nameText=(TextView)findViewById(R.id.name);
		TextView dateText=(TextView)findViewById(R.id.date);
		imageLoader=MainActivity.imageLoader;
		Intent i=this.getIntent();
		Bundle b=i.getExtras();
		String uid=b.getString("uid");
		this.uid=uid;
		type=b.getInt("type");
		List<Weibo2> list=DataManager.get(uid);
		int position = b.getInt("position");
		this.position=position;
		weibo=list.get(position);
		Log.d("my","id:"+weibo.id);
		String avatarUrl=weibo.avatarUrl;
		MainActivity.imageLoader.displayImage(avatarUrl, avatar, null);
		String name=weibo.nick;
		nameText.setText(name);
		String text=weibo.text;
		textText.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(text)));
		long date=weibo.timestamp;
		dateText.setText(Utility.formatDate(date));
		if(weibo.source==null){
			ViewGroup source=(ViewGroup)findViewById(R.id.source);
			source.setVisibility(View.GONE);
			if(!weibo.imageUrl.equals("")){
				imageLoader.displayImage(weibo.imageThumbUrl, imageView, null);
			}
		}
		else{
			Weibo2 source = weibo.source;
			ImageView image2=(ImageView)findViewById(R.id.image2);
			if(!source.imageUrl.equals("")){
				imageLoader.displayImage(source.imageThumbUrl, image2, null);
			}
			TextView text2=(TextView)findViewById(R.id.text2);
			ImageView avatar2=(ImageView)findViewById(R.id.avatar2);
			imageLoader.displayImage(source.avatarUrl, avatar2, null);
			text2.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(source.text)));
			TextView name2=(TextView)findViewById(R.id.name2);
			name2.setText(source.nick);			
		}
		TextView count=(TextView)findViewById(R.id.count);
		if(weibo.rebroadcastCount>0){
		    count.setText("原文被转发"+weibo.rebroadcastCount+"次");
		}
		else{
			count.setVisibility(View.GONE);
		}
		if(weibo.isSelf){
			findViewById(R.id.delete).setOnClickListener(this);
		}
		else{
			findViewById(R.id.delete).setVisibility(View.GONE);
		}
		findViewById(R.id.re).setOnClickListener(this);
		findViewById(R.id.comment).setOnClickListener(this);
		findViewById(R.id.reply).setOnClickListener(this);
		findViewById(R.id.image).setOnClickListener(this);
		findViewById(R.id.image2).setOnClickListener(this);
		initList();
	}
	private void re(int type){
		Intent intent;
		Bundle bundle;
		intent=new Intent(this, PostActivity.class);
		bundle = new Bundle();
		bundle.putString("uid", uid);
		bundle.putInt("type", type);
		bundle.putInt("accountType", account.type);
		bundle.putInt("position", position);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.re:
			re(1);
			break;
		case R.id.comment:
			re(2);
			break;
		case R.id.reply:
			re(3);
			break;
		case R.id.image:
			Utility.showImage(this,weibo.imageUrl,null);//);
			break;
		case R.id.image2:
			Utility.showImage(this,weibo.source.imageUrl,null);
			break;
		case R.id.delete:
			deleteWeibo();
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
		mPullDownView = (PullDownView) findViewById(R.id.commentList);
		mPullDownView.setOnPullDownListener(this);
		list = new ArrayList<Weibo2>();
		mylist = mPullDownView.getListView();
		mAdapter = new CommentListAdatper(this, R.layout.comment, list);
		mylist.setAdapter(mAdapter);
		//mPullDownView.enableAutoFetchMore(true, 1);
		account = AccountManager.getAccount(uid, type);
		if (account == null)
			return;
		getCommentList(WHAT_DID_LOAD_DATA);
	}
	public void deleteWeibo(){
		ApiManager.IApiResultListener listener = new ApiManager.IApiResultListener() {
			@Override
			public void onSuccess(ApiResult result) {
				finish();
			}
			@Override
			public void onError(int errorCode) {
				pullCallback(errorCode);
			}
		};
		ApiManager.DeleteWeibo(account, weibo.id, listener);
	}
	public void getCommentList(final int pageflag){
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
		ApiManager.getCommentList(account, 10, pageflag, weibo.id, timestamp, Id, listener);
	}
	@Override
	public void onRefresh() {
		getCommentList(WHAT_DID_REFRESH);
	}
	
	@Override
	public void onMore() {
		getCommentList(WHAT_DID_MORE);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}
	
	class CommentViewHolder {
		ImageView avatar;
		TextView name;
		TextView text;
		TextView date;
	}

	class CommentListAdatper extends ArrayAdapter<Weibo2> {

		List<Weibo2> commentList;

		LayoutInflater layoutInflater;
		
		int mResourceId;

		/**
		 * @param context
		 * @param textViewResourceId
		 * @param list
		 */
		public CommentListAdatper(Context context, int resourceId,
				List<Weibo2> list) {
			super(context, resourceId, list);

			this.layoutInflater = LayoutInflater.from(context);

			this.commentList = list;
			mResourceId=resourceId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CommentViewHolder holder;
			if (convertView == null) {
				holder = new CommentViewHolder();
				convertView = layoutInflater.inflate(mResourceId, null);
				holder.avatar=(ImageView)convertView.findViewById(R.id.avatar);
				holder.name=(TextView)convertView.findViewById(R.id.name);
				holder.text=(TextView)convertView.findViewById(R.id.text);
				holder.date=(TextView)convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			}
			else{
				holder = (CommentViewHolder) convertView.getTag();
			}
			Weibo2 weibo = commentList.get(position);
			imageLoader.displayImage(weibo.avatarUrl, holder.avatar,null);
			holder.name.setText(weibo.nick);
			holder.text.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(weibo.text)));
			holder.date.setText(Utility.formatDate(weibo.timestamp));
			return convertView;
		}

	}
}

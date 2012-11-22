package com.alloyteam.weibo;

import java.util.List;

import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo;

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
import android.widget.TextView;

public class DetailActivity extends Activity implements OnClickListener {
	private ImageView imageView;
	private ImageView avatar;
	private TextView textText;
	private TextView nameText;
	private TextView dateText;
	private String uid;
	private int position;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_detail);
		imageView=(ImageView) findViewById(R.id.image);
		avatar=(ImageView) findViewById(R.id.avatar);
		textText=(TextView)findViewById(R.id.text);
		nameText=(TextView)findViewById(R.id.name);
		dateText=(TextView)findViewById(R.id.date);
		Intent i=this.getIntent();
		Bundle b=i.getExtras();
		String uid=b.getString("uid");
		this.uid=uid;
		List<Weibo> list=DataManager.get(uid);
		int position = b.getInt("position");
		this.position=position;
		Weibo weibo=list.get(position);		
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
}

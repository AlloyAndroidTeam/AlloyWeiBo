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
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity {
	private ImageView imageView;
	private ImageView avatar;
	private TextView textText;
	private TextView nameText;
	private TextView dateText;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_detail);
		imageView=(ImageView) findViewById(R.id.thumbImage);
		avatar=(ImageView) findViewById(R.id.avatar);
		textText=(TextView)findViewById(R.id.text);
		nameText=(TextView)findViewById(R.id.name);
		dateText=(TextView)findViewById(R.id.date);
		Intent i=this.getIntent();
		Bundle b=i.getExtras();
		String uid=b.getString("uid");
		List<Weibo> list=DataManager.get(uid);
		int position = b.getInt("position");
		Weibo weibo=list.get(position);
		if(!weibo.imageUrl.equals("")){
			HomeActivity.imageLoader.displayImage(weibo.imageUrl+"/160", imageView, null);
		}			
		String avatarUrl=weibo.avatarUrl;
		HomeActivity.imageLoader.displayImage(avatarUrl+"/50", avatar, null);
		String name=weibo.name;
		nameText.setText(name);
		String text=weibo.text;
		textText.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(text)));
		long date=weibo.timestamp;
		dateText.setText(Utility.formatDate(date*1000));
	}
}

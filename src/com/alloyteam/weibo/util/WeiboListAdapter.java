package com.alloyteam.weibo.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alloyteam.weibo.DetailActivity;
import com.alloyteam.weibo.HomeActivity;
import com.alloyteam.weibo.MainActivity;
import com.alloyteam.weibo.R;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.model.Weibo2;

public class WeiboListAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	private static final int mResource1 = R.layout.weibo_item;// xml布局文件
	private static final int mResource2 = R.layout.weibo_item_type2;// xml布局文件
	List<Weibo2> mItems;
	ImageLoader imageLoader;
	Context mContext;
	
	public WeiboListAdapter(Context context, List<Weibo2> items) {
		mItems = items;
		mContext=context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader=MainActivity.imageLoader;//new ImageLoader(context);
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Weibo2 weibo = (Weibo2) this.getItem(position);
		return weibo.source==null?0:1;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public ImageLoader.ImageCallback callback=new ImageLoader.ImageCallback(){
		public Bitmap imageLoaded(Bitmap bm, String url){
			return toRoundCorner(bm, 5);
		}
	};

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i;
			switch (v.getId()) {
			case R.id.avatar:
			case R.id.avatar2:
				i = new Intent(mContext, HomeActivity.class);
				Bundle bundle = (Bundle)v.getTag();
				i.putExtras(bundle);
				mContext.startActivity(i);
				break;
			case R.id.image:
			case R.id.image2:
				Bitmap bm=v.getDrawingCache();
				Utility.showImage(mContext,(String)v.getTag(),bm);//+"/2000");
				break;
			default:
				break;
			}
		}
	};
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView = convertView;

		int type = getItemViewType(position);
		Weibo2 weibo = mItems.get(position);
		TextView text;
		TextView name;
		TextView time;
		ImageView avatar;
		ImageView image;

		if (type==0) {
			ViewCache viewCache;
			if (rowView == null) {

				rowView = mInflater.inflate(mResource1, null);

				viewCache = new ViewCache(rowView);

				rowView.setTag(viewCache);

			} else {

				viewCache = (ViewCache) rowView.getTag();

			}
			text = viewCache.getText();
			avatar = viewCache.getAvatar();
			Bundle b=new Bundle();
			b.putString("uid", weibo.uid);
			b.putString("nick", weibo.nick);
			b.putString("avatarUrl", weibo.avatarUrl);
			avatar.setTag(b);
			name = viewCache.getName();
			time = viewCache.getTime();
			image = viewCache.getImage();
			String avatarUrl = weibo.avatarUrl;
			text.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(weibo.text)));
			name.setText(weibo.nick);
			time.setText(Utility.formatDate(weibo.timestamp));
			imageLoader.displayImage(avatarUrl, avatar, callback);
			if(weibo.imageUrl != null && !weibo.imageUrl.equals("")){
				image.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.picture);
				imageLoader.displayImage(weibo.imageThumbUrl, image, null);
				image.setTag(weibo.imageUrl);
			}
			else{
				image.setVisibility(View.GONE);
			}			
			image.setOnClickListener(listener);
			avatar.setOnClickListener(listener);
		} else {
			Weibo2 source = weibo.source;
			TextView text2;
			TextView name2;
			ImageView avatar2;
			TextView count;
			ViewCache2 viewCache;
			if (rowView == null) {

				rowView = mInflater.inflate(mResource2, null);

				viewCache = new ViewCache2(rowView);

				rowView.setTag(viewCache);

			} else {

				viewCache = (ViewCache2) rowView.getTag();

			}
			text = viewCache.getText();
			avatar = viewCache.getAvatar();
			Bundle b=new Bundle();
			b.putString("uid", weibo.uid);
			b.putString("nick", weibo.nick);
			b.putString("avatarUrl", weibo.avatarUrl);
			avatar.setTag(b);
			name = viewCache.getName();
			time = viewCache.getTime();
			String avatarUrl = weibo.avatarUrl;
			if(weibo.text.length()>0){
				text.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(weibo.text)));
				text.setVisibility(View.VISIBLE);
			}
			else{
				text.setVisibility(View.GONE);
			}
			name.setText(weibo.nick);
			time.setText(Utility.formatDate(weibo.timestamp));
			imageLoader.displayImage(avatarUrl, avatar, callback); 
			
			text2 = viewCache.getText2();
			avatar2 = viewCache.getAvatar2();
			Bundle b2=new Bundle();
			b2.putString("uid", source.uid);
			b2.putString("nick", source.nick);
			b2.putString("avatarUrl", source.avatarUrl);
			avatar2.setTag(b2);
			name2 = viewCache.getName2();
			String avatarUrl2 = source.avatarUrl;
			name2.setText(source.nick);
			text2.setText(Html.fromHtml(Utility.htmlspecialchars_decode_ENT_NOQUOTES(source.text)));			
			imageLoader.displayImage(avatarUrl2, avatar2, callback); 
			image=viewCache.getImage();
			count=viewCache.getCount();
			if(source.imageUrl != null && !source.imageUrl.equals("")){
				image.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.picture);
				imageLoader.displayImage(source.imageThumbUrl, image, null);
				image.setTag(source.imageUrl);
			}
			else{
				image.setVisibility(View.GONE);
			}
			if(source.rebroadcastCount != 0){
				count.setVisibility(View.VISIBLE);
				count.setText(String.valueOf(source.rebroadcastCount));
			}
			else{
				count.setVisibility(View.GONE);
			}
			image.setOnClickListener(listener);
			avatar.setOnClickListener(listener);
			avatar2.setOnClickListener(listener);
		}
		return rowView;
	}
}

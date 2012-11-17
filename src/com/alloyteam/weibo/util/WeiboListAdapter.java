package com.alloyteam.weibo.util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.alloyteam.weibo.R;
import com.alloyteam.weibo.model.Weibo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeiboListAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	private static final int mResource1 = R.layout.weibo_item;// xml布局文件
	private static final int mResource2 = R.layout.weibo_item_type2;// xml布局文件
	List<Weibo> mItems;
	ImageLoader imageLoader;
	
	public WeiboListAdapter(Context context, List<Weibo> items) {
		mItems = items;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader=new ImageLoader(context);
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Weibo weibo = (Weibo) this.getItem(position);
		return weibo.type;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 3;
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

	@SuppressWarnings("deprecation")
	public static String formatDate(long timestamp) {
		Date curDate = new Date(System.currentTimeMillis());
		Date date = new Date(timestamp);
		if (curDate.getDate() == date.getDate()) {
			SimpleDateFormat format = new SimpleDateFormat("今天 HH:mm:ss");
			return format.format(date);
		} else if (curDate.getDate() == date.getDate() + 1) {
			SimpleDateFormat format = new SimpleDateFormat("昨天 HH:mm:ss");
			return format.format(date);
		} else if (curDate.getDate() == date.getDate() + 2) {
			SimpleDateFormat format = new SimpleDateFormat("前天 HH:mm:ss");
			return format.format(date);
		} else {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			return format.format(date);
		}
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

    static Hashtable<String, String> html_specialchars_table = new Hashtable<String, String>();
    static {
            html_specialchars_table.put("&lt;","<");
            html_specialchars_table.put("&gt;",">");
            html_specialchars_table.put("&amp;","&");
    }
    static String htmlspecialchars_decode_ENT_NOQUOTES(String s){
            Enumeration<String> en = html_specialchars_table.keys();
            while(en.hasMoreElements()){
                    String key = (String)en.nextElement();
                    String val = (String)html_specialchars_table.get(key);
                    s = s.replaceAll(key, val);
            }
            return s;
    }
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView = convertView;

		int type = getItemViewType(position);
		Weibo weibo = mItems.get(position);
		TextView text;
		TextView name;
		TextView time;
		ImageView avatar;
		ImageView image;

		if (type == 1) {
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
			name = viewCache.getName();
			time = viewCache.getTime();
			image = viewCache.getImage();
			String avatarUrl = weibo.getAvatarUrl();
			avatar.setTag(avatarUrl);
			text.setText(Html.fromHtml(htmlspecialchars_decode_ENT_NOQUOTES(weibo.getText())));
			name.setText(weibo.getName());
			time.setText(formatDate(weibo.timestamp * 1000));
			imageLoader.displayImage(avatarUrl, avatar, callback);
			if(weibo.mImage!=null){
				image.setVisibility(View.VISIBLE);
				imageLoader.displayImage(weibo.mImage+"/160", image, null);
			}
			else{
				image.setVisibility(View.GONE);
			}
		} else if (type == 2) {
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
			name = viewCache.getName();
			time = viewCache.getTime();
			String avatarUrl = weibo.getAvatarUrl();
			avatar.setTag(avatarUrl);
			text.setText(Html.fromHtml(htmlspecialchars_decode_ENT_NOQUOTES(weibo.getText())));
			name.setText(weibo.getName());
			time.setText(formatDate(weibo.timestamp * 1000));
			imageLoader.displayImage(avatarUrl, avatar, callback); 
			text2 = viewCache.getText2();
			avatar2 = viewCache.getAvatar2();
			name2 = viewCache.getName2();
			String avatarUrl2 = weibo.mAvatarUrl2;
			avatar2.setTag(avatarUrl2);
			text2.setText(Html.fromHtml(htmlspecialchars_decode_ENT_NOQUOTES(weibo.mText2)));
			name2.setText(weibo.mName2);
			imageLoader.displayImage(avatarUrl2, avatar2, callback); 
			image=viewCache.getImage();
			count=viewCache.getCount();
			if(weibo.mImage!=null){
				image.setVisibility(View.VISIBLE);
				imageLoader.displayImage(weibo.mImage+"/160", image, null);
			}
			else{
				image.setVisibility(View.GONE);
			}
			if(weibo.count!=0){
				count.setVisibility(View.VISIBLE);
				count.setText(String.valueOf(weibo.count));
			}
			else{
				count.setVisibility(View.GONE);
			}
		}
		return rowView;
	}
}

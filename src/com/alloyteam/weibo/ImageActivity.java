package com.alloyteam.weibo;

import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.GifView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;

public class ImageActivity extends Activity {
	private ImageView image;
	private LayoutParams lp;
	ImageLoadingDialog dialog;
	Display display;
	int displayWidth;
	int displayHeight;
	float startX, startY;
	boolean isDown = false;
	Activity context = this;
	private GifView gf1;
	String url;
	public ImageLoader.ImageCallback callback = new ImageLoader.ImageCallback() {
		public Bitmap imageLoaded(ImageLoader.BitmapInfo bmInfo, String url) {
			dialog.dismiss();
			// 获得Bitmap的高和宽
			Bitmap bm=bmInfo.bm;
			int bmpWidth = bm.getWidth();
			int bmpHeight = bm.getHeight();
			display = getWindowManager().getDefaultDisplay();
			int displayWidth = display.getWidth();
			float scale = (float) (displayWidth) / (float) (bmpWidth);
			Log.d("my", "" + bmpWidth + ":" + display.getWidth() + ":" + scale);
			int scaleHeight = (int) (scale * bmpHeight);
			int displayHeight = display.getHeight();
			Log.d("my", "" + scaleHeight + ":" + display.getHeight());
			// 设置缩小比例
			Matrix mx = new Matrix();
			mx.postScale(scale, scale);
			LayoutParams para;
			para = image.getLayoutParams();
			para.width = displayWidth;
			para.height = Math.max(scaleHeight, displayHeight);
			image.setLayoutParams(para);
			final int maxOffsetY = scaleHeight - displayHeight;
			Log.d("my", "" + maxOffsetY);
			String type=Utility.getType(bmInfo.bytes);
			if (type!=null&&type.equals("GIF")) {
				gf1=(GifView)findViewById(R.id.gif);
				image.setVisibility(View.GONE);
				gf1.setVisibility(View.VISIBLE);
				gf1.setGifImage(bmInfo.bytes);
			} else {
				image.setOnTouchListener(new View.OnTouchListener() {
					float mx, my;

					public boolean onTouch(View arg0, MotionEvent event) {

						float curX, curY;
						int moveX, moveY;

						switch (event.getAction()) {

						case MotionEvent.ACTION_DOWN:
							startX = mx = event.getX();
							startY = my = event.getY();
							isDown = true;
							break;
						case MotionEvent.ACTION_MOVE:
							curX = event.getX();
							curY = event.getY();
							moveX = (int) (mx - curX);
							moveY = (int) (my - curY);
							mx = curX;
							my = curY;
							if (!isDown) {
								isDown = true;
								startX = mx;
								startY = my;
								break;
							}
							if (image.getScrollY() + moveY <= maxOffsetY
									&& image.getScrollY() + moveY >= 0) {
								image.scrollBy(0, moveY);
							}
							break;
						case MotionEvent.ACTION_UP:
							curX = event.getX();
							curY = event.getY();
							moveX = (int) (mx - curX);
							moveY = (int) (my - curY);
							if (image.getScrollY() + moveY <= maxOffsetY
									&& image.getScrollY() + moveY >= 0) {
								image.scrollBy(0, moveY);
							}
							if (Math.abs(curX - startX)
									+ Math.abs(curY - startY) < 10) {
								finish();
							}
							isDown = false;
							break;
						}

						return true;
					}
				});
			}
			return bm;
		}
	};
    @Override
    protected void onPause() {
        super.onPause();
        //因为view有时无法自己结束,所以需要回调方法来处理,保证解析失败或其它状况都可以结束播放线程.
        if(gf1!=null)gf1.stopAnimate();
    }
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 去掉Activity上面的状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_image);
		dialog = new ImageLoadingDialog(this);
		dialog.show();
		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		image = (ImageView) findViewById(R.id.image);
		ImageLoader imageLoader = MainActivity.imageLoader;
		imageLoader.displayImage(url + "/2000", image, callback);

	}
}

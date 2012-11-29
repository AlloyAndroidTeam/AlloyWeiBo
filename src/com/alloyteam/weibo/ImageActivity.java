package com.alloyteam.weibo;

import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.util.ImageLoader;
import com.alloyteam.weibo.util.GifView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
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
			findViewById(R.id.relativeLayout1).setVisibility(View.GONE);
			// 获得Bitmap的高和宽
			Bitmap bm = bmInfo.bm;
			int bmpWidth = bm.getWidth();
			int bmpHeight = bm.getHeight();
			display = getWindowManager().getDefaultDisplay();
			int displayWidth = display.getWidth();
			int displayHeight = display.getHeight();
			float scale = (float) (displayWidth) / (float) (bmpWidth);
			Log.d("my", "" + bmpWidth + ":" + display.getWidth() + ":" + scale);
			int scaleHeight = (int) (scale * bmpHeight);
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
			String type = Utility.getType(bmInfo.bytes);
			if (type != null && type.equals("GIF")) {
				gf1 = (GifView) findViewById(R.id.gif);
				image.setVisibility(View.GONE);
				gf1.setVisibility(View.VISIBLE);
				gf1.setGifImage(bmInfo.bytes);
			} else {
				if (scale > 1) {
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
				} else {
					image.setScaleType(ImageView.ScaleType.MATRIX);
					if(maxOffsetY<0)mx.postTranslate(0, -maxOffsetY/2);
					image.setImageMatrix(mx);					
					image.setOnTouchListener(new View.OnTouchListener() {
						final Matrix matrix = new Matrix();
						final Matrix savedMatrix = new Matrix();

						final int NONE = 0;
						final int DRAG = 1;
						final int ZOOM = 2;
						int mode = NONE;

						final PointF start = new PointF();
						final PointF mid = new PointF();
						float oldDist = 1f;

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							ImageView view = (ImageView) v;

							switch (event.getAction() & MotionEvent.ACTION_MASK) {
							case MotionEvent.ACTION_DOWN:

								matrix.set(view.getImageMatrix());
								savedMatrix.set(matrix);
								start.set(event.getX(), event.getY());
								mode = DRAG;

								break;
							case MotionEvent.ACTION_POINTER_DOWN:
								oldDist = spacing(event);
								if (oldDist > 10f) {
									savedMatrix.set(matrix);
									midPoint(mid, event);
									mode = ZOOM;
								}
								break;
							case MotionEvent.ACTION_UP:
							case MotionEvent.ACTION_POINTER_UP:
								mode = NONE;

								break;
							case MotionEvent.ACTION_MOVE:
								if (mode == DRAG) {
									matrix.set(savedMatrix);
									matrix.postTranslate(
											event.getX() - start.x,
											event.getY() - start.y);
								} else if (mode == ZOOM) {
									float newDist = spacing(event);
									if (newDist > 10f) {
										matrix.set(savedMatrix);
										float scale = newDist / oldDist;
										matrix.postScale(scale, scale, mid.x,
												mid.y);
									}
								}
								break;
							}

							view.setImageMatrix(matrix);
							return true;
						}

						private float spacing(MotionEvent event) {
							float x = event.getX(0) - event.getX(1);
							float y = event.getY(0) - event.getY(1);
							return FloatMath.sqrt(x * x + y * y);
						}

						private void midPoint(PointF point, MotionEvent event) {
							float x = event.getX(0) + event.getX(1);
							float y = event.getY(0) + event.getY(1);
							point.set(x / 2, y / 2);
						}

					});
				}
			}
			return bm;
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		// 因为view有时无法自己结束,所以需要回调方法来处理,保证解析失败或其它状况都可以结束播放线程.
		if (gf1 != null)
			gf1.stopAnimate();
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 去掉Activity上面的状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_image);
		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		image = (ImageView) findViewById(R.id.image);
		ImageLoader imageLoader = MainActivity.imageLoader;
		imageLoader.displayImage(url + "/2000", image, callback);
	}
}

package com.alloyteam.weibo;

import com.alloyteam.weibo.util.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

public class ImageActivity extends Activity {
	private ImageView image;
	private LayoutParams lp;
	ImageLoadingDialog dialog;
	float startX,startY;
	public ImageLoader.ImageCallback callback=new ImageLoader.ImageCallback(){
		public Bitmap imageLoaded(Bitmap bm, String url){
			dialog.dismiss();
			return bm;
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_image);
        dialog = new ImageLoadingDialog(this);
        dialog.show();
		Intent intent = getIntent();
		String url=intent.getStringExtra("url");
		image = (ImageView)findViewById(R.id.image);
		ImageLoader imageLoader=HomeActivity.imageLoader;
		imageLoader.displayImage(url+"/2000", image, callback);
		image.setOnTouchListener(new View.OnTouchListener() {
			float mx, my;

	        public boolean onTouch(View arg0, MotionEvent event) {
	        	//super.onTouchEvent(event);

	            float curX, curY;
	            //int width,height;
	            //width=image.getWidth();
	            //height=image.getHeight();
	            //if(width<image.getParent().)
                int moveX,moveY;

	            switch (event.getAction()) {

	                case MotionEvent.ACTION_DOWN:
	                	startX = mx = event.getX();
	                    startY = my = event.getY();
	                    break;
	                case MotionEvent.ACTION_MOVE:
	                    curX = event.getX();
	                    curY = event.getY();
	                    moveX=(int)(mx - curX);
	                    moveY=(int)(my - curY);
	                    image.scrollBy(0, moveY);
	                    mx = curX;
	                    my = curY;
	                    break;
	                case MotionEvent.ACTION_UP:
	                    curX = event.getX();
	                    curY = event.getY();
	                    moveX=(int)(mx - curX);
	                    moveY=(int)(my - curY);
	                    image.scrollBy(0, moveY);
	                    if(Math.abs(curX-startX)+Math.abs(curY-startY)<10){
	                    	finish();
	                    }
	                    break;
	            }

	            return true;
	        }
	    });
	}
}

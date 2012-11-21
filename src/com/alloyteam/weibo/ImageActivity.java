package com.alloyteam.weibo;

import com.alloyteam.weibo.util.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
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
	float startX,startY;
	public ImageLoader.ImageCallback callback=new ImageLoader.ImageCallback(){
		public Bitmap imageLoaded(Bitmap bm, String url){
			dialog.dismiss();
			//获得Bitmap的高和宽 
			int bmpWidth=bm.getWidth(); 
			int bmpHeight=bm.getHeight();
			display = getWindowManager().getDefaultDisplay();    
			int displayWidth=display.getWidth();
			float scale=(float)(displayWidth)/(float)(bmpWidth);
			Log.d("my",""+bmpWidth+":"+display.getWidth()+":"+scale);
			int scaleHeight=(int)(scale*bmpHeight);
			int displayHeight=display.getHeight();
			Log.d("my", ""+scaleHeight+":"+display.getHeight());   
			//设置缩小比例 
			Matrix mx=new Matrix();
			mx.postScale(scale, scale);
	        LayoutParams para;  
	        para = image.getLayoutParams();
	        para.width=displayWidth;
	        para.height=scaleHeight;
	        image.setLayoutParams(para);
	        if(scale>1){
	        	final int maxOffsetY=scaleHeight-displayHeight;
	        	Log.d("my",""+maxOffsetY);
	    		image.setOnTouchListener(new View.OnTouchListener() {
	    			float mx, my;

	    	        public boolean onTouch(View arg0, MotionEvent event) {

	    	            float curX, curY;
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
	    	                    Log.d("my",""+moveY);
	    	                    if(image.getScrollY()+moveY<=maxOffsetY&&image.getScrollY()+moveY>=0){
	    	                    	image.scrollBy(0, moveY);
	    	                    }	    	                    
	    	                    mx = curX;
	    	                    my = curY;
	    	                    break;
	    	                case MotionEvent.ACTION_UP:
	    	                    curX = event.getX();
	    	                    curY = event.getY();
	    	                    moveX=(int)(mx - curX);
	    	                    moveY=(int)(my - curY);
	    	                    if(image.getScrollY()+moveY<=maxOffsetY&&image.getScrollY()+moveY>=0){
	    	                    	image.scrollBy(0, moveY);
	    	                    }	    	                    
	    	                    if(Math.abs(curX-startX)+Math.abs(curY-startY)<10){
	    	                    	finish();
	    	                    }
	    	                    break;
	    	            }

	    	            return true;
	    	        }
	    	    });
	        }
			return bm;//Bitmap.createBitmap(bm, 0, 0, bmpWidth, bmpHeight, mx, true);
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        //去掉Activity上面的状态栏  
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN,  
                      WindowManager.LayoutParams. FLAG_FULLSCREEN); 
		setContentView(R.layout.activity_image);
        dialog = new ImageLoadingDialog(this);
        dialog.show();
		Intent intent = getIntent();
		String url=intent.getStringExtra("url");
		image = (ImageView)findViewById(R.id.image);
		ImageLoader imageLoader=HomeActivity.imageLoader;
		imageLoader.displayImage(url+"/2000", image, callback);		         
        
	}
}

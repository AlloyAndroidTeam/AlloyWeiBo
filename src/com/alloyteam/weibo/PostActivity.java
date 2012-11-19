/**
 * 
 */
package com.alloyteam.weibo;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author pxz
 *
 */
public class PostActivity extends Activity implements OnClickListener{
	private EditText tvMain;  
	private Button btnBack;
	private Button btnAddPic;
	private Button btnAddFriend;
	private Button btnAddTopic;
	//private PopSelectPhoto popWin;
	private ImageView photoThumb;
	private TextView tvWordCount;
	//private PopFriend popFriend;
	
	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		//自定义标题栏 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_post);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.post_title); 
        
        
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
        				    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); 
        
        /*
        Bundle bundle = getIntent().getExtras();
        Long id = bundle.getLong("id"); 
          
        titlebarTxt.setText("编辑:" + id.toString()); 
       
        
        tvWordCount = (TextView)findViewById(R.id.editWordCount);
        */
        
        tvMain = (EditText)findViewById(R.id.etPostMain);
        
        tvWordCount = (TextView)findViewById(R.id.tvPostCount);
        bindWordCount();
        
        btnBack = (Button)findViewById(R.id.btnPostBack);        
        btnAddPic = (Button)findViewById(R.id.btnPostAddPic);
    	btnAddFriend = (Button)findViewById(R.id.btnPostAddFriend);
    	btnAddTopic = (Button)findViewById(R.id.btnPostAddTopic);
    	btnBack.setOnClickListener(this);
    	btnAddPic.setOnClickListener(this);
    	btnAddFriend.setOnClickListener(this);
    	btnAddTopic.setOnClickListener(this);    	
        
        /*
        btnBack.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}        	
        }); 
		*/
		 
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.v("onClick","" + v.getId());
		switch(v.getId()){
    	case R.id.btnPostAddFriend : //@好友
    		 
    		break;
    	case R.id.btnPostAddPic : //插入图片
   		 	insertPhoto();
    		break;
    	case R.id.btnPostAddTopic : //插入主题
   		 
    		break;
    	case R.id.btnPostBack : //返回
    		finish(); 
    		break;	
    		
    	}
	}
	/*
	 * 绑定数字提示
	 */
	private void bindWordCount(){
		tvMain.addTextChangedListener(new TextWatcher()
        {
                
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                        // TODO Auto-generated method stub 
                	//log("onTextChanged" + s + ",start:" + start + ",before:" +before+ ",length:"+s.length());
                	CharSequence sCount = tvWordCount.getText();                	                	
                	int c = 140 - s.length();
                	if (c <= 0){
                		tvWordCount.setTextColor(Color.rgb(255, 0, 0));
                	}else{
                		tvWordCount.setTextColor(Color.rgb(0, 0, 255));                		
                	}
                	tvWordCount.setText(String.valueOf(c));                	 
                	CharSequence ss = s.subSequence(start, s.length()); 
                }
                
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                        // TODO Auto-generated method stub
                }
                
                public void afterTextChanged(Editable s)
                {
                        // TODO Auto-generated method stub
                }
        });
	}
	
	/*
	 * 插入图片
	 */
	private void insertPhoto(){
		/* 用alert dialog 方式实现*/
		 final String[] arrayFruit = new String[] { "相机拍摄", "手机相册"};
		 
		 Dialog alertDialog = new AlertDialog.Builder(this).
		    setTitle("请选择").
		    setIcon(R.drawable.ic_launcher).
		    setItems(arrayFruit, onPhotoSelectClick).
			    setNegativeButton("取消", new DialogInterface.OnClickListener() {	
				     public void onClick(DialogInterface dialog, int which) {
				      // TODO Auto-generated method stub
				     }
			    }).
		    create();
		  alertDialog.show();	       	
	}
	 /*
     * 响应选择图片dialog选项处理
     */
  private android.content.DialogInterface.OnClickListener onPhotoSelectClick =  new DialogInterface.OnClickListener() {				 
	     public void onClick(DialogInterface dialog, int which) {
	    	 //Log.v("which:", which + "");
	    	 switch (which) {  
	            case 0: 
	            	Intent getImageByCamera  = new Intent("android.media.action.IMAGE_CAPTURE");  
	                startActivityForResult(getImageByCamera, 2);  	            	
	                break;  
	            case 1: 
	            	Intent intent = new Intent();
			        //开启Pictures画面Type设定为image
			        intent.setType("image/*");
			        //使用Intent.ACTION_GET_CONTENT这个Action
			        intent.setAction(Intent.ACTION_GET_CONTENT); 
			        // 取得相片后返回本画面 
				    startActivityForResult(intent, 2);
	                break;  
	            default:  
	                break;  
	            }  
	     }
    };
//    /处理选择图片后返回
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	Log.v("Result", "requestCode:"+requestCode+", result:" + resultCode+",RESULT_OK:"+RESULT_OK);
        switch (resultCode) {  
        case RESULT_OK:  
            if (data != null) { 
            	Log.v("Result", "requestCode:"+requestCode+", result:" + resultCode+",RESULT_OK:"+RESULT_OK);
            	if (photoThumb == null){
	            	photoThumb = new ImageView(this);    	
	            	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(30, 30);
	            	photoThumb.setLayoutParams(lp);
	            	//photo.setBackgroundColor(0xFFFF0000);设置背景    	
	            	((LinearLayout)findViewById(R.id.postStatebar)).addView(photoThumb);
            	}
                //取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意  
                Uri mImageCaptureUri = data.getData();  
                //返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取  
                if (mImageCaptureUri != null) {  
                    Bitmap image;  
                    try {  
                        //这个方法是根据Uri获取Bitmap图片的静态方法  
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);  
                        if (image != null) {  
                        	photoThumb.setImageBitmap(image);  
                        }  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                } else {  
                    Bundle extras = data.getExtras();  
                    if (extras != null) {  
                        //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片  
                        Bitmap image = extras.getParcelable("data");  
                        if (image != null) {  
                        	photoThumb.setImageBitmap(image);  
                        }  
                    }  
                }  
  
            }  
            break;  
        default:  
            break;  
  
        }  
    }  
}

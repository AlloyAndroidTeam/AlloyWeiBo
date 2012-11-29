/**
 * 
 */
package com.alloyteam.weibo;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.net.HttpConnection;
import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.model.Account;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Weibo; 
import com.alloyteam.weibo.model.Weibo2;

import android.app.Activity;
import android.app.AlertDialog; 
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author pxz
 *
 */
public class PostActivity extends Activity implements OnClickListener{
	private EditText tvMain;  
	private Button btnBack;
	private Button btnSave;
	private Button btnAddPic;
	private Button btnAddFriend;
	private Button btnAddTopic;
	//private PopSelectPhoto popWin;
	private ImageView photoThumb;
	private TextView tvWordCount;
	//private PopFriend popFriend;
	private AlertDialog tipsDlg;
	private String picFilePath;
	
	private int type = 0;//操作类型，0写，1转发，2评论, 3回复
	private String tid;
	
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();  //定时关闭
	private Runnable runner;
	
	private PopFriend popFrined; //好友选择窗口
	
	private String SD_CARD_TEMP_DIR; //存储照片图片路径
	
	private String titles[] = {"写微博","转发", "对话", "评论"};
	
	private TextView tvTips;
	
	private Account account;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		//自定义标题栏 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_post);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.post_title);        
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);         
         
      	
        Bundle bundle = getIntent().getExtras(); 
        try{
        	type = bundle.getInt("type");
        }catch(Exception e){
        	type = 0;
        }
        ((TextView)findViewById(R.id.tvPostTitle)).setText(titles[type]);
        

        
        
        tvMain = (EditText)findViewById(R.id.etPostMain); 
        tvTips = (TextView)findViewById(R.id.tvPostTips);
        
        tvWordCount = (TextView)findViewById(R.id.tvPostCount);
        bindWordCount();
        
        btnBack = (Button)findViewById(R.id.btnPostBack);   
        btnSave = (Button)findViewById(R.id.btnPostSave);  
        btnAddPic = (Button)findViewById(R.id.btnPostAddPic);
    	btnAddFriend = (Button)findViewById(R.id.btnPostAddFriend);
    	btnAddTopic = (Button)findViewById(R.id.btnPostAddTopic);
    	
    	 if (type != 0){        
    		String uid = bundle.getString("uid");
    		String myuid = bundle.getString("myuid");
	        int accountType = bundle.getInt("accountType");	        
	        account = AccountManager.getAccount(myuid, accountType); 
	        if (account == null){
	        	tips("获取帐号信息失败，请重试！"); 
	        }
	        
       		List<Weibo2> list=DataManager.get(uid);  
       		Weibo2 weibo=list.get(bundle.getInt("position"));
       		tid = weibo.id;
       		btnAddPic.setVisibility(View.INVISIBLE); 
       		/*
       		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,100);
       		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
       		btnAddFriend.setLayoutParams(params);
       		*/
         	Log.v("post", "" + type +", tid:" +tid+", accountType:" +accountType);
         }else{
        	account = AccountManager.getDefaultAccount();        	 
         }
    	 
    	
    	btnBack.setOnClickListener(this);
    	btnSave.setOnClickListener(this);
    	btnAddPic.setOnClickListener(this);
    	btnAddFriend.setOnClickListener(this);
    	btnAddTopic.setOnClickListener(this);    	
        
    	tvMain.setFocusable(true);
		 
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.v("onClick","" + v.getId());
		switch(v.getId()){
    	case R.id.btnPostAddFriend : //@好友
    		showFriend(v); 
    		break;
    	case R.id.btnPostAddPic : //插入图片
   		 	insertPhoto();
    		break;
    	case R.id.btnPostAddTopic : //插入主题
    		addTopic();
    		break;
    	case R.id.btnPostBack : //返回
    		finish();    		
    		break;	
    	case R.id.btnPostSave : //保存
      		 try {
				save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.v("btnPostSave","保存失败");
				e.printStackTrace();
				tips("操作异常，保存失败！");
				btnSave.setEnabled(true);
			}
    		break;	
    	}
	}
	/*
	 * 绑定数字提示
	 */
	private void bindWordCount(){
		tvMain.addTextChangedListener(new TextWatcher()
        {
                private String beforeTxt;
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
                	//Log.v("beforeTextChanged", "editing:" + editing.toString()+", s:"+s);
                	/*
                	if (defaultText.equals(s.toString())){//if (!editing){
                		//tvMain.setText("xxxx");
                		tvMain.setTextColor(Color.rgb(0, 0, 0));
                		editing = true;
                		Log.v("beforeTextChanged", "in editing:" + editing.toString()+", s:"+s);
                	}  
                	beforeTxt = s.toString();*/
                }
                
                public void afterTextChanged(Editable s)
                {
                	/*
                	if (s.length() == 0){
                		editing = false;
                		tvMain.setText(defaultText);
                		tvMain.setTextColor(Color.rgb(192, 192, 192));
                		Log.v("afterTextChanged", "in:editing:" + editing.toString()+", s:"+s + ",l:"+s.length());
                	} 
                	Log.v("afterTextChanged", "editing:" + editing.toString()+", s:"+s + ",l:"+s.length());
                	*/ 
                	if (s.length() == 0){
                		tvTips.setVisibility(View.VISIBLE);
                	}else{
                		tvTips.setVisibility(View.INVISIBLE);
                	}
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
		  
		  Log.v("getCacheDir",  this.getCacheDir().getName());
		 
	}
	 /*
     * 响应选择图片dialog选项处理
     */
  private android.content.DialogInterface.OnClickListener onPhotoSelectClick =  new DialogInterface.OnClickListener() {				 
	     public void onClick(DialogInterface dialog, int which) {
	    	 //Log.v("which:", which + "");
	    	 switch (which) {  
	            case 0: 
	            	
	            	/* 
	                	
	            	Intent getImageByCamera  = new Intent("android.media.action.IMAGE_CAPTURE"); 
	            	startActivityForResult(getImageByCamera, 2);
	            	*/ 
	            	/*
	                try{
	                	//final String start = Environment.getExternalStorageState(); 
		                final String start = Environment.getExternalStorageState();
		                final String PHOTOPATH = "/photo/";  
		                if(start.equals(Environment.MEDIA_MOUNTED)){ 
			                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
			                File file = new File(Environment.getExternalStorageDirectory()+PHOTOPATH); 
			                if(!file.exists()){  
			                	file.mkdirs();  
			                };	                
			                String tempphontname = System.currentTimeMillis()+".jpg";  
			                StringBuffer buffer = null;
			                buffer.append(Environment.getExternalStorageDirectory()+PHOTOPATH).append(tempphontname); 
			                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(buffer.toString())));  
			                startActivityForResult(intent, 2);  
		                }else{
		                	Toast.makeText(PostActivity.this,	                		
		                       "没有SD卡1", Toast.LENGTH_LONG).show();
		           
		            	}
	            	}catch(Exception e){
	                	Toast.makeText(PostActivity.this,	                		
	 	                       "没有SD卡2", Toast.LENGTH_LONG).show();
	                }*/
	            	  /* */
	            	 
	            	  
	            	  
	            	  
                	//final String start = Environment.getExternalStorageState(); 
	                final String start = Environment.getExternalStorageState();	                 
	                if(start.equals(android.os.Environment.MEDIA_MOUNTED)){ 
	                	 SD_CARD_TEMP_DIR = Environment.getExternalStorageDirectory() 
        	  					 			+ File.separator + "weito.jpg";        	   
		            	  Intent takePictureFromCameraIntent = new Intent(
		            			  	MediaStore.ACTION_IMAGE_CAPTURE);
		            	  takePictureFromCameraIntent.putExtra(
		            	    android.provider.MediaStore.EXTRA_OUTPUT, Uri
		            	      .fromFile(new File(SD_CARD_TEMP_DIR)));
		            	  startActivityForResult(takePictureFromCameraIntent, 2);
	                }else{
	                	Toast.makeText(PostActivity.this,	                		
	                       "没有SD卡", Toast.LENGTH_LONG).show();
	           
	            	}
	                Log.v("start", start);
		            	 
					
	                /*
	                Intent intent = new Intent();  
	                ContentValues values = new ContentValues();  
	                Uri photoUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
	                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);  
	                startActivityForResult(intent, 2);  
	                */
	            	/*
	            	Uri imageFileUri = getContentResolver().insert(  
	            			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());  
        			 
        			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);  
        			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);  
        			startActivityForResult(i, 2); 
	            			*/
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
	            	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(50, 50);
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
                        //picPath
                        picFilePath = getPicPath(mImageCaptureUri);
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                } else {  
                	picFilePath = SD_CARD_TEMP_DIR;
                    Bundle extras = data.getExtras();  
                    if (extras != null) {  
                        //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片  
                        Bitmap image = extras.getParcelable("data");  
                        if (image != null) {  
                        	photoThumb.setImageBitmap(image);  
                        } 
                        /*
                        //保存到相册
                        ContentResolver cr = getContentResolver();                          
                        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(cr, image, "myPhoto", "alloy android"));
                        picFilePath = getPicPath(uri);
                        */
                        
                    }  
                    Log.v("onActivityResult", "from Bundle extras" + picFilePath);
                }  
  
            }  
            break;  
        default:  
            break;  
  
        }  
    } 
    private String getPicPath(Uri originalUri){
    	//Uri originalUri = data.getData();        //获得图片的uri          
        //这里开始的第二部分，获取图片的路径：   
        String[] proj = {MediaStore.Images.Media.DATA};          
        Cursor cursor = managedQuery(originalUri, proj, null, null, null);   
        //按我个人理解 这个是获得用户选择的图片的索引值   
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);             
        cursor.moveToFirst();  
        //最后根据索引值获取图片路径  
        String path = cursor.getString(column_index);  	
    	return path;
    }
    /**
     * 发布
     * @throws Exception 
     */
    private void save() throws Exception{    
    	 if (account == null){
    		 return ;
    	 }
    	 String content = tvMain.getText().toString();
//    	 if (content.length() > 140){
//    		 return;
//    	 }
    	 if ((type != 1 && content.length() == 0) ||
    			 content.length() > 140){
    		 return;
    	 }
    	 btnSave.setEnabled(false);
    	 final Context _context = this;
    	 ApiManager.IApiListener listener =  new ApiManager.IApiListener() {

				@Override
				public void onJSONException(JSONException exception) {
					Log.d("add", "onJSONException");
					tips("发送失败，请重试！");
					btnSave.setEnabled(true);
				}

				public void onFailure(String msg) {
					Log.d("add", "onFailure");
					tips("发送失败，请重试！");
					btnSave.setEnabled(true);
				}

				@Override
				public void onComplete(JSONObject result) {
					Log.d("add", "onComplete"+result.toString());
					String checkTxt = "发送失败，请重试！";					 					 
					checkTxt = ApiManager.checkResult(account, result); 
					if (checkTxt.equals("0")){												
						//tips("发送成功.");
						Intent intent = new Intent();						    	
				    	intent.setAction("com.alloyteam.weibo.WEIBO_ADDED"); 					
						_context.sendBroadcast(intent);								 
						finish();
					}else{								
						tips(checkTxt);
					} 					 
					btnSave.setEnabled(true);
				}
			};
		
		//Account account = AccountManager.getDefaultAccount(); 
		
		////操作类型，0写，1转发，2评论
		switch(type){
			case 1:
				ApiManager.readd(account, tid, content, listener);  
				break;
			case 2:
				ApiManager.reply(account, tid, content, listener);
				break;
			case 3:
				ApiManager.comment(account, tid, content, listener);
				break;	
			default:
				if (picFilePath != null){
					ApiManager.add(account, content, picFilePath, listener);  
				 }else{
					 ApiManager.add(account, content, listener); 
				 }
		}	
		
    }
    
    
     
	/**
	 * 提示
	 */
	private void tips(String msg){
		if (tipsDlg == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);  
			builder. setTitle("提示").setIcon(R.drawable.ic_launcher);
			tipsDlg = builder.create();
		}
		tipsDlg.setMessage(msg);
		tipsDlg.show();
		autoClose(2000);
	}
	/**
	 * 延时关闭 
	 */
	public void autoClose(long duration){  
        //创建自动关闭任务
		if (runner == null){
	        runner = new Runnable() {  
	            @Override  
	            public void run() {  
	            	if (tipsDlg != null){	            		 
	            		tipsDlg.dismiss();
	            	}
	            }  
	        };  
		}
        //新建调度任务  
        executor.schedule(runner, duration, TimeUnit.MILLISECONDS);            
    }  
	
	
	/**
	 * #话题
	 */
	private void addTopic(){
		int index = tvMain.getSelectionStart();//获取光标所在位置
		 
		String text="#请在这里输入话题#";
		 
		Editable edit = tvMain.getEditableText();//获取EditText的文字		
		if (index < 0 || index >= edit.length() ){
			edit.append(text);	
		}else{
			edit.insert(index,text);//光标所在位置插入文字			
		}
		tvMain.setSelection(index+1, index + text.length() - 1);		
	}
	
	/**
	 * @好友
	 */	 
	private void showFriend(View v){
		popFrined = new PopFriend(this);				
		LayoutInflater inflater = LayoutInflater.from(this);
		//popFrined.setHeight(height)
		popFrined.showAtLocation(v, Gravity.BOTTOM, 0, 0);
		//popFrined.setOnDismissListener(onDismissListener)
		//popFrined.showAsDropDown(btnAddFriend, 0, (- popFrined.getHeight() - 30));
		
	}
	 
	
	public void setExitText(String atTxt){
		Editable edit = tvMain.getEditableText();
		edit.append(atTxt);
	}
	
	
	
	
}

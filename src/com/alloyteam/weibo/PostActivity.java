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
import com.alloyteam.weibo.model.Weibo; 

import android.app.Activity;
import android.app.AlertDialog; 
import android.app.Dialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
	
	private int type = 0;//操作类型，0写，1转发，2评论
	private String tid;
	
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();  //定时关闭
	private Runnable runner;
	
	private PopFriend popFrined;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		//自定义标题栏 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_post);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.post_title);        
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);         
        
        /*
        Bundle bundle = getIntent().getExtras();
        Long id = bundle.getLong("id");           
        titlebarTxt.setText("编辑:" + id.toString());         
        tvWordCount = (TextView)findViewById(R.id.editWordCount);
        */
        Bundle bundle = getIntent().getExtras();
        try{
        	type = bundle.getInt("type");
        }catch(Exception e){
        	type = 0;
        }
        if (type != 0){        	
        	tid = bundle.getString("tid");
        }
        tvMain = (EditText)findViewById(R.id.etPostMain);
        
        tvWordCount = (TextView)findViewById(R.id.tvPostCount);
        bindWordCount();
        
        btnBack = (Button)findViewById(R.id.btnPostBack);   
        btnSave = (Button)findViewById(R.id.btnPostSave);  
        btnAddPic = (Button)findViewById(R.id.btnPostAddPic);
    	btnAddFriend = (Button)findViewById(R.id.btnPostAddFriend);
    	btnAddTopic = (Button)findViewById(R.id.btnPostAddTopic);
    	
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
	            	/*
	            	Intent getImageByCamera  = new Intent("android.media.action.IMAGE_CAPTURE");  
	                startActivityForResult(getImageByCamera, 2);  
	                */	
	                try{
	                	//final String start = Environment.getExternalStorageState();
	                
	            	/**/
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
	                }
					
	                /*
	                Intent intent = new Intent();  
	                ContentValues values = new ContentValues();  
	                Uri photoUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
	                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);  
	                startActivityForResult(intent, 2);  
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
                        //picPath
                        picFilePath = getPicPath(mImageCaptureUri);
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
                    Log.v("onActivityResult", "from Bundle extras");
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
    	 String content = tvMain.getText().toString();
    	 if (content == null || content.length() == 0 || content.length() > 140){
    		 return;
    	 }
    	 btnSave.setEnabled(false);
    	 final Context _context = this;
    	 ApiManager.IApiListener listener =  new ApiManager.IApiListener() {

				@Override
				public void onJSONException(JSONException exception) {
					Log.d("add", "onJSONException");
				}

				public void onFailure(String msg) {
					Log.d("add", "onFailure");
				}

				@Override
				public void onComplete(JSONObject result) {
					Log.d("add", "onComplete"+result.toString()); 
					try {							 
						String errcode = result.getString("errcode");
						String ret = result.getString("ret");
						if (errcode.equals("0")){												
							//tips("发送成功.");
							Intent intent = new Intent();						    	
					    	intent.setAction("com.alloyteam.weibo.WEIBO_ADDED"); 					
							_context.sendBroadcast(intent);								 
							finish();
						}else{								
							checkErrcode(Integer.parseInt(ret), Integer.parseInt(errcode));//tips("发送失败，请重试！");
						} 
						 
					} catch (JSONException je) {
						Log.d("json", "error");
					}
					btnSave.setEnabled(true);
				}
			};
		 if (picFilePath != null){
			 addPic("json", content + "addPic", "127.0.0.1", listener);
		 }else{
			 add("json", content + "add", "127.0.0.1", listener);
		 }
    }
    
    /**
     * 检查返回的错误
     */
    private void checkErrcode(int ret, int errcode){
    	String txt = "发送失败，请重试！";
    	  
		 switch(errcode){
	 		case 1:
	 			txt = "必须为用户侧真实ip";
	 			break;
	 		case 2:
	 			txt = "微博内容超出长度限制";
	 			break;
// 		 		case 3:
// 		 			txt = "经度值错误";
// 		 			break;
// 		 		case 4:
// 		 			txt = "纬度值错误";
// 		 			break;
	 		case 3:
	 			txt = "格式错误、用户无效";
	 			break;	
	 		
	 		case 4:
	 			txt = "有过多脏话";
	 			break;
	 		case 5:
	 			txt = "禁止访问，如城市，uin黑名单限制等";
	 			break;
	 		case 9:
	 			if (ret == 1){
	 				txt = "图片大小超出限制或为0";
	 			}else{
	 				txt = "包含垃圾信息";
	 			}
	 			break;
	 		case 10:
	 			if (ret == 1){
	 				txt = "图片格式错误，目前仅支持gif、jpeg、jpg、png、bmp及ico格式";
	 			}else{
	 				txt = "发表太快";
	 			}
	 			
	 			break;
	 		case 12:
	 			txt = "源消息审核中";
	 			break;	
	 		case 13:
	 			txt = "重复发表";
	 			break;
	 		case 14:
	 			txt = "未实名认证";
	 			break;
	 		case 16:
	 			txt = "服务器内部错误导致发表失败";
	 			break;
	 		case 15: 
	 			
	 		case 1001:
	 			txt = "公共uin黑名单限制";
	 			break;
	 		case 1002:
	 			txt = "公共IP黑名单限制";
	 			break;
	 		case 1003:
	 			txt = "微博黑名单限制";
	 			break;
	 		case 1004:
	 			txt = "单UIN访问微博过快";
	 			break;	
	 		case 1472:
	 			txt = "服务器内部错误导致发表失败";
	 			break;
 		 }
    	  
    	 tips(txt);
    }
    
    /**
	 * 发送微博，文字
	 */
	public void add(String format, String content,
			String clientip, final ApiManager.IApiListener listener) throws Exception {
		
		 Account account = AccountManager.getDefaultAccount();    	 
         Bundle params = new Bundle(); 
         params.putString("format", format);
         params.putString("content", content);     
         params.putString("longitude", "");
         params.putString("syncflag", "1");          
         ApiManager.requestAsync(account, Constants.Tencent.T_ADD,
					params, "POST", listener);         
	}
	 /**
		 * 转发微博，文字
		 */
		public void reply(String format, String content,
				String clientip, final ApiManager.IApiListener listener) throws Exception {
			
			 Account account = AccountManager.getDefaultAccount();    	 
	         Bundle params = new Bundle(); 
	         params.putString("format", format);
	         params.putString("content", content);     
	         params.putString("longitude", "");
	         params.putString("syncflag", "1");          
	         ApiManager.requestAsync(account, Constants.Tencent.T_ADD,
						params, "POST", listener);         
		}
		 /**
		 * 评论微博，文字
		 */
		public void readd(String format, String content,
				String clientip, final ApiManager.IApiListener listener) throws Exception {
			
			 Account account = AccountManager.getDefaultAccount();    	 
	         Bundle params = new Bundle(); 
	         params.putString("format", format);
	         params.putString("content", content);     
	         params.putString("longitude", "");
	         params.putString("syncflag", "1");          
	         ApiManager.requestAsync(account, Constants.Tencent.T_ADD,
						params, "POST", listener);         
		}
	
	/**
	 * 发表一条带图片的微博
	 * 
	 * @param oAuth
	 * @param format 返回数据的格式 是（json或xml）
	 * @param content  微博内容
	 * @param clientip 用户IP(以分析用户所在地)
	 * @param jing 经度（可以填空）
	 * @param wei 纬度（可以填空）
	 * @param picpath 可以是本地图片路径 或 网络地址
	 * @param syncflag  微博同步到空间分享标记（可选，0-同步，1-不同步，默认为0）  
	 * @return
	 * @throws Exception
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%BE%AE%E5%8D%9A%E7%9B%B8%E5%85%B3/%E5%8F%91%E8%A1%A8%E4%B8%80%E6%9D%A1%E5%B8%A6%E5%9B%BE%E7%89%87%E7%9A%84%E5%BE%AE%E5%8D%9A">腾讯微博开放平台上关于此条API的文档1-本地图片</a>
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%BE%AE%E5%8D%9A%E7%9B%B8%E5%85%B3/%E7%94%A8%E5%9B%BE%E7%89%87URL%E5%8F%91%E8%A1%A8%E5%B8%A6%E5%9B%BE%E7%89%87%E7%9A%84%E5%BE%AE%E5%8D%9A">腾讯微博开放平台上关于此条API的文档2-网络图片</a>
	 */
	public void addPic(String format, String content,
			String clientip,  final ApiManager.IApiListener listener)
			throws Exception {
		Account account = AccountManager.getDefaultAccount();
    	 
        Bundle params = new Bundle(); 
        params.putString("format", format);
        params.putString("content", content);         
        params.putString("clientip", clientip);
        params.putString("longitude", "");
        params.putString("syncflag", "1");
        params.putString("compatibleflag", "0"); 
        ApiManager.postAsync(account, Constants.Tencent.T_ADD_PIC, params, picFilePath, listener);
        
	};
	/**
	 * 提示
	 */
	private void tips(String msg){
		if (tipsDlg == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this); 
			//tipsDlg = new AlertDialog.Builder(this);	
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
		//popFrined.showAsDropDown(btnAddFriend, 0, (- popFrined.getHeight() - 30));
		
	}
	 
	
	
	
	
	
}

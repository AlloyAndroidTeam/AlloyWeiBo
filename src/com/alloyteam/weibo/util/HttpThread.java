package com.example.my;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpThread extends Thread {
	private Handler mHandler;
	private Activity mActivity;
	
	public HttpThread(Handler handler, String url, Activity activity){
		mHandler=handler;
		mActivity=activity;
	}
	
	@Override
	public void run() // 线程处理的内容
	{
		Log.d("run","start");
		String s=getFromAssets(mActivity,"weibo.json");
		Message msg=mHandler.obtainMessage(0, s);
		mHandler.sendMessage(msg);
	}
	

    public static class MyHandler extends Handler {
    	final HttpCallback mHttpCallback;

        MyHandler(final HttpCallback httpCallback) {
        	mHttpCallback=httpCallback;
        }

        @Override
        public void handleMessage(Message msg) {
        	try{
        		Log.d("run","parse");
        		JSONObject obj = new JSONObject((String) msg.obj);
        		mHttpCallback.onResponse(obj);
        	}
        	catch (JSONException je)  
            {  
                Log.d("json","error");
            }  
        }
    };
    
    public interface HttpCallback {

        public void onResponse(JSONObject js);

    }

	
    public String getFromAssets(Activity activity,String fileName){ 
        try { 
            InputStreamReader inputReader = new InputStreamReader( activity.getResources().getAssets().open(fileName) ); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "";
        }
    }
}

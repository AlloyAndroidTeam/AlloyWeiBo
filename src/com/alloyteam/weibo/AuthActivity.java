package com.alloyteam.weibo;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.model.Account;

public class AuthActivity extends Activity {

	static final String TAG = "AuthActivity";
	
	WebView webView;
	
	ProgressBar progressBar;
	
	int accountType;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);	
        
        webView = (WebView) findViewById(R.id.authWebView);
        
        progressBar = (ProgressBar) findViewById(R.id.authProgressBar);
        
        accountType = this.getIntent().getIntExtra("type", Constants.TENCENT);
        String url = "";
        if(accountType == Constants.TENCENT){
        	url = Constants.Tencent.OAUTH_GET_ACCESS_TOKEN +
        			"?client_id=" + Constants.Tencent.APP_KEY +
            		"&response_type=token" +
            		"&redirect_uri=" + Constants.Tencent.REDIRECT_URL;
        }
//        url = "https://www.google.com.hk/";
//        url = "http://www.google.com.hk/";
//        System.out.println(url);
        Log.v(TAG, "load: " + url);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
//				Log.v(TAG, "onPageStarted url: " + url);
				if(url.indexOf(Constants.Tencent.REDIRECT_URL) == 0){
					view.stopLoading();
					progressBar.setVisibility(View.GONE);
					handleAuthResult(view, url);
					return;
				}
				progressBar.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}
			
        	
        });
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_auth, menu);
        return true;
    }
    
    private void handleAuthResult(WebView view, String url){
//    	Log.v(TAG, "handleAuthResult: " + url);
    	Bundle values = Utility.parseUrl(url, "#");
    	
    	Activity context = AuthActivity.this;
		//Intent intent = new Intent();
		//context.setResult(accountType, intent);
    	
    	String uid =  values.getString("name");
    	if(AccountManager.exists(uid, accountType)){
    		Toast.makeText(AuthActivity.this, "该帐号已绑定，请勿重复绑定", Toast.LENGTH_SHORT).show();
    		context.finish();
    		return;
    	}
    	Account account = new Account();
    	account.type = accountType;
    	account.uid = uid;
    	
    	account.accessToken = values.getString("access_token");
    	account.refreshToken = values.getString("refresh_token");
    	account.nick = values.getString("nick");
    	
    	account.openId = values.getString("openid");
    	account.openKey = values.getString("openkey");
    	
    	account.authTime = new Date();
		account.invalidTime = new Date(account.authTime.getTime()
				+ Integer.parseInt(values.getString("expires_in")) * 1000);
    	Log.i(TAG, account.authTime.getTime() + " : " + values.getString("expires_in"));
    	
    	account.isDefault = AccountManager.getAccountCount() == 0;
    	
    	AccountManager.addAccount(account);
    	
    	Intent intent = new Intent();
    	
    	intent.setAction("com.alloyteam.weibo.NEW_ACCOUNT_ADD");
    	
		intent.putExtra("action", "addAccount");
		intent.putExtra("uid", account.uid);
		intent.putExtra("type", account.type);
		
		context.sendBroadcast(intent);
		
//		startActivity(intent);
		context.finish();
    }
}

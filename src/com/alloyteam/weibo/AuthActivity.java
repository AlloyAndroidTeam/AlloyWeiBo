package com.alloyteam.weibo;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.Constants;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.model.Account;

public class AuthActivity extends Activity {

	static final String TAG = "AuthActivity";

	WebView webView;

	ProgressBar progressBar;

	int accountType;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		webView = (WebView) findViewById(R.id.authWebView);

		progressBar = (ProgressBar) findViewById(R.id.authProgressBar);

		accountType = this.getIntent().getIntExtra("type", Constants.TENCENT);
		String url = Constants.getAuthUrl(accountType);  
		Log.v(TAG, "load: " + url);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.v(TAG, "onPageStarted url: " + url);
				String redirectUrl = Constants.getRedirectUrl(accountType);
				if (url.indexOf(redirectUrl) == 0) {
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
			
			/* (non-Javadoc)
			 * @see android.webkit.WebViewClient#onReceivedSslError(android.webkit.WebView, android.webkit.SslErrorHandler, android.net.http.SslError)
			 */
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			    // TODO Auto-generated method stub
			    super.onReceivedSslError(view, handler, error);
			    handler.proceed();
			}

		});
		webView.loadUrl(url);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
	    // TODO Auto-generated method stub
	    super.onDestroy();
	    webView.destroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_auth, menu);
		return true;
	}

	private void handleAuthResult(WebView view, String url) {
		Bundle values = Utility.parseUrl(url, "#");
		Activity context = AuthActivity.this;
		String uid = values.getString("uid");
		if(uid == null){
			uid = values.getString("name");
		}
		
		Account old = AccountManager.getAccount(uid, accountType);
		
		Account account = new Account();
		account.type = accountType;
		account.uid = uid;
		account.nick = values.getString("nick");
		if(account.nick == null){
			account.nick = account.uid;
		}
		
		account.refreshToken = values.getString("refresh_token");
		account.openId = values.getString("openid");
		account.openKey = values.getString("openkey");
		
		account.accessToken = values.getString("access_token");
		
		account.authTime = new Date();
		long expiresTime = Long.parseLong(values.getString("expires_in"));
//		if(accountType == Constants.TENCENT){
			expiresTime *= 1000;
//		}
		account.invalidTime = new Date(account.authTime.getTime()
				+  expiresTime);
		Log.i(TAG,
				account.authTime.getTime() + " : "
						+ values.getString("expires_in"));
		int accountCount = AccountManager.getAccountCount();
		if(old != null){
			account.isDefault = old.isDefault;
		}else{
			account.isDefault = accountCount == 0;
		}
		
//		Intent intent = new Intent();
//		intent.putExtra("uid", account.uid);
//		intent.putExtra("type", account.type);
		if (old != null) {
//			Toast.makeText(AuthActivity.this, "该帐号已绑定，请勿重复绑定",
//					Toast.LENGTH_SHORT).show();
			AccountManager.updateAccount(account);
		}else {
			AccountManager.addAccount(account);
		}
		if(accountCount == 0){
			Intent intent = new Intent(this, MainActivity.class);
			context.startActivity(intent);
		}
		context.finish();
	}
}

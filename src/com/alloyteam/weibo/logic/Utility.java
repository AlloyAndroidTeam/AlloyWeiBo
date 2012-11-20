/**
 * @author azraellong
 * @date 2012-11-16
 */
package com.alloyteam.weibo.logic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.util.Log;

/**
 * @author azraellong
 * 
 */
public class Utility {

	public static Bundle parseUrl(String url) {
		return parseUrl(url, "?");
	}

	public static Bundle parseUrl(String url, String queryStart) {
		Bundle values = new Bundle();
		int index = url.indexOf(queryStart);
		if (index != -1) {
			url = url.substring(index + 1);
		}
		String[] arr = url.split("&");
		for (String str : arr) {
			String[] kv = str.split("=");
			String key, value;
			if (kv.length == 1) {
				key = kv[0];
				value = "";
			} else if (kv.length > 1) {
				key = kv[0];
				value = kv[1];
			} else {

				continue;
			}
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			values.putString(key, value);
		}
		return values;
	}

	public static String toQueryString(Bundle parameters) {
		if (parameters == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			Object parameter = parameters.get(key);
			String value;
			if (parameter instanceof Integer || parameter instanceof Long){
				value = parameter + "";
			}else if( parameter instanceof String) {
				value = (String) parameter;
			}else{
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			sb.append(URLEncoder.encode(key) + "="
					+ URLEncoder.encode(value));
		}
		return sb.toString();
	}

	public static ArrayList<NameValuePair> toPostData(Bundle bundle) {
		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
		for (String key : bundle.keySet()) {
			data.add(new BasicNameValuePair(key, bundle.getString(key)));
		}
		return data;
	}

	public static String request(String url, String method, Bundle params)
			throws IOException {
		String result = "";
		HttpClient httpClient = getHttpClient();
		HttpUriRequest request = null;

		if (method.equals("GET")) {
			String encodedParam = toQueryString(params);

			if (!url.endsWith("?"))
				url = url + "?";
			url = url + encodedParam;
			Log.i("http request", "get: " + url);
			request = new HttpGet(url);
		} else if (method.equals("POST")) {
			HttpPost post = new HttpPost(url);
			ArrayList<NameValuePair> data = toPostData(params);
			post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			request = post;
		}

		HttpResponse response = httpClient.execute(request);
		if (isHttpSuccessExecuted(response)) {
			result = EntityUtils.toString(response.getEntity());
		}
		return result;
	}

	public static DefaultHttpClient getHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
		HttpConnectionParams.setSoTimeout(httpParams, 20000);
		// HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		return httpClient;
	}

	public static boolean isHttpSuccessExecuted(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		return (statusCode > 199) && (statusCode < 400);
	}

}

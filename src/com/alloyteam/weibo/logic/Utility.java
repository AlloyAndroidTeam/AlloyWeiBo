/**
 * @author azraellong
 * @date 2012-11-16
 */
package com.alloyteam.weibo.logic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.alloyteam.weibo.ImageActivity;
 

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
	public static byte[] Bitmap2Bytes(Bitmap bm){  
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();    
	    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);    
	    return baos.toByteArray();  
	}  	
	/**
	  * 将网络图片，转换为 btye 类型
	  * @param is
	  * @return
	  * @throws IOException
	  */
	 public static byte[] getBytes(InputStream is) throws IOException {           
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	  int imgSize = 1024*4;
	  byte[] b = null;  
	  byte[] bytes = null;
	  try {
	   b= new byte[imgSize];
	   int len = 0;           
	   while ((len = is.read(b, 0, imgSize)) != -1)            
	   {            
	    baos.write(b, 0, len);            
	    baos.flush();           
	   }           
	   bytes = baos.toByteArray();           
	   baos.close();
	   is.close();
	  } catch (Exception e) {
	   //Log.i(TAG, "将网络图片，转换为 btye 类型 方法异常"+e.toString());
	  }
	  return bytes;
	 }
	 
    public static String getType(byte[] data) {  
        String type = "BMP";  
        // Png test:  
        if (data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {  
            type = "PNG";  
            return type;  
        }  
        // Gif test:  
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {  
            type = "GIF";  
            return type;  
        }  
        // JPG test:  
        if (data[6] == 'J' && data[7] == 'F' && data[8] == 'I'  
                && data[9] == 'F') {  
            type = "JPG";  
            return type;  
        }  
        return type;  
    }
    static Hashtable<String, String> html_specialchars_table = new Hashtable<String, String>();
    static {
            html_specialchars_table.put("&lt;","<");
            html_specialchars_table.put("&gt;",">");
            html_specialchars_table.put("&amp;","&");
    }
    public static String htmlspecialchars_decode_ENT_NOQUOTES(String s){
            Enumeration<String> en = html_specialchars_table.keys();
            while(en.hasMoreElements()){
                    String key = (String)en.nextElement();
                    String val = (String)html_specialchars_table.get(key);
                    s = s.replaceAll(key, val);
            }
            return s;
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
	
	@SuppressWarnings("deprecation")
	public static String formatDate(long timestamp) {
		Date curDate = new Date(System.currentTimeMillis());
		Date date = new Date(timestamp);
		if (curDate.getDate() == date.getDate()) {
			SimpleDateFormat format = new SimpleDateFormat("今天 HH:mm:ss");
			return format.format(date);
		} else if (curDate.getDate() == date.getDate() + 1) {
			SimpleDateFormat format = new SimpleDateFormat("昨天 HH:mm:ss");
			return format.format(date);
		} else if (curDate.getDate() == date.getDate() + 2) {
			SimpleDateFormat format = new SimpleDateFormat("前天 HH:mm:ss");
			return format.format(date);
		} else {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			return format.format(date);
		}
	}

	public static String request(String url, String method, Bundle params)
			throws IOException {
		String result = "";
		HttpClient httpClient = getHttpClient();
		HttpUriRequest request = null;

		if (method.equals("GET")) {
			String encodedParam = toQueryString(params);
			if(url.indexOf("?") == -1){
				url = url + "?";
			}else{
				url = url + "&";
			}
			url = url + encodedParam;
			Log.i("http request", "get: " + url);
			request = new HttpGet(url);		
			
		} else if (method.equals("POST")) {
			
			HttpPost post = new HttpPost(url);
			Log.i("http request", "post: " + url);
			ArrayList<NameValuePair> data = toPostData(params);
			Log.v("request", params.toString());
			post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			request = post;
			
		}
		HttpResponse response = httpClient.execute(request);
		if (isHttpSuccessExecuted(response)) {
			result = EntityUtils.toString(response.getEntity());
			Log.d("http request",result);
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
	
	public static void showImage(Context context, String url, Bitmap bm){
		Intent intent = new Intent(context, ImageActivity.class);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}
	
	public static boolean isHttpSuccessExecuted(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		Log.d("response",""+statusCode);
		return (statusCode > 199) && (statusCode <= 400);
	}
	
	 
	 /**
     * Post方法传送文件和消息
     * 使用：httpmime-4.1.3.jar库文件
     * @param url  连接的URL
     * @param queryString 请求参数串
     * @param files 上传的文件列表
     * @return 服务器返回的信息
     * @throws Exception
     */
    public static String postWithFile(String url, Bundle params, String filePath) throws Exception {    	
    	  Log.v("httpPostWithFile", "httpPostWithFile");
          HttpClient httpClient= new DefaultHttpClient();
          HttpPost httpPost= new HttpPost(url);          
          MultipartEntity mulentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
          
          ArrayList<NameValuePair> data = toPostData(params); 
          
          StringBody stringBody;
          FormBodyPart fbp;
          
          for(NameValuePair queryParam:data){
	        stringBody = new StringBody(queryParam.getValue(),Charset.forName("UTF-8"));
	        fbp= new FormBodyPart(queryParam.getName(), stringBody); 
	        mulentity.addPart(fbp); 
	      }
          
          FileBody fileBody;
          File targetFile; 
          
          //添加图片表单数据         
          if (filePath != null && filePath.length() > 0){
	          targetFile= new File(filePath);          
	          fileBody = new FileBody(targetFile,"application/octet-stream"); 
	          fbp= new FormBodyPart("pic", fileBody);
	          mulentity.addPart(fbp);  
          }
          httpPost.setEntity(mulentity); 
          String responseData = null;
          try {
             HttpResponse response= httpClient.execute(httpPost);
             //Log.i(tag, "httpPostWithFile [2] StatusLine = "+response.getStatusLine());
             if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){ 
            	 responseData = EntityUtils.toString(response.getEntity());
             }else{
            	 Log.i("HttpResponse", "error = "+ response.getStatusLine().getStatusCode());
             }
	             
	       } catch (Exception e) {
	             e.printStackTrace();
	       }finally{
	           httpPost.abort();
	        }
           //Log.v("responseData", responseData);
           return responseData;
    }
}

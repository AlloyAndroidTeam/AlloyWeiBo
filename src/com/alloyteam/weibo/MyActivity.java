package com.example.my;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class MyActivity extends Activity {
	ListView mylist;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my);
        mylist=(ListView) findViewById(R.id.listView1);
        
        final Activity context=this;
        new HttpThread(new HttpThread.MyHandler(
			new HttpThread.HttpCallback(){
				public void onResponse(JSONObject obj){
					try{
						List<Weibo> list=new ArrayList<Weibo>();
			        	JSONObject data =  obj.getJSONObject("data");
			        	JSONArray info = data.getJSONArray("info");
			        	Log.d("json","parse");
			        	for(int i=0;i<info.length();++i){
				        	JSONObject item = info.getJSONObject(i);
				        	String text = item.getString("text");
				        	String name = item.getString("name");
				        	String avatarUrl=item.getString("head")+"/50";
				        	int type=item.getInt("type");
				        	Weibo weibo=new Weibo(name,text,avatarUrl);
				        	long timestamp=item.getLong("timestamp");
				            weibo.type=type;
				            weibo.timestamp=timestamp;
				            if(type==2){
					            JSONObject source=item.getJSONObject("source");
					        	String text2 = source.getString("text");
					        	String name2 = source.getString("name");
					        	String avatarUrl2=source.getString("head")+"/50";
				        		weibo.mText2=text2;
				        		weibo.mAvatarUrl2=avatarUrl2;
				        		weibo.mName2=name2;
				            	if(source.get("image")!=JSONObject.NULL){
				            		Log.d("my","image");
				            		JSONArray images=source.getJSONArray("image");
				            		weibo.mImage=images.getString(0);
				            	}
				            	weibo.count = item.getInt("count");
				            }
				            else{
				            	if(item.get("image")!=JSONObject.NULL){
				            		Log.d("my","image");
				            		JSONArray images=item.getJSONArray("image");
				            		weibo.mImage=images.getString(0);
				            	}
				            }
				            list.add(weibo);
				        }
			            WeiboListAdapter ila=new WeiboListAdapter(context,list);
			            mylist.setAdapter(ila);
			            TextView text=(TextView) context.findViewById(R.id.textView1);
			            text.setVisibility(View.GONE);
			            //LayoutParams lp=(LayoutParams) mylist.getLayoutParams();
			            //lp.leftMargin=0;
			            //mylist.setLayoutParams(lp);
			        }
					catch (JSONException je)  
		            {  
		                Log.d("json","error");
		            }  
    			}
			}
		),"",this).start();

    }
}

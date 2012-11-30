package com.alloyteam.weibo;
 

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alloyteam.weibo.logic.AccountManager;
import com.alloyteam.weibo.logic.ApiManager;
import com.alloyteam.weibo.logic.Utility;
import com.alloyteam.weibo.logic.ApiManager.ApiResult;
import com.alloyteam.weibo.model.DataManager;
import com.alloyteam.weibo.model.Listeners;
import com.alloyteam.weibo.model.Weibo2;  

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;


public class PopFriend extends PopupWindow {
	private View popView;
	private ListView listView;
	private EditText etFilter;
	private PostActivity postActivity;
	private Button btnFinish;
	
	private String[] strs = new String[] {
		    "first", "second", "third", "fourth", "fifth","six", "seven", "eight"
		    };//定义一个String数组用来显示ListView的内容
	private ArrayAdapter adapter;
	private ArrayList<String> dataList=new ArrayList<String>();
	private ArrayList<String> dataSrc=new ArrayList<String>();
	
	private HashMap<String, JSONArray> hashList = new HashMap<String, JSONArray>(); //key为页码，做缓存
	private boolean lock = false;//拉取数据的时候锁住
	
	//private ArrayList<String,Object> data
	
	private int nextStartIndex = 0;
	private Handler handler;
	private Thread sThread;
	
	public PopFriend(Activity context) {  
        super(context);
        
        postActivity = (PostActivity)context;
         
        
        LayoutInflater inflater = (LayoutInflater) context  
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        popView = inflater.inflate(R.layout.pop_friend, null); 
         
          
        //设置PopupWindow的View  
        this.setContentView(popView);  
        //设置SelectPicPopupWindow弹出窗体的宽  
        this.setWidth(LayoutParams.FILL_PARENT);  
        //设置SelectPicPopupWindow弹出窗体的高  
       this.setHeight(LayoutParams.MATCH_PARENT);
        //this.setWidth(200);
        //this.setHeight(250); 
        
       
        
        //设置SelectPicPopupWindow弹出窗体可点击  
        this.setFocusable(true);  
        //实例化一个ColorDrawable颜色为半透明  
        ColorDrawable dw = new ColorDrawable(0xb0000000);  
        //设置SelectPicPopupWindow弹出窗体的背景  
        this.setBackgroundDrawable(dw);  
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框  
       
        /*
        popView.setOnTouchListener(new OnTouchListener() {  
              
            public boolean onTouch(View v, MotionEvent event) {  
                
                int height = popView.findViewById(R.id.listViewFriend).getTop();  
                int y=(int) event.getY();  
                if(event.getAction()==MotionEvent.ACTION_UP){  
                    if(y<height){  
                        dismiss();  
                    }  
                }   
                return true;  
            }  
        });      
        */
        
        
        etFilter = (EditText)popView.findViewById(R.id.etPopFriendFilter);        
        bindFilter();

        btnFinish = (Button)popView.findViewById(R.id.btnPopFriendFinish);
        btnFinish.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub 
				String txt = "";
				SparseBooleanArray a = listView.getCheckedItemPositions();  
				Log.v("btnFinish onClick", "dataSrc.size=" + dataSrc.size());
				int l = dataSrc.size();
				l = l > a.size() ? a.size() : l;
		        for(int i = 0; i < l ; i++)  
		        {  Log.v("onClick I:", i + "");
		            if (a.valueAt(i))  
		            {  
		                Long idx = listView.getAdapter().getItemId(a.keyAt(i));  
		                Log.v("btnFinish onClick", "index=" + idx);
		                String s = dataSrc.get(idx.intValue());
		                String []sArr = s.split("@");
		            	txt += "@" + sArr[1].replace(")", "");
		            }  
		        }  
		        postActivity.setExitText(txt);
		        dismiss();				
			}
        	
        });
        initListView(context);
    }  
	/*
	 * 初始化listview
	 */
	private void initListView(Context context){
		listView = (ListView)popView.findViewById(R.id.listViewFriend); 
		
		adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_multiple_choice, dataSrc);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
		
		listView.setOnScrollListener(onScroll);
		/*
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {		
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) { 
				Log.v("onItemLongClick", "arg2:" + arg2 + ", arg3:" + arg3);
				String txt = etFilter.getText().toString();
				txt = "@" + txt.replace("@", "");
				dismiss();
				postActivity.setExitText(txt);
			}
	    });
	    */
		//重置起至位置 
		nextStartIndex = 0;
		//handler = createHandler();
		getData();
	}

	/*
	 * 绑定数字提示
	 */
	private void bindFilter(){
		etFilter.addTextChangedListener(new TextWatcher()
        {                
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                        // TODO Auto-generated method stub 
                	//log("onTextChanged" + s + ",start:" + start + ",before:" +before+ ",length:"+s.length());                	      	 
                	CharSequence ss = s.subSequence(start, s.length()); 
                }
                
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                        // TODO Auto-generated method stub
                }
                
                public void afterTextChanged(Editable s)
                {
                	
                	 adapter.clear();                	  
                	 filterSrcList();                 	 
                	 adapter.notifyDataSetChanged();
                	
                }
        });
	}

	/**
	 * 返回筛选后的数据
	 * @param filter
	 * @return
	 */
	public void filterSrcList(){
		String filter = etFilter.getText().toString().toLowerCase(); 
		boolean isFilter = false;
		
		if (filter != null && !filter.equals("") && !filter.equals("@")){
			isFilter = true;
		} 
		filter = filter.replace("@", "");
		Log.v("filter", filter); 	
		
		
		int l = hashList.size(); 
		dataSrc.clear();
		
		Iterator iter = hashList.entrySet().iterator();  
		while (iter.hasNext()) {  
			Map.Entry entry = (Map.Entry) iter.next();  
		    Object key = entry.getKey();  
		    JSONArray jArr = (JSONArray) entry.getValue();
		    int l2 = jArr.length();
		    for (int i = 0; i < l2; i++){				
				try {
					JSONObject o = (JSONObject) jArr.get(i);
					String name = o.getString("name");
					String nick = o.getString("nick");
					if (isFilter){
						if (name.toLowerCase().startsWith(filter) || nick.toLowerCase().startsWith(filter)){
							String str = nick + "(@" + name + ")";
							dataSrc.add(str); 
						}
					}else{
						String str = nick + "(@" + name + ")";
						dataSrc.add(str); 
					}					
				} catch (JSONException e) { 
					e.printStackTrace();
				}   				
			} 
		} 	 
	}
	 
	
	/**
	 * 获取数据
	 */
	public void getData(){
		
		if (nextStartIndex == -1){
			return ;//数据已经拉完
		}
		
		lock = true;
		final com.alloyteam.weibo.model.Account account = AccountManager.getDefaultAccount();
		JSONObject json = null;
		//尝试从缓存读取
		try {
			json = Listeners.get(account.type, nextStartIndex);
			if (json != null){ 
				parseData(json);
				Log.v("getData", " from Listeners " + nextStartIndex);
				lock = false;
				return ;
			}			
		} catch (Exception e) { 
			e.printStackTrace();
		}	
		
		ApiManager.IApiResultListener listener = new ApiManager.IApiResultListener() {
			@Override
			public void onSuccess(ApiResult result) {
				if(result != null && result.listeners != null){
					 
					int lastStartIndex = nextStartIndex;
					Listeners.add(account.type, lastStartIndex, result.listeners);//保存到model						
					parseData(result.listeners);					
					lock = false;
				}else{
					Log.v("getData  error:", " ApiResult null");
				}
			}
			@Override
			public void onError(int errorCode) {
				 Log.v("getData  error:", "" + errorCode);
			}
		};
		 
		     
		ApiManager.getListeners(account, nextStartIndex, 10, listener);
	}
	/**
	 * 处理标准数据
	 * @param json
	 */
	private void parseData(JSONObject json){		
		try {
			nextStartIndex = json.getInt("nextStartIndex");		
			int lastStartIndex = json.getInt("startIndex");
			JSONArray list = json.getJSONArray("list"); 			
			hashList.put(getPageKey(lastStartIndex), list);//保存为上一页的页码					
			filterSrcList();		
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 Log.v("parseData  error:", json.toString());
		}
	}
	
	
	/**
	 * hash key：把数字专为字符串
	 * @param startIndex
	 * @return
	 */
	private String getPageKey(int startIndex){
		return "page_" + startIndex;
	}
	
	
	private OnScrollListener onScroll = new OnScrollListener(){

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,  
				int visibleItemCount, int totalItemCount) { 
	        if ((firstVisibleItem + visibleItemCount == totalItemCount)  
	                && (totalItemCount != 0)) {
	        	Log.v("onScroll", "onScroll");
	        	if (!lock){
	        		getData();
	        	} 
	        }  
	    }  

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
		}
		
	};
		
	 
}

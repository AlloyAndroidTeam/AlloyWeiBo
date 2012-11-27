package com.alloyteam.weibo;
 

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

public class PopFriend extends PopupWindow {
	private View popView;
	private ListView listView;
	private EditText etFilter;
	
	private String[] strs = new String[] {
		    "first", "second", "third", "fourth", "fifth","six", "seven", "eight"
		    };//定义一个String数组用来显示ListView的内容
	private ArrayAdapter adapter;
	private ArrayList<String> dataList=new ArrayList<String>();
	
	
	public PopFriend(Activity context) {  
        super(context);  
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
       
        popView.setOnTouchListener(new OnTouchListener() {  
              
            public boolean onTouch(View v, MotionEvent event) {  
                /**/ 
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
        
        initListView(context);
        
        etFilter = (EditText)popView.findViewById(R.id.etPopFriendFilter);
        
        bindFilter();
        //etFilter.seton
    }  
	/*
	 * 初始化listview
	 */
	private void initListView(Context context){
		listView = (ListView)popView.findViewById(R.id.listViewFriend);
		/*为ListView设置Adapter来绑定数据*/
		dataList.add("111");
		dataList.add("22");
		dataList.add("33");
		dataList.add("33");
		
		adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {		
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Log.v("onItemLongClick", "arg2:" + arg2 + ", arg3:" + arg3);
				dataList.add("after:"+ arg2 );
				adapter.notifyDataSetChanged();
			}
	    });
		   
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
                        // TODO Auto-generated method stub
                }
        });
	}


}

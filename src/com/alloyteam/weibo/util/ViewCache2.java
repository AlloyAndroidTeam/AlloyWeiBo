package com.alloyteam.weibo.util;

import com.alloyteam.weibo.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewCache2 { 

    private View baseView;

    private TextView text;
    
    private TextView name;

    private ImageView avatar;

    private TextView text2;
    
    private TextView name2;
    
    private TextView count;

    private ImageView avatar2;

    private TextView time;
    
    private ImageView image;
 

    public ViewCache2(View baseView) {

        this.baseView = baseView;

    }

 
    public TextView getText2() {

        if (text2 == null) {

        	text2 = (TextView) baseView.findViewById(R.id.textView5);

        }

        return text2;

    }

 
    public TextView getName2() {

        if (name2 == null) {

        	name2 = (TextView) baseView.findViewById(R.id.textView4);

        }

        return name2;

    }
    
    public ImageView getAvatar2() {

        if (avatar2 == null) {

            avatar2 = (ImageView) baseView.findViewById(R.id.imageView2);

        }

        return avatar2;

    }
    
    public TextView getText() {

        if (text == null) {

        	text = (TextView) baseView.findViewById(R.id.textView1);

        }

        return text;

    }

    public TextView getCount() {

        if (count == null) {

        	count = (TextView) baseView.findViewById(R.id.textView6);

        }

        return count;

    }
 
    public TextView getName() {

        if (name == null) {

        	name = (TextView) baseView.findViewById(R.id.textView2);

        }

        return name;

    }
    
    public ImageView getAvatar() {

        if (avatar == null) {

            avatar = (ImageView) baseView.findViewById(R.id.imageView1);

        }

        return avatar;

    }
    
    public ImageView getImage() {

        if (image == null) {

            image = (ImageView) baseView.findViewById(R.id.thumbImage);

        }

        return image;

    }
    
    public TextView getTime() {

        if (time == null) {

        	time = (TextView) baseView.findViewById(R.id.textView3);

        }

        return time;

    }
 
}


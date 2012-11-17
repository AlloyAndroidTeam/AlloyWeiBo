package com.alloyteam.weibo.util;

import com.alloyteam.weibo.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewCache { 

    private View baseView;

    private TextView text;
    
    private TextView name;

    private ImageView avatar;

    private TextView time;
 
    private ImageView image;

    public ViewCache(View baseView) {

        this.baseView = baseView;

    }

 

    public TextView getText() {

        if (text == null) {

        	text = (TextView) baseView.findViewById(R.id.textView1);

        }

        return text;

    }

 
    public TextView getName() {

        if (name == null) {

        	name = (TextView) baseView.findViewById(R.id.textView2);

        }

        return name;

    }
    
    public TextView getTime() {

        if (time == null) {

        	time = (TextView) baseView.findViewById(R.id.textView3);

        }

        return time;

    }
 
    public ImageView getAvatar() {

        if (avatar == null) {

        	avatar = (ImageView) baseView.findViewById(R.id.imageView1);

        }

        return avatar;

    }

    public ImageView getImage() {

        if (image == null) {

        	image = (ImageView) baseView.findViewById(R.id.imageView2);

        }

        return image;

    }

}


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

        	text = (TextView) baseView.findViewById(R.id.text);

        }

        return text;

    }

 
    public TextView getName() {

        if (name == null) {

        	name = (TextView) baseView.findViewById(R.id.name);

        }

        return name;

    }
    
    public TextView getTime() {

        if (time == null) {

        	time = (TextView) baseView.findViewById(R.id.date);

        }

        return time;

    }
 
    public ImageView getAvatar() {

        if (avatar == null) {

        	avatar = (ImageView) baseView.findViewById(R.id.avatar);

        }

        return avatar;

    }

    public ImageView getImage() {

        if (image == null) {

        	image = (ImageView) baseView.findViewById(R.id.image);

        }

        return image;

    }

}


package com.alloyteam.weibo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * @package：
 * @data：2012-9-27 上午8:59:40
 * @description：The class is for...
 */
public class ImageLoadingDialog extends Dialog {

    public ImageLoadingDialog(Context context) {
        super(context, R.style.ImageloadingDialogStyle);
        //setOwnerActivity((Activity) context);// 设置dialog全屏显示
    }

    private ImageLoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_imageloading);
    }

}
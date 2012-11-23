/**
 * @author azraellong
 * @date 2012-11-23
 */
package com.alloyteam.weibo.model;

import android.R.integer;

/**
 * @author azraellong
 * 
 */
public class Weibo2 {

	
	/**
	 * 微博类型 1-原创发表，2-转载，3-私信，4-回复，5-空回，6-提及，7-评论
	 */
	public static final int WEIBO_TYPE_ORIGINAL = 1;
	public static final int WEIBO_TYPE_REBROADCAST = 2;
	public static final int WEIBO_TYPE_PRIVATE = 3;
	public static final int WEIBO_TYPE_REPLY = 4;
	public static final int WEIBO_TYPE_EMPTY_REPLY = 5;
	public static final int WEIBO_TYPE_MENTION = 6;
	public static final int WEIBO_TYPE_COMMENT = 7;
	
	 /**
		 * 微博状态 0-正常，1-删除，2-审核中
		 */
	public static final int WEIBO_STATUS_NORMAL = 0;
	public static final int WEIBO_STATUS_DELETE = 1;
	public static final int WEIBO_STATUS_VERIFY = 2;
	
	/**
	 * 发表者的头像url
	 */
	public String avatarUrl = "";

	/**
	 * 发表者的id
	 */
	public String uid = "";

	/**
	 * 是否是自己发表的
	 */
	public boolean isSelf = false;

	/**
	 * 发表者的昵称
	 */
	public String nick = "";

	/**
	 * 是否是认证用户
	 */
	public boolean isVip = false;

	/**
	 * 微博id
	 */
	public String id = "";
	/**
	 * 微博内容
	 */
	public String text = "";
//	/**
//	 * 微博原始内容, 适用于转发和评论的情况
//	 */
//	public String originText = "";
	/**
	 * 微博附带的图片url(大图)
	 */
	public String imageUrl = "";

	/**
	 * 微博附带的图片 url(缩略图)
	 */
	public String imageThumbUrl = "";

	/**
	 * 微博附带的图片 url(中图)
	 */
	public String imageMiddleUrl = "";

	/**
	 * 微博发表的时间戳
	 */
	public long timestamp = 0;

	/**
	 * 微博被转播的次数
	 */
	public int rebroadcastCount = 0;

	/**
	 * 微博被评论的次数
	 */
	public int commentCount = 0;

	/**
	 * 微博类型 1-原创发表，2-转载，3-私信，4-回复，5-空回，6-提及，7-评论
	 */
	public int type = 1;

	/**
	 * 微博状态 0-正常，1-系统删除，2-审核中，3-用户删除，4-根删除（根节点被系统审核删除）
	 */
	public int status = 0;

	/**
	 * 当微博类型 type = 2时 ,source就是被转播的weibo
	 */
	public Weibo2 source = null;
	
	

}

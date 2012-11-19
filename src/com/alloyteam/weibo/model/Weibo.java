package com.alloyteam.weibo.model;

public class Weibo {
	
	private String mName="";
	private String mText="";
	private String mImageUrl="";
	private String mAvatarUrl="";
	public String mImage=null;
	public long timestamp;
	public String mName2="";
	public String mText2="";
	public String mImageUrl2="";
	public String mAvatarUrl2="";
	public int type=1;
	public int count=0;
	
	public Weibo(String name,String text,String avatarUrl){
		mName=name;
		mText=text;
		mAvatarUrl=avatarUrl;
	}
	public Weibo(){
		
	}
	public String getName(){
		return mName;
	}
	
	public String getText(){
		return mText;
	}
	
	public String getImageUrl(){
		return mImageUrl;
	}
	public String getAvatarUrl(){
		return mAvatarUrl;
	}
	public void setName(String name){
		mName=name;
	}
	
	public void setText(String text){
		mText=text;
	}
	
	public void setImageUrl(String imageUrl){
		mImageUrl=imageUrl;
	}
}

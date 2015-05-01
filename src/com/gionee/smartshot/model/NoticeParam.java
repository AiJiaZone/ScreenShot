package com.gionee.smartshot.model;

import android.content.Intent;

public class NoticeParam {
    private String mClassName;
    private String mContent;
    private int mIcon;
    private int mId;
    private Intent mIntent;
    private String mParam;
    private String mPkgName;
    private String mTicker;
    private String mTitle;
    
    public NoticeParam(int id, String title, String content) {
        mId = id;
        mTitle = title;
        mContent = content;
    }
    
    public NoticeParam(int id, String title, String content, Intent intent, int icon, String ticker) {
        mId = id;
        mTitle = title;
        mContent = content;
        mIntent = intent;
        mIcon = icon;
        mTicker = ticker;
    }
    
    public String getClassName() {
        return mClassName;
    }
    
    public String getContent() {
        return mContent;
    }
    
    public int getIcon() {
        return mIcon;
    }
    
    public int getId() {
        return mId;
    }
    
    public Intent getIntent() {
        return mIntent;
    }
    
    public String getParam() {
        return mParam;
    }
    
    public String getPkgName() {
        return mPkgName;
    }
    
    public String getTicker() {
        return mTicker;
    }
    
    public String getTitle() {
        return mTitle;
    }
    
    public void setClassName(String className){
        mClassName = className;
    }
    
    public void setContent(String content) {
        mContent = content;
    }
    
    public void setIcon(int icon) {
        mIcon = icon;
    }
    
    public void setId(int id) {
        mId = id;
    }
    
    public void setIntent(Intent intent) {
        mIntent = intent;
    }
    
    public void setParam(String param) {
        mParam = param;
    }
    
    public void setPkgName(String pkgName) {
        mPkgName = pkgName;
    }
    
    public void setTicker(String ticker) {
        mTicker = ticker;
    }
    
    public void setTitle(String title) {
        mTitle = title;
    }
    
    public String toString() {
        return "NoticeParam [mId=" + mId + ", mTitle=" + mTitle + ", mContent=" + mContent + ", mPkgName=" + mPkgName + ", mClassName=" + mClassName + ", mParam=" + mParam + ", mTicker=" + mTicker + ", mIntent=" + mIntent + ", mIcon=" + mIcon + "]";
    }
    
}

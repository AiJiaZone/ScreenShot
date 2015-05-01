package com.smartshot.ui;

import android.app.Application;
import android.content.Context;

public class SmartShotApp extends Application {
    private static final String TAG = "SmartShotApp";
    private static SmartShotApp mSmartShotApp;
    private Context mContext;
    private boolean mMainViewExist;
    
    public static SmartShotApp getAppInstance() {
        return mSmartShotApp;
    }
    
    public boolean isMainViewExist() {
        return mMainViewExist;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mSmartShotApp = this;
        mContext = getApplicationContext();
    }
    
    public void setMainViewExist(boolean isExist) {
        mMainViewExist = isExist;
    }
    
}

package com.gionee.smartshot.ui.widget;

import com.gionee.smartshot.R;
import com.gionee.smartshot.preference.SharedPreferenceUtils;
import com.gionee.smartshot.ui.SmartShotApp;
import com.gionee.smartshot.utils.StringUtil;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class SuperShotMenuManager {
    
    private static SuperShotMenuManager mShotMenuManager;
    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private FloatShotMenuWindow mMenuWindow;
    private WindowManager mWindowManager;
    
    private SuperShotMenuManager(Context context) {
        mContext = context;
        mWindowManager = ((WindowManager)context.getSystemService("window"));
    }
    
    public static SuperShotMenuManager getInstance(Context context) {
        if (mShotMenuManager == null) {
            mShotMenuManager = new SuperShotMenuManager(context);
        }
        return mShotMenuManager;
    }
    
    public void addView() {
        if (mMenuWindow == null) {
            mMenuWindow = new FloatShotMenuWindow(mContext);
        }
        
        if (mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLayoutParams.gravity = 51;
            mLayoutParams.format = 1;
            mLayoutParams.windowAnimations = R.style.anim_view;
        }
        
        DisplayMetrics displayMetrics = new DisplayMetrics();;
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int orientation = mContext.getResources().getConfiguration().orientation;
        String str = SharedPreferenceUtils.getString(mContext, SharedPreferenceUtils.KEY_MAINVIEW_COORD, "");
        
        if (!StringUtil.isEmpty(str)) {
            String[] arrayOfString = str.split("/");
            mLayoutParams.x = Integer.valueOf(arrayOfString[0]).intValue();
            mLayoutParams.y = Integer.valueOf(arrayOfString[1]).intValue();
        } else if (orientation == 1) {
            mLayoutParams.x = (displayMetrics.widthPixels  / 4);
            mLayoutParams.y = (displayMetrics.heightPixels / 4);
        } else {
            mLayoutParams.x = (displayMetrics.widthPixels  / 3);
            mLayoutParams.y = (displayMetrics.heightPixels / 4);
        }
        
        
        SmartShotApp.getAppInstance().setMainViewExist(true);
        
        mWindowManager.addView(mMenuWindow, mLayoutParams);
        mMenuWindow.setLayouParam(mLayoutParams);
        
        
    }
    
    public void removeView()
    {
        if (mMenuWindow != null) {
            SmartShotApp.getAppInstance().setMainViewExist(false);
            mWindowManager.removeView(mMenuWindow);
            mMenuWindow = null;
        }
    }
    
}

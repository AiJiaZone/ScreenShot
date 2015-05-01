package com.gionee.smartshot.utils;

import android.util.Log;

public class LogUtil {
    protected static final String TAG_PREFIX = "SmartShot";
    
    public static void d(String str) {
        if (StringUtil.isEmpty(str))
            return;
        Log.d("SmartShot", str);
    }
    
    public static void d(String str1, String str2) {
        if (StringUtil.isEmpty(str2))
            return;
        Log.d("SmartShot/" + str1, str2);
    }
    
}

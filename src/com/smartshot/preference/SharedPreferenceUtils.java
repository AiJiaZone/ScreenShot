package com.smartshot.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils {
    public static final String KEY_MAINVIEW_COORD = "Key_Mainview_Coord";
    public static final String SHARE_PRE_TAG = "SmartShot";
    private static SharedPreferences mPrefs = null;
    
    public static boolean contains(Context context, String str) {
        return getPrefs(context).contains(str);
    }
    
    public static boolean getBoolean(Context context, String str, boolean booleanValue) {
        return getPrefs(context).getBoolean(str, booleanValue);
    }
    
    public static int getInt(Context context, String str, int paramInt) {
        return getPrefs(context).getInt(str, paramInt);
    }
    
    public static long getLong(Context context, String str, long paramLong) {
        return getPrefs(context).getLong(str, paramLong);
    }
    
    public static SharedPreferences getPrefs(Context context) {
        if (mPrefs == null) {
            synchronized (SharedPreferences.class) {
                if (mPrefs == null) {
                    try {
                        mPrefs = context.getSharedPreferences(SHARE_PRE_TAG, Context.MODE_PRIVATE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                }
            }
        }
        return mPrefs;
    }
    
    public static String getString(Context context, String str1, String str2) {
        return getPrefs(context).getString(str1, str2);
    }
    
    public static boolean putBoolean(Context context, String str, boolean booleanValue) {
        return getPrefs(context).edit().putBoolean(str, booleanValue).commit();
    }
    
    public static boolean putInt(Context context, String str, int paramInt) {
        return getPrefs(context).edit().putInt(str, paramInt).commit();
    }
    
    public static boolean putLong(Context context, String str, long paramLong) {
        return getPrefs(context).edit().putLong(str, paramLong).commit();
    }
    
    public static boolean putString(Context context, String str1, String str2) {
        return getPrefs(context).edit().putString(str1, str2).commit();
    }
    
    public static void remove(Context context, String str) {
        getPrefs(context).edit().remove(str).commit();
    }
    
}

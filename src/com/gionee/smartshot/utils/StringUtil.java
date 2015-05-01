package com.gionee.smartshot.utils;

public class StringUtil {
    
    public static boolean isEmpty(String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static boolean isEquals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if(str1 != null && str2 != null) {
            return str1.equals(str1);
        } else {
            return false;
        }
        
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}

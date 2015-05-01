package com.smartshot.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import com.gionee.smartshot.R;
import com.smartshot.model.NoticeParam;
import com.smartshot.ui.widget.SmartShotNoticeficationManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

public class SmartShotUtil {
    
    public static final String INTERNAL_CARD = "/storage/sdcard0";
    private static final String TAG = "SmartShotUtil";
    public static boolean isSaveComplete = false;
    
    
    public static boolean deleteFile(String str) {
        if (StringUtil.isNotEmpty(str)) {
            File file = new File(str);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        }
        return false;
    }

    public static int dip2px(float dpValue, float scale) {
        return (int)(0.5F + dpValue * scale);
    }

    public static int dip2px(Context context, float dpValue) {
        return (int)(0.5F + dpValue * context.getResources().getDisplayMetrics().density);
    }

    public static String getAvaibleSDCardPath() {
        String str = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!Environment.getExternalStorageState().equals("mounted")) {
            str = INTERNAL_CARD;
        }
        return str;
    }
    
    public static String getFilePath() {
        String str1 = getAvaibleSDCardPath();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str1 + "/截屏");
        File file = new File(stringBuffer.toString());
        if (!file.exists()) {
            Log.i("SmartShotUtil", "file is not exist");
            file.mkdir();
        }
        String str2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        stringBuffer.append("/录屏_").append(str2).append(".mp4");
        return stringBuffer.toString();
    }

    public static Intent getImageFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        return intent;
    }
    
    public static String getResourceString(Context context, int id) {
        return context.getResources().getString(id);
    }
    
    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> cls = Class.forName("com.android.internal.R$dimen");
            Object obj = cls.newInstance();
            int x = Integer.parseInt(cls.getField("status_bar_height").get(obj).toString());
            int y = context.getResources().getDimensionPixelSize(x);
            return y;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public static boolean isAvailableExternalMemory(File paramFile) {
        StatFs localStatFs = new StatFs(paramFile.getPath());
        return (int)(localStatFs.getBlockSize() * localStatFs.getAvailableBlocks() / 1048576L) > 15;
    }
    
    public static boolean isHome(Context context) {
        ArrayList<String> arrayList = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        Iterator<ResolveInfo> iterator = packageManager.queryIntentActivities(intent, Intent.FLAG_ACTIVITY_NO_ANIMATION).iterator();
        while (iterator.hasNext()) {
            arrayList.add(((ResolveInfo)iterator.next()).activityInfo.packageName);
        }
        LogUtil.d(TAG, "names =  " + arrayList);
        return arrayList.contains(((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).
                getRunningTasks(1).get(0)).topActivity.getPackageName());
    }
    
    public static boolean isUCBrowser(Context context) {
        return "com.UCMobile".equals(((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).
                getRunningTasks(1).get(0)).topActivity.getPackageName());
    }
    
    public static boolean isBrowser(Context context) {
        String packageName = ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).
                getRunningTasks(1).get(0)).topActivity.getPackageName();
        return "com.UCMobile".equals(packageName) || "com.android.browser".equals(packageName);
    }
    
    public static boolean isInDirectlyHintToBottom(Context context) {
        return ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).
                get(0)).topActivity.getClassName().contains("com.android.soundrecorder.SoundRecorder");
    }
    
    public static boolean isInFullWidthCompareApp(Context context) {
        String str = ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName();
        return (str.contains("com.android.mms.ui.ComposeMessageActivity") || str.contains("com.android.camera.CameraLauncher"));
    }
    
    public static boolean isInGallery3D(Context paramContext) {
        return ((ActivityManager.RunningTaskInfo)((ActivityManager)paramContext.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName().contains("com.android.gallery3d.GalleryTabActivity");
    }
    
    public static boolean isInIManagerPhoneClean(Context context) {
        return ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName().contains("com.iqoo.secure.ui.phoneoptimize.PhoneCleanActivity");
    }
    
    public static boolean isInQQ(Context context) {
        return ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName().contains("com.tencent.mobileqq");
    }
    
    public static boolean isInUnmoveableApp(Context context) {
        String str = ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName();
        LogUtil.d("SmartShotUtil", "names =  " + str);
        return (str.contains("VoiceAssistant") ||
                str.contains("MediaPlaybackActivity") || str.contains("PlayOnlineActivity") ||
                str.contains("com.android.VideoPlayer.MovieViewActivity") || str.contains("CameraActivity") ||
                str.contains("CameraLauncher") || str.contains("com.gionee.video.player.MovieActivity"));
    }
    
    public static boolean isInternalSDCard() {
        String str = Environment.getExternalStorageDirectory().getAbsolutePath();
        return (StringUtil.isNotEmpty(str)) && (INTERNAL_CARD.equals(str));
    }
    
    private static void saveImage(Bitmap bitmap, String str, Context context) {
        isSaveComplete = false;
        String str1 = getAvaibleSDCardPath();
        String str2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        File file1 = new File(str1 + "/截屏/");
        String str3 = ".png";
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
        if (str.equals("JPG")) {
            str3 = ".jpg";
            compressFormat = Bitmap.CompressFormat.JPEG;
        }
        
        File file2 = new File(str1 + "/截屏/" + "超级截屏_" + str2 + str3);
        
        try {
            if (!file1.exists()) {
                file1.mkdirs();
            }
            
            if (!file2.exists()) {
                file2.createNewFile();
            }
            
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            if (bitmap.compress(compressFormat, 80, fileOutputStream)) {
                Looper.prepare();
                
                NoticeParam noticeParam = null;
                String content = null;
                if (isInternalSDCard()) {
                    content = getResourceString(context, R.string.screenshot_saved_to_tf_card);
                } else {
                    content = getResourceString(context, R.string.screenshot_saved_to_sd_card);
                }
                
                noticeParam = new NoticeParam(SmartShotConstant.NOTICEFICATION_ID_OF_SCROLL_SHOT, 
                        getResourceString(context, R.string.screenshot_saved), content, 
                        getImageFileIntent(file2), R.drawable.image_save_noti, getResourceString(context, R.string.screenshot_saved));
                
                SmartShotNoticeficationManager smartShotNoticeficationManager = new SmartShotNoticeficationManager(context);
                smartShotNoticeficationManager.sendImageSaveNoticefication(noticeParam);
                //add by hbx
                smartShotNoticeficationManager.reomveNoticeficationByID(SmartShotConstant.NOTICEFICATION_ID_OF_SCROLL_SHOT_SAVING);
                context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file2)));
                fileOutputStream.flush();
                fileOutputStream.close();
                isSaveComplete = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveScrollShotImageToTFCard(Bitmap bitmap, String str, Context context) {
        saveImage(bitmap, str, context);
    }
    
}

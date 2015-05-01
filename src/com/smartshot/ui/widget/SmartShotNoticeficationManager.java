package com.smartshot.ui.widget;

import com.smartshot.model.NoticeParam;
import com.smartshot.utils.StringUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class SmartShotNoticeficationManager {

    private static final String TAG = "ShotNoticeficationManager";
    private static SmartShotNoticeficationManager mNoticeficationManager;
    private Context mContext;
    private String mFilePath;
    private NotificationManager mManager;

    public SmartShotNoticeficationManager(Context context) {
        mManager = (NotificationManager)context.getSystemService("notification");
        mContext = context;
    }

    public static SmartShotNoticeficationManager getInstance(Context context) {
        Log.d(TAG," SmartShotNoticeficationManager " + mNoticeficationManager);
        if (mNoticeficationManager == null) {
            mNoticeficationManager = new SmartShotNoticeficationManager(context);
        }
        return mNoticeficationManager;
    }
    
    public String getFilePath() {
        return mFilePath;
    }
    
    public void reomveNoticeficationByID(int id) {
        Log.d(TAG, " reomveNoticeficationByID " + id);
        if (mManager != null) {
            mManager.cancel(id);
        }
    }
    
    public void sendImageSaveNoticefication(NoticeParam noticeParam) {
        if (noticeParam == null || mManager == null) {
            return;
        }
        
        Intent intent = noticeParam.getIntent();
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        }
        Notification notification = new Notification.Builder(mContext).setAutoCancel(true).
                setTicker(noticeParam.getTicker()).setSmallIcon(noticeParam.getIcon()).
                setContentTitle(noticeParam.getTitle()).setContentText(noticeParam.getContent()).
                setWhen(System.currentTimeMillis()).setContentIntent(pendingIntent).build();
        
        reomveNoticeficationByID(noticeParam.getId());
        mManager.notify(noticeParam.getId(), notification);
        
    }
    
    public void sendNormalNoticefication(NoticeParam noticeParam) {
        if (noticeParam == null) {
            return;
        }
        
        Notification notification = new Notification();
        notification.icon  = noticeParam.getIcon();
        notification.flags = Notification.FLAG_AUTO_CANCEL | notification.flags;
        
        Intent intent = null;
        if (StringUtil.isNotEmpty(noticeParam.getPkgName()) && StringUtil.isNotEmpty(noticeParam.getClassName())) {
            intent = new Intent();
            intent.setClassName(noticeParam.getPkgName(), noticeParam.getClassName());
        } else {
            intent = noticeParam.getIntent();
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0); 
        String str1 = noticeParam.getTitle();
        String str2 = noticeParam.getContent();
        notification.setLatestEventInfo(mContext, str1, str2, pendingIntent);
        
        if (mManager != null) {
            mManager.notify(noticeParam.getId(), notification);
        }
    }
    
    public void setFilePath(String str) {
        mFilePath = str;
    }
}

package com.smartshot.ui;

import com.gionee.smartshot.R;
import com.smartshot.model.NoticeParam;
import com.smartshot.utils.SmartShotConstant;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class SuperShotFloatViewService extends Service {
    
    private static final String TAG = "SuperShotFloatViewService";
    private Context mContext;
    private SuperShotFloatView mFloatView;
    private Notification mNotification;
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        if (SuperShotFloatView.instance != null) {
            mFloatView = SuperShotFloatView.instance;
        }
        
        mContext = getApplicationContext();
        
        mNotification = new Notification();
        NoticeParam noticeParam = new NoticeParam(SmartShotConstant.NOTICEFICATION_ID_OF_LONGSCREEN_SHOT, 
                mContext.getString(R.string.scrollcapture_is_running) + "...", " ", null,
                R.drawable.image_save_noti, "ticker");
        
        mNotification = new Notification.Builder(mContext).setSmallIcon(noticeParam.getIcon())
                .setContentTitle(noticeParam.getTitle()).setWhen(System.currentTimeMillis())
                .setContentIntent(null).build();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        if (intent != null) {
            startForeground(SmartShotConstant.NOTICEFICATION_ID_OF_LONGSCREEN_SHOT, mNotification);
            if (mFloatView == null) {
                mFloatView = SuperShotFloatView.instance;
            }
            mFloatView.showFloatViewToDoLongShot();
        } else {
            Log.d(TAG, "intent = null");
        }
        
        return super.onStartCommand(intent, flags, startId);
    }
    
}

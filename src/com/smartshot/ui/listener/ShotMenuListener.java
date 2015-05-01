package com.smartshot.ui.listener;


import java.io.File;

import com.gionee.smartshot.R;
import com.smartshot.ui.NoSpaceActivity;
import com.smartshot.ui.widget.SuperShotFloatViewManager;
import com.smartshot.ui.widget.SuperShotMenuManager;
import com.smartshot.utils.SmartShotConstant;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

public class ShotMenuListener implements View.OnClickListener{
    
    private static final String TAG = "ShotMenuListener";
    private Context mContext;
    private File mCurrentFileDir;
    private SuperShotFloatViewManager mFloatViewManager;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SmartShotConstant.MSG_STOP_INSUFFIICIENT_SPACE:
                Toast.makeText(mContext, mContext.getString(R.string.insufficient_space), 1).show();
                break;
            default:
            }
            
        }
    };
    
    private SuperShotMenuManager mShotMenuManager;
    
    public ShotMenuListener(Context context) {
        mContext = context;
        mShotMenuManager = SuperShotMenuManager.getInstance(mContext);
    }
    
    @Override
    public void onClick(View view) {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            Toast.makeText(mContext, mContext.getString(R.string.no_storage_message), 0).show();
        } else {
            mCurrentFileDir = Environment.getExternalStorageDirectory();
            //stop check the external memory, because Gionee is not the same...
            //!SmartShotUtil.isAvailableExternalMemory(mCurrentFileDir)
            if (false) {
                if (view.getId() != R.id.btn_delete)
                {
                    Intent intent = new Intent();
                    intent.setClass(this.mContext, NoSpaceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            } else {
                if (mShotMenuManager != null) {
                    mShotMenuManager.removeView();
                    
                    switch (view.getId()) {
                    case R.id.scroll_shot_text:
                        if (mFloatViewManager == null) {
                            mFloatViewManager = SuperShotFloatViewManager.getInstance(mContext);
                        }
                        mFloatViewManager.startLongScreenShot();
                        break;
                    case R.id.window_shot_text:
                        break;
                    case R.id.screen_record_text:
                        break;
                    default:
                        mShotMenuManager.removeView();
                        
                    }
                    
                }
            }
        }
    }
    
}

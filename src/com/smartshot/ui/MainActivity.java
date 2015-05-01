package com.smartshot.ui;

import com.gionee.smartshot.R;
import com.smartshot.ui.widget.SuperShotMenuManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends Activity {
    
    private static String TAG = "MainActivity";
    private Context mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        
        SuperShotMenuManager superShotMenuManager = SuperShotMenuManager.getInstance(getApplicationContext());
        boolean bool = SmartShotApp.getAppInstance().isMainViewExist();
        
        if (!Environment.getExternalStorageState().equals("mounted") && !bool) {
            Toast.makeText(mContext, getString(R.string.no_storage_message), 0).show();
            finish();
            superShotMenuManager.removeView();
            return;
        }
        
        if (!bool) {
            superShotMenuManager.addView();
        }
        
        finish();
    }
    
}

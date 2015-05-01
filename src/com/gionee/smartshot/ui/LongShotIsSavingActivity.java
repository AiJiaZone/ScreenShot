package com.gionee.smartshot.ui;

import com.gionee.smartshot.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LongShotIsSavingActivity extends Activity implements View.OnClickListener {
    
    private static final String TAG = "LongShotIsSavingActivity";
    public static LongShotIsSavingActivity instance;
    private Button mBtnOK;
    private Context mContext;
    private boolean mIsChecked;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.long_shot_saving_dialog_activity);
        
        instance = this;
        mContext = this;
        mBtnOK = ((Button)findViewById(R.id.longshot_saving_ok));
        mBtnOK.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.longshot_saving_ok:
            finish();
            break;
        default:
        }
    }
    
}

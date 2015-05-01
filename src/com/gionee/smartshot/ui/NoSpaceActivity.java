package com.gionee.smartshot.ui;

import com.gionee.smartshot.R;
import com.gionee.smartshot.utils.SmartShotUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NoSpaceActivity extends Activity implements View.OnClickListener {
    
    
    private static final String TAG = "NoSpaceActivity";
    private Button mBtnCancel;
    private Button mBtnClear;
    private Context mContext;
    private TextView mDialogMsg;
    private boolean mIsChecked;
    
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.clear:
            String str = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();
            Intent intent = new Intent();
            intent.putExtra("PhoneCardName", str);
            intent.setComponent(new ComponentName("com.android.filemanager", "com.android.filemanager.FileManagerActivity"));
            startActivity(intent);
            break;
        case R.id.cancel:
            finish();
            break;
        default:
        }
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_space_dialog_activity);
        
        mContext = NoSpaceActivity.this;
        mBtnClear = ((Button)findViewById(R.id.clear));
        mBtnCancel = ((Button)findViewById(R.id.cancel));
        mDialogMsg = ((TextView)findViewById(R.id.dialog_message));
        
        if (!SmartShotUtil.isInternalSDCard()) {
            mDialogMsg.setText(getString(R.string.sd_card_error_space_expired));
        }
        
        mBtnClear.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }
    
}

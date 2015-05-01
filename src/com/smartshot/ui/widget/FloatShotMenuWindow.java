package com.smartshot.ui.widget;

import com.gionee.smartshot.R;
import com.smartshot.preference.SharedPreferenceUtils;
import com.smartshot.ui.listener.ShotMenuListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatShotMenuWindow extends LinearLayout {
    public static FloatShotMenuWindow instance;
    private TextView mBtnDelete;
    private TextView mBtnScrollShot;
    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private ShotMenuListener mScrollShotListener;
    private WindowManager mWindowManager;
    private int statusBarHeight;
    private float xDownInScreen;
    private float xInScreen;
    private float xInView;
    private float yDownInScreen;
    private float yInScreen;
    private float yInView;
    
    
    public FloatShotMenuWindow(Context context) {
        super(context);
        mContext = context;
        initView();
    }
    
    public FloatShotMenuWindow(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        initView();
    }
    
    public FloatShotMenuWindow(Context context, AttributeSet attributeSet, int paramInt) {
        super(context, attributeSet, paramInt);
        mContext = context;
        initView();
    }
    
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> cls = Class.forName("com.android.internal.R$dimen");
                Object obj = cls.newInstance();
                int i = ((Integer)cls.getField("status_bar_height").get(obj)).intValue();
                statusBarHeight = mContext.getResources().getDimensionPixelSize(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return statusBarHeight;
    }
    
    private void initView() {
        instance = this;
        mWindowManager = (WindowManager)this.mContext.getSystemService("window");
        LayoutInflater.from(mContext).inflate(R.layout.screenshot_popuwindow, this);
        
        mBtnScrollShot = ((TextView)findViewById(R.id.scroll_shot_text));
        mScrollShotListener = new ShotMenuListener(mContext);
        mBtnScrollShot.setOnClickListener(mScrollShotListener);
        
        mBtnDelete = ((TextView)findViewById(R.id.btn_delete));
        mBtnDelete.setOnClickListener(new ShotMenuListener(mContext));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        motionEvent.getRawX();
        motionEvent.getRawY();
        switch (motionEvent.getAction()) {
        case 0:
            xInView = motionEvent.getX();
            yInView = motionEvent.getY();
            xDownInScreen = motionEvent.getRawX();
            yDownInScreen = (motionEvent.getRawY() - getStatusBarHeight());
            xInScreen = motionEvent.getRawX();
            yInScreen = (motionEvent.getRawY() - getStatusBarHeight());
            break;
        case 1:
            SharedPreferenceUtils.putString(mContext, SharedPreferenceUtils.KEY_MAINVIEW_COORD, mLayoutParams.x + "/" + mLayoutParams.y);
            break;
        case 2:
            xInScreen = motionEvent.getRawX();
            yInScreen = (motionEvent.getRawY() - getStatusBarHeight());
            mLayoutParams.x = (int)(xInScreen - xInView);
            mLayoutParams.y = (int)(yInScreen - yInView);
            mWindowManager.updateViewLayout(this, mLayoutParams);
            break;
        default:
        }
        
        return true;
    }
    
    public void setLayouParam(WindowManager.LayoutParams layoutParams) {
        mLayoutParams = layoutParams;
    }
    
}

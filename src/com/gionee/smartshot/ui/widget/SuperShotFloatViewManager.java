package com.gionee.smartshot.ui.widget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.gionee.smartshot.R;
import com.gionee.smartshot.ui.SuperShotFloatView;
import com.gionee.smartshot.utils.SmartShotConstant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.PowerManager;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;

public class SuperShotFloatViewManager {
    
    private static final String TAG = "SuperShotFloatViewManager";
    private static SuperShotFloatViewManager mShotFloatViewManager = null;
    private Context mContext;
    private Display mDisplay;
    private Matrix mDisplayMatrix;
    private SuperShotFloatView mFloatView;
    private WindowManager.LayoutParams mLayoutParams;
    PowerManager.WakeLock mWakeLock;
    private WindowManager mWindowManager;
    PowerManager pManager;
    private Point point;
    private int screenHeightPixels;
    private int screenWidthPixels;
    
    private SuperShotFloatViewManager(Context context) {
        mContext = context;
        mWindowManager = ((WindowManager)context.getSystemService("window"));
        point = getScreenPiont();
        setScreenWidthAndHeight(point);
        mLayoutParams = setWindowManagerParams();
    }
    
    private float getDegreesForRotation(int ratation) {
        switch (ratation) {
        case 1:
            return 270.0F;
        case 2:
            return 180.0F;
        case 3:
            return 90.0F;
        default:
            return 0.0F;
        }
    }
    
    private static synchronized void syncInit(Context context) {
        if (mShotFloatViewManager == null) {
            mShotFloatViewManager = new SuperShotFloatViewManager(context);
        }
    }
    
    public static SuperShotFloatViewManager getInstance(Context context) {
        if (mShotFloatViewManager == null) {
            syncInit(context);
        }
        return mShotFloatViewManager;
    }
    
    private void initDisplayParams() {
        mDisplayMatrix = new Matrix();
        mDisplay = mWindowManager.getDefaultDisplay();
    }
    
    private void setScreenWidthAndHeight(Point point) {
        screenWidthPixels  = point.x;
        screenHeightPixels = point.y;
    }
    
    public Point getScreenPiont() {
        initDisplayParams();
        Point point = new Point();
        mDisplay.getSize(point);
        return point;
    }
    
    public static WindowManager.LayoutParams setWindowManagerParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = -2147483360;
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.format = -2;
        layoutParams.gravity = 51;
        layoutParams.windowAnimations = R.style.shot_anim;
        layoutParams.x = 0;
        layoutParams.y = 0;
        return layoutParams;
    }
    
    public Bitmap screenShot() {
        Bitmap bitmap = null;
        setScreenWidthAndHeight(getScreenPiont());
        float[] screenPixels = new float[2];
        screenPixels[0] = screenWidthPixels;
        screenPixels[1] = screenHeightPixels;
        float degree = getDegreesForRotation(mDisplay.getRotation());
        
        if (degree != 0.0F) {
            mDisplayMatrix.reset();
            mDisplayMatrix.preRotate(-degree);
            mDisplayMatrix.mapPoints(screenPixels);
            screenPixels[0] = Math.abs(screenPixels[0]);
            screenPixels[1] = Math.abs(screenPixels[1]);
        }
        
        try {
            Class<?> cls = Class.forName("android.view.SurfaceControl");
            Method screenShotMethod = cls.getDeclaredMethod("screenshot", int.class, int.class);
            bitmap = (Bitmap)screenShotMethod.invoke(cls, (int)screenPixels[0], (int)screenPixels[1]);
            if (degree != 0.0F) {
                Bitmap bp = Bitmap.createBitmap(screenWidthPixels, screenHeightPixels, Bitmap.Config.ARGB_8888);
                Canvas cs = new Canvas(bp);
                cs.translate(bp.getWidth() / 2, bp.getHeight() / 2);
                cs.rotate(degree);
                cs.translate(-screenPixels[0] / 2.0F, -screenPixels[1] / 2.0F);
                cs.drawBitmap(bitmap, 0.0F, 0.0F, null);
                cs.setBitmap(null);
                bitmap = bp;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
        return bitmap;
    }
    
    public void startLongScreenShot() {
        if (mFloatView == null) {
            mFloatView = new SuperShotFloatView(mContext, mShotFloatViewManager, mWindowManager, mLayoutParams, point);
        }
        mContext.startService(new Intent(SmartShotConstant.ACTION_LONGSCREEN_SHOT_SERVICE));
    }
    
}

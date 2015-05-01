package com.gionee.smartshot.ui;

import com.gionee.smartshot.utils.SmartShotConstant;
import com.gionee.smartshot.utils.SmartShotUtil;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScrollShotView extends SurfaceView {
    
    private static final int RECT_COLOR = 0;
    private static final String TAG = "ScrollShotView";
    private float RADIUS = 16.0F;
    private int RECT_MIN_LENGTH = 200;
    private float STROKE_WIDTH = 7.0F;
    private Point actionDownPoint = new Point();
    private int actionDownRectBottom;
    private int actionDownRectTop;
    private Point actionMovePoint = new Point();
    private Rect clearStatusBarDrawRect;
    private Canvas mCanvas;
    private SuperShotFloatView mFloatView;
    private Handler mHandler;
    private Paint mPaint;
    private Point[] mPoints = new Point[2];
    private Rect mRect;
    private SurfaceHolder mSurfaceHolder;
    private int press2Points;
    private int screenHeight;
    private int screenWidth;
    private int statusbarHeight;

    public ScrollShotView(Context context, Rect rect, int width, int height, Handler handler, SuperShotFloatView superShotFloatView) {
        super(context);
        
        statusbarHeight = SmartShotUtil.getStatusBarHeight(context);
        mRect = rect;
        screenWidth = width;
        screenHeight = height;
        clearStatusBarDrawRect = new Rect(0, 0, width, statusbarHeight);
        Log.d("myrect", "enter CutRectView... mRect = " + screenWidth + screenHeight);
        
        mHandler = handler;
        mFloatView = superShotFloatView;
        mSurfaceHolder = getHolder();
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder surfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
                
            }
            
            public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
                drawRect();
            }
            
            public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder){
            }
        });
    }
    
    private void drawRect() {
        Log.d("myrect", "enter drawRect... mRect = " + mRect);
        mPaint = new Paint();
        mCanvas = mSurfaceHolder.lockCanvas();
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(mPaint);
        mCanvas.drawColor(Color.parseColor("#99000000"));
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawRect(mRect, mPaint);
        
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mCanvas.drawRect(mRect, mPaint);
//        mCanvas.drawLine(0, 23 * 1920 / 32 , mRect.right, 23 * 1920 / 32, mPaint);
//        mCanvas.drawLine(0, 1  * 1920 / 4 , mRect.right, 1  * 1920 / 4, mPaint);
        //Log.d("hbx", "23 * (mRect.bottom + 1) / 32 = " + 23 * 1920 / 32);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        
        if (mFloatView.getIsFirstPressNextPageButton()) {
            mCanvas.drawCircle((mRect.left + mRect.right) / 2, mRect.top, RADIUS, mPaint);
        } else {
            mCanvas.drawCircle((mRect.left + mRect.right) / 2, mRect.bottom, RADIUS, mPaint);
        }
        
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawRect(clearStatusBarDrawRect, mPaint);
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }
    
    private static int getDistence(Point point1, Point point2) {
        int x1 = point1.x;
        int y1 = point1.y;
        int x2 = point2.x;
        int y2 = point2.y;
        return (int)Math.sqrt(Math.pow(x1 - x2, 2.0d) + Math.pow(y1 - y2, 2.0d));
    }
    
    private void setPointsValue() {
        mPoints[0] = new Point((mRect.left + mRect.right) / 2, mRect.top);
        mPoints[1] = new Point((mRect.left + mRect.right) / 2, mRect.bottom);
    }
    
    private int getPosition(Point point) {
        if (press2Points > 0 && press2Points < 3) {
            return press2Points;
        }
        
        setPointsValue();
        
        for (int i = 0; i < mPoints.length; i++) {
            if (getDistence(mPoints[i], point) < 50)
                return i + 1;
        }
        
        return -1;
    }
    
    private void sendExitMessage(){
        mHandler.sendEmptyMessage(SmartShotConstant.MSG_EXIT_LONG_SCREEN_SHOT);
    }
    
    protected void onConfigurationChanged(Configuration configuration) {
        switch (configuration.orientation) {
        case Configuration.ORIENTATION_PORTRAIT:
            Log.d(TAG, "Configuration.ORIENTATION_PORTRAIT");
            sendExitMessage();
            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            Log.d(TAG, "Configuration.ORIENTATION_LANDSCAPE");
            sendExitMessage();
            break;
        default:
            
        }
    }
    
    public boolean onTouchEvent(MotionEvent motionEvent) {
        Log.d("myrect", "enter onTouch...");
        actionMovePoint.x = (int)motionEvent.getX();
        actionMovePoint.y = (int)motionEvent.getY();
        
        switch (motionEvent.getAction()) {
        case MotionEvent.ACTION_DOWN:
            Log.d("myrect", "enter event.ACTION_DOWN...");
            actionDownPoint.x = (int)motionEvent.getX();
            actionDownPoint.y = (int)motionEvent.getY();
            actionDownRectTop = mRect.top;
            actionDownRectBottom = mRect.bottom;
            break;
        case MotionEvent.ACTION_MOVE:
            Log.d("myrect", "enter event.ACTION_MOVE...");
            switch (getPosition(actionMovePoint)) {
            case 1:
                Log.d("hbx", "position = 1");
                if(mFloatView.getIsFirstPressNextPageButton()) {
                    mRect.top = this.actionMovePoint.y;
                    if (actionDownRectBottom - actionMovePoint.y < RECT_MIN_LENGTH)
                        mRect.top = actionDownRectBottom - RECT_MIN_LENGTH;
                    if (this.mRect.top < this.statusbarHeight)
                        this.mRect.top = this.statusbarHeight;
                    if (this.mRect.top > -50 + this.screenHeight / 2)
                        this.mRect.top = (-50 + this.screenHeight / 2);
                    drawRect();
                    press2Points = 1;
                }
                break;
            case 2:
                Log.d("hbx", "position = 2");
                if (!mFloatView.getIsFirstPressNextPageButton()) {
                    mRect.bottom = actionMovePoint.y;
                    if (actionMovePoint.y - actionDownRectTop < RECT_MIN_LENGTH)
                        mRect.bottom = actionDownRectTop + RECT_MIN_LENGTH;
                    if (mRect.bottom >screenHeight-1)
                        mRect.bottom = screenHeight-1;
                    if (mRect.bottom < 50 + screenHeight / 2)
                        mRect.bottom = (50 + screenHeight / 2);
                    mHandler.sendEmptyMessage(SmartShotConstant.SCROLL_SHOT_UPDATE_LL_POSITION);
                    drawRect();
                    press2Points = 2;
                }
                break;
            default:
                break;
            }
            break;
        default:
            press2Points = -1;
            //mFloatView.setNextPageTextViewEnabled(false);
            Log.d("myrect", "enter actionDownPoint = " + actionDownPoint);
            break;
        }
        
        return false;
    }
    
}

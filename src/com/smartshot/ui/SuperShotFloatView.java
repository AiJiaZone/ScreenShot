package com.smartshot.ui;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import com.gionee.smartshot.R;
import com.smartshot.model.NoticeParam;
import com.smartshot.ui.widget.SmartShotNoticeficationManager;
import com.smartshot.ui.widget.SuperShotFloatViewManager;
import com.smartshot.utils.LogUtil;
import com.smartshot.utils.SmartShotConstant;
import com.smartshot.utils.SmartShotUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SuperShotFloatView {
    
    private static int AUTO_MOVE_PROPOTION_DOWN = 0;
    private static int AUTO_MOVE_PROPOTION_UP = 0;
    private static final String TAG = "SuperShotFloatView";
    public static SuperShotFloatView instance;
    private int LL_PADDING;
    private Queue<Bitmap> bitmapQueue = new LinkedList<Bitmap>();
    private Bitmap bottomAreaBitmap;
    private int counterLongShot = 0;
    private int defaultSameArea;
    private int delTaX;
    private long downTime;
    protected int firstTopLineHeight;
    private MyHandler handler = new MyHandler(this);
    private boolean hasExit = false;
    private CompareTask imageCompareTask;
    protected int injectEventEndPoint;
    protected int injectEventStartPoint;
    private boolean isFirstComapre = true;
    private boolean isFirstPressNextPageButton = true;
    private boolean isImageMerging = false;
    private boolean isSaveComplete = true;
    private boolean isScrolledToBottom = false;
    private boolean isReachBottom = false;
    private boolean isUnscrollable = false;
    private Bitmap lastBitmap;
    private LinearLayout ll;
    private CompareTask mCompareTask;
    private Context mContext;
    private SuperShotFloatViewManager mFloatViewManager;
    private WindowManager.LayoutParams mLLTopViewParams;
    private int mLastPageY;
    private Rect mRect;
    private ScrollShotView mScrollShotView;
    private WindowManager.LayoutParams mScrollShotViewTopViewParams;
    private TreeSet<Integer> mTreeSet = new TreeSet<Integer>();
    private double mOldPer = 0.0; 
    private WindowManager mWindowManager;
    private Bitmap mergedBitmap;
    private int mergedBitmapFinalHeight;
    private Canvas mergedCanvas;
    private int moveDistance;
    private MyTask myTask = new MyTask();
    private MyTaskCompleteButton myTaskCompleteButton = new MyTaskCompleteButton();
    private TextView nextPageTextView;
    private TextView onKeyTextView;
    private int screenHeightPixels;
    private int screenWidthPixels;
    private int statusBarHeight;
    private Bitmap topAreaBitmap;
    private int touchXpoint;
    private Bitmap firstBitmap;
    private boolean isUnSlideApp = false;
    
    static {
        AUTO_MOVE_PROPOTION_UP = 30;
        AUTO_MOVE_PROPOTION_DOWN = 1280;
    }
    
    public SuperShotFloatView(Context context, SuperShotFloatViewManager superShotFloatViewManager, 
            WindowManager windowManager, WindowManager.LayoutParams layoutParams, Point point) {
        instance = this;
        mContext = context;
        mFloatViewManager = superShotFloatViewManager;
        mWindowManager = windowManager;
        mScrollShotViewTopViewParams = layoutParams;
        screenWidthPixels  = point.x;
        screenHeightPixels = point.y;
        moveDistance = (AUTO_MOVE_PROPOTION_UP * screenHeightPixels / AUTO_MOVE_PROPOTION_DOWN);
        Log.d(TAG,"moveDistance = " + moveDistance);
        mLLTopViewParams = SuperShotFloatViewManager.setWindowManagerParams();
        mLLTopViewParams.gravity = 85;
        //default values is -2, changed by hbx???
        mLLTopViewParams.width = -2;
        mLLTopViewParams.height = -2;
        mScrollShotViewTopViewParams.windowAnimations = 0;
        mLLTopViewParams.windowAnimations = 0;
        delTaX = SmartShotUtil.dip2px(mContext.getResources().getDisplayMetrics().density, 2.0F);
        defaultSameArea = SmartShotUtil.dip2px(mContext.getResources().getDisplayMetrics().density, 5.0F);
        touchXpoint = SmartShotUtil.dip2px(mContext.getResources().getDisplayMetrics().density, 2.0F);//5.0F
        Log.d("SuperShotFloatView", "delTaX = " + delTaX + " defaultSameArea = " + defaultSameArea);
        statusBarHeight = SmartShotUtil.getStatusBarHeight(mContext);
        LL_PADDING = (int)mContext.getResources().getDimension(R.dimen.scroll_shot_ll_padding);
    }
    
    private boolean compareBitmapPart(Bitmap first, Bitmap second, int width, int height, int pixels) {
        int totalCount = 0;
        int fixCount = 0;
        int color1[][] = new int[3][pixels];
        int color2[][] = new int[3][pixels];
        for (int x = 30; x < width - 30; x += 8) {
            for (int num = 0; num < pixels; num++) {
                color1[0][num] = Color.red(   first.getPixel(x, height + num));
                color1[1][num] = Color.green( first.getPixel(x, height + num));
                color1[2][num] = Color.blue(  first.getPixel(x, height + num));
                color2[0][num] = Color.red(  second.getPixel(x, height + num));
                color2[1][num] = Color.green(second.getPixel(x, height + num));
                color2[2][num] = Color.blue( second.getPixel(x, height + num));
                
                totalCount++;
                if (Math.abs(color1[0][0] - color2[0][0]) < 50 || Math.abs(color1[1][0] - color2[1][0]) < 50 ||
                        Math.abs(color1[2][0] - color2[2][0]) < 50) {
                    fixCount++;
                }
            }
        }
        
        if (fixCount * 1.0 / totalCount > 0.999d) {
            return true;
        }
        
        return false;
    }
    
    private boolean isNotSlideApp(Bitmap firstBitmap, Bitmap secondBitmap) {
        int height = firstBitmap.getHeight();
        int width  = firstBitmap.getWidth();
        
        boolean isTopSame = false;
        boolean isMid1Same = false;
        boolean isMid2Same = false;
        boolean isMid3Same = false;
        boolean isMid4Same = false;
        boolean isBottomSame = false;
        
        isTopSame = compareBitmapPart(firstBitmap, secondBitmap, width, 0, 5);
        
        isMid1Same = compareBitmapPart(firstBitmap, secondBitmap, width, height/5, 5);
        
        isMid2Same = compareBitmapPart(firstBitmap, secondBitmap, width, height*2/5, 5);
        
        isMid3Same = compareBitmapPart(firstBitmap, secondBitmap, width, height*3/5, 5);
        
        isMid4Same = compareBitmapPart(firstBitmap, secondBitmap, width, height*4/5, 5);
        
        isBottomSame = compareBitmapPart(firstBitmap, secondBitmap, width, height - 5, 5);
        
        if (!(isTopSame && isMid1Same && isMid2Same && isMid3Same && isMid4Same && isBottomSame) 
                && SmartShotUtil.isBrowser(mContext)) {
            Log.d("hbx", "compare half web page...");
            isTopSame = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, 0, 5);
            isMid1Same = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, height/5, 5);
            isMid2Same = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, height*2/5, 5);
            isMid3Same = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, height*3/5, 5);
            isMid4Same = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, height*4/5, 5);
            isBottomSame = compareBitmapPart(firstBitmap, secondBitmap, width / 2 + 29, height - 5, 5);
        }
        
        return isTopSame && isMid1Same && isMid2Same && isMid3Same && isMid4Same && isBottomSame;
    }
    
    private void compareBitmap(Bitmap bp, int beginX, int endX, int height, int pixels) {
        int x;
        int y;
        int color1[][] = new int[3][pixels];
        int color2[][] = new int[3][pixels];
        int totalCount = 0;
        int fixCount = 0;
        double oldPer = 0;
        double newPer = 0;
        boolean match = false;
        
        for (y = mergedBitmapFinalHeight - height; y <= mergedBitmapFinalHeight - pixels; y += 1) {
            for (x = beginX; x < endX; x += 10) {
                for (int num = 0; num < pixels; num++) {
                    color1[0][num] = Color.red(  mergedBitmap.getPixel(x, y + num));
                    color1[1][num] = Color.green(mergedBitmap.getPixel(x, y + num));
                    color1[2][num] = Color.blue( mergedBitmap.getPixel(x, y + num));
                    color2[0][num] = Color.red(  bp.getPixel(x, num));
                    color2[1][num] = Color.green(bp.getPixel(x, num));
                    color2[2][num] = Color.blue( bp.getPixel(x, num));
                    
                    totalCount++;
                    if (Math.abs(color1[0][num] - color2[0][num]) < 50 || Math.abs(color1[1][num] - color2[1][num]) < 50 ||
                            Math.abs(color1[2][num] - color2[2][num]) < 50) {
                        fixCount++;
                    }
                }
            }
            
            newPer = fixCount * 1.0 / totalCount ;
            
            if (newPer > 0.999d && newPer >=oldPer) {
                mTreeSet.add(Integer.valueOf(y));
                oldPer = newPer;
                match = true;
            }
            fixCount = 0;
            totalCount = 0;
        }
        if (match) {
            match = false;
            Log.d("hbx","y = " + mTreeSet.last().intValue() + " per = " + String.format("%.3f", oldPer));
        } else {
            Log.d("hbx","y no match...");
        }
        oldPer = 0;
        newPer = 0;
    }
    
    private void compareBigBitmap(Bitmap bp, int beginX, int endX, int height, int pixels) {
        int x;
        int y;
        int color1[][] = new int[3][pixels];
        int color2[][] = new int[3][pixels];
        int totalCount = 0;
        int fixCount = 0;
        double oldPer = 0;
        double newPer = 0;
        boolean match = false;
        
        for (y = mergedBitmapFinalHeight - height; y <= mergedBitmapFinalHeight - pixels; y += 1) {
            for (x = beginX; x < endX; x += 8) {
                for (int num = 0; num < pixels; num++) {
                    color1[0][num] = Color.red(  mergedBitmap.getPixel(x, y + num));
                    color1[1][num] = Color.green(mergedBitmap.getPixel(x, y + num));
                    color1[2][num] = Color.blue( mergedBitmap.getPixel(x, y + num));
                    color2[0][num] = Color.red(  bp.getPixel(x, num));
                    color2[1][num] = Color.green(bp.getPixel(x, num));
                    color2[2][num] = Color.blue( bp.getPixel(x, num));
                    
                    totalCount++;
                    if (Math.abs(color1[0][num] - color2[0][num]) < 50 || Math.abs(color1[1][num] - color2[1][num]) < 50 ||
                            Math.abs(color1[2][num] - color2[2][num]) < 50) {
                        fixCount++;
                    }
                }
            }
            
            newPer = fixCount * 1.0 / totalCount ;
            
            if (newPer > 0.99d && newPer >= oldPer) {
                mTreeSet.add(Integer.valueOf(y));
                oldPer = newPer;
                match = true;
            }
            fixCount = 0;
            totalCount = 0;
        }
        if (match) {
            match = false;
            Log.d("hbx","y = " + mTreeSet.last().intValue() + " per = " + String.format("%.3f", oldPer));
        } else {
            Log.d("hbx","y no match...");
        }
        oldPer = 0;
        newPer = 0;
    }
    
    private boolean compareBitmapPart1(Bitmap first, int beginX, int endX, int firstTop, int secondTop, int pixels) {
        int totalCount = 0;
        int fixCount = 0;
        int color1[][] = new int[3][pixels];
        int color2[][] = new int[3][pixels];
        for (int x = beginX; x < endX; x += delTaX) {
            for (int num = 0; num < pixels; num++) {
                color1[0][num] = Color.red(   first.getPixel(x, firstTop + num));
                color1[1][num] = Color.green( first.getPixel(x, firstTop + num));
                color1[2][num] = Color.blue(  first.getPixel(x, firstTop + num));
                color2[0][num] = Color.red(  mergedBitmap.getPixel(x, secondTop + num));
                color2[1][num] = Color.green(mergedBitmap.getPixel(x, secondTop + num));
                color2[2][num] = Color.blue( mergedBitmap.getPixel(x, secondTop + num));
                
                totalCount++;
                if (Math.abs(color1[0][0] - color2[0][0]) < 50 || Math.abs(color1[1][0] - color2[1][0]) < 50 ||
                        Math.abs(color1[2][0] - color2[2][0]) < 50) {
                    fixCount++;
                }
            }
        }
        
        if (fixCount * 1.0 / totalCount > 0.999d) {
            return true;
        }
        
        return false;
    }
    
    private void compareBigBitmap1(Bitmap bp, int beginX, int endX, int height, int pixels) {
        Thread ths[] = new Thread[20];
        mOldPer = 0;
        try {
            ths[0] = new Thread(new CompareRunnable(bp,beginX,endX,832,790,pixels));
            ths[0].start();
            ths[0].join();
                    
            ths[1] = new Thread(new CompareRunnable(bp,beginX,endX,789,750,pixels));
            ths[1].start();
            ths[1].join();
            
            ths[2] = new Thread(new CompareRunnable(bp,beginX,endX,749,710,pixels));
            ths[2].start();
            ths[2].join();
            
            ths[3] = new Thread(new CompareRunnable(bp,beginX,endX,709,670,pixels));
            ths[3].start();
            ths[3].join();
            
            ths[4] = new Thread(new CompareRunnable(bp,beginX,endX,669,630,pixels));
            ths[4].start();
            ths[4].join();
            
            ths[5] = new Thread(new CompareRunnable(bp,beginX,endX,629,590,pixels));
            ths[5].start();
            ths[5].join();
            
            ths[6] = new Thread(new CompareRunnable(bp,beginX,endX,589,550,pixels));
            ths[6].start();
            ths[6].join();
            
            ths[7] = new Thread(new CompareRunnable(bp,beginX,endX,549,510,pixels));
            ths[7].start();
            ths[7].join();
            
            ths[8] = new Thread(new CompareRunnable(bp,beginX,endX,509,470,pixels));
            ths[8].start();
            ths[8].join();
            
            ths[9] = new Thread(new CompareRunnable(bp,beginX,endX,469,430,pixels));
            ths[9].start();
            ths[9].join();
            
            ths[10] = new Thread(new CompareRunnable(bp,beginX,endX,429,390,pixels));
            ths[10].start();
            ths[10].join();
            
            ths[11] = new Thread(new CompareRunnable(bp,beginX,endX,389,350,pixels));
            ths[11].start();
            ths[11].join();
            
            ths[12] = new Thread(new CompareRunnable(bp,beginX,endX,349,310,pixels));
            ths[12].start();
            ths[12].join();
            
            ths[13] = new Thread(new CompareRunnable(bp,beginX,endX,309,270,pixels));
            ths[13].start();
            ths[13].join();
            
            ths[14] = new Thread(new CompareRunnable(bp,beginX,endX,269,230,pixels));
            ths[14].start();
            ths[14].join();
            
            ths[15] = new Thread(new CompareRunnable(bp,beginX,endX,229,190,pixels));
            ths[15].start();
            ths[15].join();
            
            ths[16] = new Thread(new CompareRunnable(bp,beginX,endX,189,150,pixels));
            ths[16].start();
            ths[16].join();
            
            ths[17] = new Thread(new CompareRunnable(bp,beginX,endX,149,110,pixels));
            ths[17].start();
            ths[17].join();
            
            ths[18] = new Thread(new CompareRunnable(bp,beginX,endX,109,70,pixels));
            ths[18].start();
            ths[18].join();
            
            ths[19] = new Thread(new CompareRunnable(bp,beginX,endX,69,-5,pixels));
            ths[19].start();
            ths[19].join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOldPer = 0;
        
//        for(Thread th : ths) {
//            try {
//                th.join();
//            } catch (InterruptedException e) {
//                Log.d("hbx", "compareBigBitmap1 exception...");
//                e.printStackTrace();
//            }
//        }
    }
    
    private int addImageToMergedCanvas(Bitmap bitmap) {
        
        if (isNotSlideApp(firstBitmap,bitmap)) {
            isUnSlideApp = true;
            isReachBottom = true;
            bitmap.recycle();
            mTreeSet.clear();
            Log.d("hbx", "It is not slide app or come the bottom...");
            return -2;
        }
        
        compareBitmap(bitmap, 30, screenWidthPixels - 30,10, 5);
        if (mTreeSet.size() == 0) {
//            Log.d("hbx", "compare 50 10");
//            compareBitmap(bitmap, 30, screenWidthPixels - 30,50, 10);
            if (mTreeSet.size() == 0) {
                Log.d("hbx", "compareBigBitmap 832 10");
                compareBigBitmap(bitmap, 30, screenWidthPixels - 30,832, 10);
            }
        }
        
        if (mTreeSet.size() > 0) {
            Log.d("hbx","y = " + mTreeSet.last().intValue() + " distance = " + 
                    (mergedBitmapFinalHeight - mTreeSet.last().intValue()) +
                    " mTreeSet size = " + mTreeSet.size());
            mergedCanvas.drawBitmap(bitmap, 0, mTreeSet.last().intValue(), null);
            mergedBitmapFinalHeight = bitmap.getHeight() + mTreeSet.last().intValue();
            mTreeSet.clear();
        } else {
            Log.d("hbx","defualt y = 7" );
            mergedCanvas.drawBitmap(bitmap, 0, mergedBitmapFinalHeight - 7, null);
            mergedBitmapFinalHeight += bitmap.getHeight() - 7;
        }
        firstBitmap.recycle();
        firstBitmap = bitmap;
        
        return 1;
    }
    
    private void addImageToMergedCanvasBottom(Bitmap bitmap) {
        if (bitmap != null) {
            if (isReachBottom()) {
                Log.d("hbx", "bottom compare 20 10");
                compareBitmap(bitmap, 0, screenWidthPixels - 25, 20, 10); //fix 11
            } else {
                compareBitmap(bitmap, 0, screenWidthPixels - 25, 10, 5);
                if (mTreeSet.size() == 0) {
                    Log.d("hbx", "bottom compareBigBitmap 832 5...");
                    compareBigBitmap(bitmap, 30, screenWidthPixels - 30,832, 5);
                }
            }
            
            
            if (mTreeSet.size() > 0) {
                Log.d("hbx","y = " + mTreeSet.last().intValue() + " the bottom distance = " + 
                        (mergedBitmapFinalHeight - mTreeSet.last().intValue()) +
                        " mTreeSet size = " + mTreeSet.size());
                mergedCanvas.drawBitmap(bitmap, 0, mTreeSet.last().intValue(), null);
                mergedBitmapFinalHeight = bitmap.getHeight() + mTreeSet.last().intValue();
                mTreeSet.clear();
            } else {
                if (isReachBottom()) {
                    Log.d("hbx","defualt reach bottom y = 10" );// fix 5
                    mergedCanvas.drawBitmap(bitmap, 0, mergedBitmapFinalHeight - 10, null);
                    mergedBitmapFinalHeight += bitmap.getHeight() - 10;
                } else {
                    Log.d("hbx","defualt bottom y = 7" );
                    mergedCanvas.drawBitmap(bitmap, 0, mergedBitmapFinalHeight - 7, null);
                    mergedBitmapFinalHeight += bitmap.getHeight() - 7;
                }
            }
            
            bitmap.recycle();
        }
    }
    
    private void addImageToMergedCanvasDirectly(Bitmap bitmap) {
        initMergedCanvas();
        if (bitmap != null) {
            mergedCanvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
            mergedBitmapFinalHeight = bitmap.getHeight();
            bitmap.recycle();
        }
    }
    
    private Bitmap getScreenShotImage() {
        return mFloatViewManager.screenShot();
    }
    
    private void initMergedCanvas() {
        if (mergedBitmap == null) {
            try {
                mergedBitmap = Bitmap.createBitmap(screenWidthPixels, 7 * screenHeightPixels, Bitmap.Config.ARGB_4444);
                if (mergedCanvas == null) {
                    mergedCanvas = new Canvas(mergedBitmap);
                }
            } catch (OutOfMemoryError e) {
                LogUtil.d(TAG, "Long Screen OutOfMemory, try again... e.toString() = " + e.toString());
                System.gc();
                System.runFinalization();
                mergedBitmap = Bitmap.createBitmap(screenWidthPixels, 7 * screenHeightPixels, Bitmap.Config.ARGB_4444);
            }
        }
    }
    
    private void initVaules() {
        handler.removeCallbacksAndMessages(null);
        if (imageCompareTask != null) {
            imageCompareTask.cancel(true);
            imageCompareTask = null;
        }
        
        if (mCompareTask != null) {
            mCompareTask.cancel(true);
            mCompareTask = null;
        }
        
        if (nextPageTextView != null && !nextPageTextView.isEnabled()) {
            nextPageTextView.setEnabled(true);
        }
        
        isFirstPressNextPageButton = true;
        isFirstComapre = true;
        isUnscrollable = false;
        isScrolledToBottom = false;
        isReachBottom = false;
        isUnSlideApp = false;
        isImageMerging = false;
        bitmapQueue.clear();
        counterLongShot = 0;
        mergedBitmapFinalHeight = 0;
        removeMyView();
    }
    
    private void injectPointerEvent(float startPointX, float startPointY, float endPointX, float endPointY) {
        
        Class<?> cls;
        try {
            cls = Class.forName("android.hardware.input.InputManager");
            Method staticMethod = cls.getDeclaredMethod("getInstance");  
            Object obj = staticMethod.invoke(cls);
            Method injectMethod = cls.getDeclaredMethod("injectInputEvent",InputEvent.class, int.class);
            
            MotionEvent downEvent = null;
            MotionEvent moveEvent = null;
            MotionEvent upEvent = null;
            downTime = SystemClock.uptimeMillis();
            long eventTime = downTime;
            float moveStep = startPointY;
            int stepLong = 19;
            int timeSpan = 17;
            if (SmartShotUtil.isUCBrowser(mContext)) {
                stepLong = 12;
                Log.d("hbx", "It is UC Browser running...");
            }
            //downEvent start
            downEvent = MotionEvent.obtain(downTime, eventTime, 0, startPointX, startPointY, 0);
            downEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            injectMethod.invoke(obj, downEvent, 2);
            downEvent.recycle();
            
            //moveEvent start
            moveStep -= stepLong;
            eventTime += timeSpan;
            while (moveStep >= endPointY) {
                moveEvent = MotionEvent.obtain(downTime, eventTime, 2, startPointX, moveStep, 0);
                moveEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                injectMethod.invoke(obj, moveEvent, 2);
                moveEvent.recycle();
                
                if (isReachBottom()) {
                    Log.d("hbx","move reach bottom...");
                    handler.post(new Runnable() {
                        public void run() {
                            showToastAtTop(SmartShotUtil.getResourceString(mContext, R.string.hit_the_bottom));
                            setNextPageTextViewEnabled(false);
                            isScrolledToBottom = true;
                        }
                    });
                    endPointY = moveStep;
                    mLastPageY = (int)moveStep  + stepLong * 2;
                    
                    moveEvent = MotionEvent.obtain(downTime, eventTime, 2, startPointX, moveStep, 0);
                    moveEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                    injectMethod.invoke(obj, moveEvent, 2);
                    moveEvent.recycle();
                    eventTime += timeSpan;
                    moveStep -= stepLong;
                    
                    moveEvent = MotionEvent.obtain(downTime, eventTime, 2, startPointX, moveStep, 0);
                    moveEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                    injectMethod.invoke(obj, moveEvent, 2);
                    moveEvent.recycle();
                    eventTime += timeSpan;
                    moveStep -= stepLong;
                    
                    //upEvent start
                    upEvent = MotionEvent.obtain(downTime, eventTime, 1, startPointX, moveStep, 0);
                    upEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                    injectMethod.invoke(obj, upEvent, 2);
                    upEvent.recycle();
                    return;
                }
                eventTime += timeSpan;
                moveStep -= stepLong;
            }
            
            moveEvent = MotionEvent.obtain(downTime, eventTime, 2, startPointX, endPointY, 0);
            moveEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            injectMethod.invoke(obj, moveEvent, 2);
            moveEvent.recycle();
            
            //upEvent start
            eventTime += timeSpan;
            upEvent = MotionEvent.obtain(downTime, eventTime, 1, startPointX, endPointY, 0);
            upEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            injectMethod.invoke(obj, upEvent, 2);
            upEvent.recycle();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    
    private boolean isInUnmoveableApp() {
        return (SmartShotUtil.isHome(mContext)) || (SmartShotUtil.isInUnmoveableApp(mContext));
    }
    
    private boolean isReachBottom() {
        return isReachBottom;
    }
    
    private void makeTopSurfaceViewToShotLongScreen() {
        mRect = new Rect(0, statusBarHeight, screenWidthPixels - 1, screenHeightPixels - 1);
        mScrollShotView = new ScrollShotView(mContext, mRect, screenWidthPixels, screenHeightPixels, handler, this);
        mWindowManager.addView(mScrollShotView, mScrollShotViewTopViewParams);
        
        ll = ((LinearLayout)LayoutInflater.from(this.mContext).inflate(R.layout.top_longshot_linearlayout, null));
        ShotWindowListener shotWindowListener = new ShotWindowListener();
        ((TextView)ll.findViewById(R.id.longshot_textview_exit)).setOnClickListener(shotWindowListener);
        ((TextView)ll.findViewById(R.id.longshot_textview_save)).setOnClickListener(shotWindowListener);
        
        nextPageTextView = ((TextView)ll.findViewById(R.id.longshot_textview_nextpage));
        nextPageTextView.setOnClickListener(shotWindowListener);
        
        mLLTopViewParams.x = LL_PADDING;
        mLLTopViewParams.y = (screenHeightPixels - mRect.bottom + LL_PADDING);
        mWindowManager.addView(ll, mLLTopViewParams);
        
        if (onKeyTextView == null) {
            onKeyTextView = new TextView(mContext);
            onKeyTextView.setBackgroundColor(0);
            onKeyTextView.setFocusableInTouchMode(true);
            onKeyTextView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View View, int keyCode, KeyEvent keyEvent) {
                    Log.d("SuperShotFloatView", " keyCode = " + keyCode);
                    if (keyEvent.getAction() == 1) {
                        switch (keyCode) {
                        case 3:
                            resetToDefaultValue();
                            Intent localIntent = new Intent("android.intent.action.MAIN");
                            localIntent.addCategory("android.intent.category.HOME");
                            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//270532608
                            mContext.startActivity(localIntent);
                            return true;
                        case 4:
                            resetToDefaultValue();
                            return true;
                        default:
                            
                        }
                    }
                    return false;
                }
            });
            mWindowManager.addView(onKeyTextView, mLLTopViewParams);
        }
    }
    
    private void mergeLongShotImageAndSaveToTFCardCanvas() {
        if (bottomAreaBitmap != null) {
            if (isUnscrollable) {
                addImageToMergedCanvasBottom(bottomAreaBitmap);
                if (!bottomAreaBitmap.isRecycled()) {
                    bottomAreaBitmap.recycle();
                }
                bottomAreaBitmap = null;
            }
        } else {
            if (firstTopLineHeight != statusBarHeight) {
                int j;
                j = mergedBitmapFinalHeight - (screenHeightPixels - mRect.bottom-1) - firstTopLineHeight;
                if (j > 0) {
                    try {
                        Bitmap localBitmap1 = Bitmap.createBitmap(mergedBitmap, 0, firstTopLineHeight, screenWidthPixels, j);
                        mergedBitmap.recycle();
                        mergedBitmap = null;
                        saveScrollShotImageToTFCard(localBitmap1, "", mContext);
                    } catch (OutOfMemoryError e) {
                        LogUtil.d("SuperShotFloatView", "OutOfMemoryError mergeLongShot...e.tostring = " + e.toString());
                        System.gc();
                        System.runFinalization();
                    }
                } else {
                    Bitmap localBitmap3 = Bitmap.createBitmap(lastBitmap, 0, mRect.top, screenWidthPixels, mRect.height());
                    lastBitmap.recycle();
                    lastBitmap = null;
                    saveScrollShotImageToTFCard(localBitmap3, "", this.mContext);
                    addImageToMergedCanvas(bottomAreaBitmap);
                }
            }
            
        }
    }
    
    //????
    private void nextPageActionCanvas() {
        setMyViewVisibility(View.INVISIBLE);
        handler.postDelayed(new Runnable(){
            public void run() {
                if (SmartShotUtil.isInDirectlyHintToBottom(mContext)) {
                    counterLongShot++;
                    lastBitmap = getScreenShotImage();
                    showToastAtTop(SmartShotUtil.getResourceString(mContext, R.string.hit_the_bottom));
                    setNextPageTextViewEnabled(false);
                    setMyViewVisibility(0);
                    return;
                }
                
                if (isInUnmoveableApp()) {
                    showToastAtTop(SmartShotUtil.getResourceString(mContext, R.string.content_un_extended));
                    resetToDefaultValue();
                    return;
                }
                
                if (counterLongShot == 10 && !isReachBottom()) {
                    showToastAtTop(mContext.getString(R.string.maximum_to_ten_pages));
                    setNextPageTextViewEnabled(false);
                    setMyViewVisibility(0);
                    return;
                }
                counterLongShot++;
                
                Bitmap bitmap1 = getScreenShotImage();
                Bitmap bitmap2 = Bitmap.createBitmap(bitmap1, 0, injectEventEndPoint, screenWidthPixels, injectEventStartPoint - injectEventEndPoint);
                
//                Log.d("hbx" , "shot height = " + (injectEventStartPoint - injectEventEndPoint) + " injectEventStartPoint = " + 
//                        injectEventStartPoint + " injectEventEndPoint = " + injectEventEndPoint);
                
                if (isFirstPressNextPageButton) {
                    topAreaBitmap = Bitmap.createBitmap(bitmap1, 0, 0, screenWidthPixels, injectEventStartPoint);
                    addImageToMergedCanvasDirectly(topAreaBitmap);
                    isFirstPressNextPageButton = false;
                    firstTopLineHeight = mRect.top;
                    mRect.top = statusBarHeight;
                    bitmap1.recycle();
                    firstBitmap = bitmap2;
                } else {
                    bitmapQueue.offer(bitmap2);
                    if (isImageMerging) {
                        Log.d("hbx", "delay 50L merge...");
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                new CompareTask().execute();
                            }
                        }, 50L);
                    } else {
                        new CompareTask().execute();
                    }
                    
//                    if (imageCompareTask == null) {
//                        imageCompareTask = new CompareTask();
//                    } else {
//                        imageCompareTask = null;
//                        imageCompareTask = new CompareTask();
//                    }
//                    imageCompareTask.execute(new Void[0]);
                }
                
                new Thread(new Runnable() {
                    public void run() {
                        if (!isInUnmoveableApp()) {
                            if (SmartShotUtil.isInGallery3D(mContext)) {
                                injectPointerEvent(touchXpoint, injectEventStartPoint, touchXpoint, injectEventEndPoint + SmartShotUtil.dip2px(mContext, 36.0F));
                            } else {
                                injectPointerEvent(touchXpoint, injectEventStartPoint, touchXpoint, injectEventEndPoint);
                            }
                        }
                        
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                setMyViewVisibility(View.VISIBLE);
                            }
                        }, 800L);
                        
                    }
                }).start();
            }
        }
        , 20L);
    }
    
    private void pressCompleteButton() {
        Log.d(TAG, "enter pressCompleteButton");
        
        setNextPageTextViewEnabled(true);
        
        new Thread(new Runnable() {
            public void run() {
                if (!hasExit) {
                    mergeLongShotImageAndSaveToTFCardCanvas();
                    handler.post(new Runnable(){
                        public void run() {
                            resetToDefaultValue();
                        }
                    });
                }
            }
        }).start();
    }
    
    private void setScreenWidthHeightAndLayoutParams() {////???
        Point point = mFloatViewManager.getScreenPiont();
        screenWidthPixels  = point.x;
        screenHeightPixels = point.y;
        mScrollShotViewTopViewParams.width  = screenWidthPixels;
        mScrollShotViewTopViewParams.height = screenHeightPixels;
        
        //injectEventStartPoint = 27 * screenHeightPixels / 32;
        injectEventStartPoint = 23 * screenHeightPixels / 32;
        if (screenHeightPixels == 480) {
            injectEventStartPoint = 26 * screenHeightPixels / 32;
        } else if (screenHeightPixels == 720) {
            injectEventStartPoint = injectEventStartPoint - 5;
        }
        //injectEventEndPoint = 1 * screenHeightPixels / 8;
        injectEventEndPoint = 1 * screenHeightPixels / 4; //screenHeightPixels = 1776
        if (screenHeightPixels == 960) {
            injectEventEndPoint = (10 + 1 * this.screenHeightPixels / 4);
        } else if (screenHeightPixels == 1920 && SmartShotUtil.isInIManagerPhoneClean(mContext)) {
            injectEventEndPoint = 850;
        }
        
    }
    
    private void showToastAtTop(String str) {
        Toast toast = Toast.makeText(mContext, str, 0);
        toast.setGravity(48, 0, 100);
        toast.show();
    }

    
    private void startCompareTask() {
        mCompareTask = new CompareTask();
        mCompareTask.execute(new Void[0]);
    }
    
    public boolean getIsFirstPressNextPageButton() {
        return isFirstPressNextPageButton;
    }

    public boolean isSaveComplete() {
        return isSaveComplete;
    }
    
    public void resetToDefaultValue() {
        Log.d("SuperShotFloatView", "enter ------>resetToDefaultValue");
        hasExit = true;
        initVaules();
        mContext.stopService(new Intent(SmartShotConstant.ACTION_LONGSCREEN_SHOT_SERVICE));
    }
    
    
    private void setMyViewVisibility(int arg) {
        if (mScrollShotView != null && mScrollShotView.getVisibility() != arg) {
            mScrollShotView.setVisibility(arg);
        }
        
        if (ll != null && ll.getVisibility() != arg) {
            ll.setVisibility(arg);
        }
    }
    
    protected void setNextPageTextViewEnabled(boolean isEnable) {
        if (nextPageTextView != null && nextPageTextView.isEnabled() != isEnable) {
            nextPageTextView.setEnabled(isEnable);
        }
    }
    
    public void showFloatViewToDoLongShot() {
        Settings.System.putInt(mContext.getContentResolver(), "listview_oversroll", 0);
        setScreenWidthHeightAndLayoutParams();
        
        if (!isSaveComplete) {
            Intent intent = new Intent(mContext, LongShotIsSavingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return;
        }
        
        initVaules();
        hasExit = false;
        if (mergedBitmap != null) {
            mergedBitmap.recycle();
            mergedBitmap = null;
        }
        mergedCanvas = null;
        if (mScrollShotView == null && ll == null) {
            makeTopSurfaceViewToShotLongScreen();
        } else {
            setMyViewVisibility(View.VISIBLE);
        }
        
        new Thread() {
            public void run() {
                if (!hasExit)
                    try {
                        Thread.sleep(200L);
                        System.gc();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }.start();
    }
    
    private void removeMyView() {
        Log.d(TAG, "remove my view.");
        
        nextPageTextView = null;
        
        if (mScrollShotView != null) {
            mWindowManager.removeView(mScrollShotView);
            mScrollShotView = null;
        }
        
        if (ll != null) {
            mWindowManager.removeView(ll);
            ll = null;
        }
        
        if (onKeyTextView != null) {
            mWindowManager.removeView(onKeyTextView);
            onKeyTextView = null;
        }
        
    }
    
    private void saveActionCanvas() {
        setMyViewVisibility(View.GONE);
        removeMyView();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SmartShotUtil.isInDirectlyHintToBottom(mContext)) {
                    if (counterLongShot == 0) {
                        Log.d("hbx","come here 1...");
                        lastBitmap = getScreenShotImage();
                    }
                    sendSavingNotification();
                    
                    if (mRect.top == statusBarHeight) {
                        Log.d("hbx","come here 2...");
                        saveScrollShotImageToTFCardDirectly(lastBitmap, "", mContext);
                        return;
                    }
                    
                    Log.d("hbx","come here 3...");
                    Bitmap localBitmap2 = Bitmap.createBitmap(lastBitmap, 0, mRect.top, screenWidthPixels, mRect.height());
                    lastBitmap.recycle();
                    lastBitmap = null;
                    saveScrollShotImageToTFCardDirectly(localBitmap2, "", mContext);
                    return;
                }
                
                lastBitmap = getScreenShotImage();
                if (counterLongShot == 0) {
                    sendSavingNotification();
                    if (mRect.top == statusBarHeight) {
                        saveScrollShotImageToTFCardDirectly(lastBitmap, "", mContext);
                        return;
                    }
                    Bitmap localBitmap1 = Bitmap.createBitmap(lastBitmap, 0, mRect.top, screenWidthPixels, mRect.height());
                    lastBitmap.recycle();
                    lastBitmap = null;
                    saveScrollShotImageToTFCardDirectly(localBitmap1, "", mContext);
                    return;
                }
                
                if (isReachBottom()) {
                    if (isUnSlideApp) {
                        mLastPageY = injectEventStartPoint - 10;
                        Log.d("hbx","saveAction is not slide app...");
                    }
                    bottomAreaBitmap = Bitmap.createBitmap(lastBitmap, 0, mLastPageY, screenWidthPixels, screenHeightPixels - (mLastPageY + 1));
                } else {
                    if (isUnscrollable) {
                        bottomAreaBitmap = Bitmap.createBitmap(lastBitmap, 0, injectEventStartPoint, screenWidthPixels, screenHeightPixels - injectEventStartPoint);
                    }
                    bottomAreaBitmap = Bitmap.createBitmap(lastBitmap, 0, injectEventEndPoint, screenWidthPixels, screenHeightPixels - injectEventEndPoint);
                }
                
//                if (imageCompareTask == null) {
//                    imageCompareTask = new CompareTask();
//                    imageCompareTask.execute(new Void[0]);
//                }
                lastBitmap.recycle();
                lastBitmap = null;
                sendSavingNotification();
                addImageToMergedCanvasBottom(bottomAreaBitmap);
                saveScrollShotImageToTFCardDirectly(mergedBitmap, "", mContext);
                //handler.postDelayed(new MyTaskCompleteButton(), 50L);
            }
        }, 20L);
        
    }
    
    private void saveScrollShotImageToTFCard(Bitmap bitmap, String str, Context context) {
        SmartShotUtil.saveScrollShotImageToTFCard(bitmap, "JPG", context);
        isSaveComplete = true;
        if (LongShotIsSavingActivity.instance != null) {
            LongShotIsSavingActivity.instance.finish();
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
    
    private void saveScrollShotImageToTFCardDirectly(final Bitmap bitmap, final String str, final Context context) {
        new Thread(new Runnable() {
            public void run() {
                if (mergedBitmapFinalHeight > 0 && mergedBitmapFinalHeight < 7 * screenHeightPixels) {
                    Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidthPixels, mergedBitmapFinalHeight);
                    SmartShotUtil.saveScrollShotImageToTFCard(tempBitmap, "JPG", context);
                    if (tempBitmap != null && !tempBitmap.isRecycled()) {
                        tempBitmap.recycle();
                    }
                } else {
                    SmartShotUtil.saveScrollShotImageToTFCard(bitmap, "JPG", context);
                }
                
                isUnscrollable = true;
                if (LongShotIsSavingActivity.instance != null) {
                    LongShotIsSavingActivity.instance.finish();
                }
                
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                
                if (firstBitmap != null && !firstBitmap.isRecycled()) {
                    firstBitmap.recycle();
                }
                
                resetToDefaultValue();
            }
        }).start();
    }
    
    private void sendSavingNotification() {
        Toast.makeText(mContext, SmartShotUtil.getResourceString(mContext, R.string.saving), 0).show();
        NoticeParam noticeParam = new NoticeParam(SmartShotConstant.NOTICEFICATION_ID_OF_SCROLL_SHOT_SAVING, 
                SmartShotUtil.getResourceString(mContext, R.string.screenshot_saving), 
                SmartShotUtil.getResourceString(mContext, R.string.screenshot_saving), null, R.drawable.image_save_noti,
                SmartShotUtil.getResourceString(mContext, R.string.screenshot_saving));
        new SmartShotNoticeficationManager(mContext).sendImageSaveNoticefication(noticeParam);
    }
    
    private class CompareTask extends AsyncTask<Void, Void, Integer> {
        private CompareTask() {
            
        }
        
        protected Integer doInBackground(Void[] paramArray) {
            Log.d(TAG, "enter doInBackground");
            int result = -1;
            
            if (hasExit || bitmapQueue == null || bitmapQueue.isEmpty()) {
                return -1;
            }
            
            Bitmap bitmap = (Bitmap)bitmapQueue.poll();
            Log.d("scrollshot", "start  addImageToMerged...");
            if (bitmap != null){
                isImageMerging = true;
                result = addImageToMergedCanvas(bitmap);
                isImageMerging = false;
            }
            
            return result;
        }
        
        protected void onPostExecute(Integer integer) {
            Log.d("SuperShotFloatView", "addImageToMerged finish");
            if (!hasExit) {
                switch (integer.intValue()) {
                case 1:
                    Log.d("SuperShotFloatView", "case 1 : start new asynctask...");
                    handler.post(new MyTask());
                    break;
                case -2:
                    Log.d("SuperShotFloatView", "case -2:");
                    if (!hasExit && mScrollShotView != null && mScrollShotView.getVisibility() != View.GONE) {
                        showToastAtTop(SmartShotUtil.getResourceString(mContext, R.string.hit_the_bottom));
                    }
                    setNextPageTextViewEnabled(false);
                    isScrolledToBottom = true;
                    break;
                case -1:
                    Log.d("SuperShotFloatView", "case -1");
                    break;
                case 0:
                    break;
                default:
                }
            }
        }
    }
    
    private static class MyHandler extends Handler{
        private WeakReference<SuperShotFloatView> mSuperShotFloatViewReference;
        
        public MyHandler(SuperShotFloatView superShotFloatView)
        {
            mSuperShotFloatViewReference = new WeakReference<SuperShotFloatView>(superShotFloatView);
        }
        
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            SuperShotFloatView superShotFloatView = (SuperShotFloatView)mSuperShotFloatViewReference.get();
            if (superShotFloatView != null) {
                switch (msg.what) {
                case SmartShotConstant.SCROLL_SHOT_UPDATE_LL_POSITION:
                    superShotFloatView.mLLTopViewParams.y = (superShotFloatView.screenHeightPixels - superShotFloatView.mRect.bottom + superShotFloatView.LL_PADDING);
                    superShotFloatView.mWindowManager.updateViewLayout(superShotFloatView.ll, superShotFloatView.mLLTopViewParams);
                    break;
                case SmartShotConstant.MSG_EXIT_LONG_SCREEN_SHOT:
                    Toast.makeText(superShotFloatView.mContext, SmartShotUtil.getResourceString(superShotFloatView.mContext, 
                            R.string.not_support_screen_orientation_change), 0).show();
                    superShotFloatView.resetToDefaultValue();
                    break;
                default:
                }
            }
            
        }
    }
    
    private class MyTask implements Runnable {
        private MyTask() {
            
        }
        
        public void run() {
            if (!hasExit && !isScrolledToBottom) {
                if (!bitmapQueue.isEmpty()) {
                    startCompareTask();
                }
            } else {
                //shandler.postDelayed(myTask, 50);
            }
        }
    }
    
    private class MyTaskCompleteButton implements Runnable {
        private MyTaskCompleteButton() {
            
        }
        
        public void run() {
            Log.d(TAG, "enter mytaskcompletebutton");
            
            if (!hasExit) {
                if (!bitmapQueue.isEmpty()) {
                    if (isImageMerging){
                        handler.postDelayed(myTaskCompleteButton, 50L);
                        return;
                    }
                    
                    if (isScrolledToBottom) {
                        handler.post(new Runnable() {
                            public void run() {
                                pressCompleteButton();
                            }
                        });
                        return;
                    }
                    
                }
            }
            
            if (isImageMerging) {
                handler.postDelayed(myTaskCompleteButton, 50L);
            }
            
            if (isScrolledToBottom) {
                handler.post(new Runnable(){
                    public void run(){
                        pressCompleteButton();
                    }
                });
            }
            
            handler.postDelayed(myTaskCompleteButton, 50L);
        }
    }
    private class ShotWindowListener implements View.OnClickListener {
        private ShotWindowListener() {
            
        }
        
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.longshot_textview_exit:
                resetToDefaultValue();
                break;
            case R.id.longshot_textview_save:
                saveActionCanvas();
                break;
            case R.id.longshot_textview_nextpage:
                nextPageActionCanvas();
                break;
            default:
            }
            
        }
    }

    private class CompareRunnable implements Runnable {
        private Bitmap bitmap;
        private int beginX;
        private int endX;
        private int beginY;
        private int endY;
        private int pixels;
        private int color1[][];
        private int color2[][];

        
        public CompareRunnable (Bitmap bp, int beginX, int endX, int beginY, int endY, int pixels) {
            this.bitmap = bp;
            this.beginX = beginX;
            this.endX = endX;
            this.beginY = beginY;
            this.endY = endY;
            this.pixels = pixels;
            this.color1 = new int[3][pixels];
            this.color2 = new int[3][pixels];
        }

        @Override
        public void run() {
            int y = 0;
            int x = 0;
            int totalCount = 0;
            int fixCount = 0;
            double newPer = 0;
            boolean match = false;
            Log.d("hbx", "begin FinalHeight = " + (mergedBitmapFinalHeight - beginY) + " end FinalHeight = " + (mergedBitmapFinalHeight - endY));
            for (y = mergedBitmapFinalHeight - beginY; y <= mergedBitmapFinalHeight - endY; y += 1) {
                for (x = beginX; x < endX; x += 8) {
                    for (int num = 0; num < pixels; num++) {
                        color1[0][num] = Color.red(  mergedBitmap.getPixel(x, y + num));
                        color1[1][num] = Color.green(mergedBitmap.getPixel(x, y + num));
                        color1[2][num] = Color.blue( mergedBitmap.getPixel(x, y + num));
                        color2[0][num] = Color.red(  bitmap.getPixel(x, num));
                        color2[1][num] = Color.green(bitmap.getPixel(x, num));
                        color2[2][num] = Color.blue( bitmap.getPixel(x, num));
                        totalCount++;
                        if (Math.abs(color1[0][num] - color2[0][num]) < 50 || Math.abs(color1[1][num] - color2[1][num]) < 50 ||
                                Math.abs(color1[2][num] - color2[2][num]) < 50) {
                            fixCount++;
                        }
                    }
                }
                
                newPer = fixCount * 1.0 / totalCount ;
                if (newPer > 0.98d && newPer >= mOldPer) {
                    mTreeSet.add(y);
                    mOldPer = newPer;
                    match = true;
                }
                fixCount = 0;
                totalCount = 0;
            }
            
            if (match) {
                match = false;
                Log.d("hbx","y = " + mTreeSet.last().intValue() + " per = " + String.format("%.3f", mOldPer));
            } else {
                Log.d("hbx","y no match...");
            }
            newPer = 0;
        }
    }
    
    private class MergingThread extends Thread {
        private Bitmap mergingBP;
        public void run() {
            try{
                while(true) {
                    while((mergingBP = bitmapQueue.poll()) != null) {
                        System.out.println("is not empty");
                    }
                    
                    System.out.println("挂起");
                    wait();
                }   
            } catch(Exception e) {         
                System.out.println("MergingThread Error");     
            }  
        }
    }
}

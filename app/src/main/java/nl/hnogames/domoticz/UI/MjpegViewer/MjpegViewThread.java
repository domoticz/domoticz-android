package nl.hnogames.domoticz.UI.MjpegViewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by YJ Kim on 2017-02-23.
 */

public class MjpegViewThread extends Thread {
    public MjpegCallback mCallback;
    public int displayMode;
    private SurfaceHolder mSurfaceHolder;
    private MjpegInputStream mInputStream;
    private Bitmap imgBack;
    private boolean mRun;
    private Rect destRect;
    private Rect destSrc;
    private int dispWidth;
    private int dispHeight;
    private int prevdispWidth;
    private int prevdispHeight;
    private int prevdisplayMode;
    private int imgBackWidth = 0;
    private int imgBackHeight = 0;
    private int previmgBackWidth = 0;
    private int previmgBackHeight = 0;

    private Canvas c = null;
    private Paint bgColor;

    public MjpegViewThread(SurfaceHolder surfaceHolder, View view) {
        mSurfaceHolder = surfaceHolder;
        mRun = false;
        mCallback = null;
        dispWidth = view.getWidth();
        dispHeight = view.getHeight();
        prevdispWidth = dispWidth;
        prevdispHeight = dispHeight;
        destRect = new Rect(0, 0, dispWidth, dispHeight);
        destSrc = null;
        displayMode = 1; // Default : SIZE_FIT_MODE
        prevdisplayMode = 1;

        bgColor = new Paint();
        bgColor.setStyle(Paint.Style.FILL);
        bgColor.setARGB(255, 0, 0, 0);
    }

    public void StopRunning() {
        mRun = false;
    }

    public void SetViewSize(int vw, int vh) {
        synchronized (mSurfaceHolder) {
            dispWidth = vw;
            dispHeight = vh;
        }
    }

    private void UpdateDestRect(int bmw, int bmh) {
        if (displayMode == 1) {
            // SIZE FIT MODE
            int tempx;
            int tempy;

            float bmasp = (float) bmw / (float) bmh;
            bmw = dispWidth;
            bmh = (int) (dispWidth / bmasp);
            if (bmh > dispHeight) {
                bmh = dispHeight;
                bmw = (int) (dispHeight * bmasp);
            }

            tempx = (dispWidth / 2) - (bmw / 2);
            tempy = (dispHeight / 2) - (bmh / 2);
            destRect = new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            destSrc = null;
        } else {
            // FULL SIZE FIT(CENTER CROP)
            float viewRatio = (float) dispWidth / (float) dispHeight;
            float newHeight = (bmh > bmw / viewRatio) ? bmh / viewRatio : bmh;
            float newWidth = newHeight * viewRatio;
            float offSetWidth = (bmh > bmw / viewRatio) ? (bmw - newWidth) / 2 : 0;
            float offSetHeight = (bmh > bmw / viewRatio) ? 0 : (bmh - newHeight) / 2;

            destRect = new Rect(0, 0, dispWidth, dispHeight);
            destSrc = new Rect((int) offSetWidth, (int) offSetHeight, (int) newWidth, (int) newHeight);
        }
    }

    private void AlertState(int s) {
        if (mCallback != null) mCallback.onStateChange(s);
    }

    public void setInputStream(MjpegInputStream mIn) {
        mInputStream = mIn;
        mRun = true;
    }

    @Override
    public void run() {
        while (mRun) {
            c = null;
            // 1. Get image
            try {
                imgBack = mInputStream.readMjpegFrame();
                imgBackWidth = imgBack.getWidth();
                imgBackHeight = imgBack.getHeight();
                if (imgBack != null) {
                    if (imgBackWidth != previmgBackWidth || imgBackHeight != previmgBackHeight) {
                        UpdateDestRect(imgBack.getWidth(), imgBack.getHeight());
                        previmgBackHeight = imgBackHeight;
                        previmgBackWidth = imgBackWidth;
                    } else if (dispWidth != prevdispWidth || dispHeight != prevdispHeight) {
                        UpdateDestRect(imgBack.getWidth(), imgBack.getHeight());
                        prevdispWidth = dispWidth;
                        prevdispHeight = dispHeight;
                    } else if (displayMode != prevdisplayMode) {
                        UpdateDestRect(imgBack.getWidth(), imgBack.getHeight());
                        prevdisplayMode = displayMode;
                    }
                }
            } catch (Exception e) {
                imgBack = null;
            }

            if (imgBack == null) {
                AlertState(3);
                mRun = false;
            }

            // 2. Image update
            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    if (c != null && imgBack != null) {
                        c.drawRect(0, 0, c.getWidth(), c.getHeight(), bgColor);
                        c.drawBitmap(imgBack, destSrc, destRect, null);
                    }
                }
            } finally {
                if (c != null) mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
}

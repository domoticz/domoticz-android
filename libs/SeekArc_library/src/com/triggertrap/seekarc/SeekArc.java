/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.triggertrap.seekarc;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * SeekArc.java
 * <p>
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 *
 * @author Neil Davies
 */
public class SeekArc extends View {

    private static final String TAG = SeekArc.class.getSimpleName();
    private static int INVALID_PROGRESS_VALUE = -1;
    // The initial rotational offset -90 means we start at 12 o'clock
    private final int mAngleOffset = -90;

    /**
     * The Drawable for the seek arc thumbnail
     */
    private Drawable mThumb;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mProgressMax = 100;

    /**
     * The Current value that the SeekArc is set to
     */
    private int mProgress = 0;

    private int mProgressMin = 0;

    /**
     * The width of the progress line for this SeekArc
     */
    private int mProgressWidth = 4;

    /**
     * The Width of the background arc for the SeekArc
     */
    private int mArcWidth = 2;

    /**
     * Clock face lines width
     */
    private float mClockfaceWidth = 1;

    private int mClockfacePadding = 40;

    private Rect mClockfaceRect = new Rect();

    /**
     * Clock face font size in sp
     */
    private int mClockfaceFontSize = 14;

    /**
     * The Angle to start drawing this Arc from
     */
    private int mStartAngle = 0;

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private int mSweepAngle = 360;

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private int mRotation = 0;

    /**
     * Give the SeekArc rounded edges
     */
    private boolean mRoundedEdges = false;

    /**
     * Show clock face
     */
    private boolean mShowClockface = true;

    /**
     * Enable touch inside the SeekArc
     */
    private boolean mTouchInside = true;

    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    private boolean mClockwise = true;


    /**
     * is the control enabled/touchable
     */
    private boolean mEnabled = true;

    private boolean isLeapEnabled = true;

    private boolean mTouchInProgress = false;

    private float mLeapProgressValue;

    private int mClockfaceSide = 1;

    // Internal variables
    private float mArcRadius = 0;
    private float mProgressSweep = 0;
    private RectF mArcRect = new RectF();
    private Paint mArcPaint;
    private Paint mProgressPaint;
    private Paint mClockfacePaint;
    private float mTranslateX;
    private float mTranslateY;
    private float mThumbXPos;
    private float mThumbYPos;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    @Nullable
    private OnSeekArcChangeListener mOnSeekArcChangeListener;

    public interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc  The SeekArc whose progress has changed
         * @param progress The current progress level. This will be in the range
         *                 0..max where max was set by
         *                 {@link ProgressArc#setMax(int)}. (The default value for
         *                 max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        void onStartTrackingTouch(SeekArc seekArc);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        void onStopTrackingTouch(SeekArc seekArc);

        boolean onTrackingLeap(SeekArc seekArc, boolean isRising);
    }

    public SeekArc(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SeekArc(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public SeekArc(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        Log.d(TAG, "Initialising SeekArc");
        final Resources res = context.getResources();
        float density = context.getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings
        int arcColor = res.getColor(R.color.progress_gray);
        int progressColor = res.getColor(R.color.default_blue_light);
        int clockfaceColor = res.getColor(R.color.clockface_gray);
        int thumbHalfheight = 0;
        int thumbHalfWidth = 0;
        mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
        // Convert progress width to pixels for current density
        mProgressWidth = (int) (mProgressWidth * density);


        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekArc, defStyle, 0);

            Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (thumb != null) {
                mThumb = thumb;
            }

            a.getString(R.styleable.SeekArc_clockStyle);


            thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
            mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);

            setMax(a.getInteger(R.styleable.SeekArc_progressMax, mProgressMax));
            mProgress = a.getInteger(R.styleable.SeekArc_progress, mProgress);
            mProgressMin = a.getInteger(R.styleable.SeekArc_progressMin, mProgressMin);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.SeekArc_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);

            mClockfaceFontSize = (int) a.getDimension(R.styleable.SeekArc_clockfaceFontSize, mClockfaceFontSize);

            mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
                    mRoundedEdges);
            mShowClockface = a.getBoolean(R.styleable.SeekArc_showClockface, mShowClockface);

            mClockfaceWidth = a.getFloat(R.styleable.SeekArc_clockfaceFontThickness, mClockfaceWidth);

            mClockfacePadding = a.getDimensionPixelSize(R.styleable.SeekArc_clockfacePadding, mClockfacePadding);

            clockfaceColor = a.getColor(R.styleable.SeekArc_clockfaceFontColor, clockfaceColor);

            mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise,
                    mClockwise);
            mEnabled = a.getBoolean(R.styleable.SeekArc_enabled, mEnabled);

            arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
            progressColor = a.getColor(R.styleable.SeekArc_progressColor,
                    progressColor);

            a.recycle();
        }

        mProgress = (mProgress > mProgressMax) ? mProgressMax : mProgress;
        mProgress = (mProgress < mProgressMin) ? mProgressMin : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mProgressSweep = (float) mProgress / mProgressMax * mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);
        //mArcPaint.setAlpha(45);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);


        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mClockfaceFontSize, getResources().getDisplayMetrics());

        Log.d(TAG, String.valueOf(mClockfaceFontSize) + " : " + String.valueOf(px));

        mClockfacePaint = new Paint();
        mClockfacePaint.setColor(clockfaceColor);
        mClockfacePaint.setAntiAlias(true);
        mClockfacePaint.setStyle(Paint.Style.FILL);
        mClockfacePaint.setStrokeWidth(mClockfaceWidth);
        mClockfacePaint.setTextSize(mClockfaceFontSize);
        mClockfacePaint.setTextAlign(Paint.Align.CENTER);

        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = canvas.getWidth() / 2;
        float centerY = canvas.getHeight() / 2;

        if (!mClockwise) {
            canvas.scale(-1, 1, centerY, centerX);
        }
        mArcRect.set(centerX - mArcRadius, centerY - mArcRadius, centerX + mArcRadius, centerY + mArcRadius);


        // Draw the arcs
            final int arcStart = mStartAngle + mAngleOffset + mRotation;
            final int arcSweep = mSweepAngle;
            canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
            canvas.drawArc(mArcRect, arcStart, mProgressSweep, false,
                    mProgressPaint);

        if (mShowClockface) {

            float radius = mArcRadius + (mClockfaceSide == 1 ? mClockfacePadding : (mClockfacePadding * -1));
            for (int i = 0; i < 12; i++) {
                String number = String.valueOf(i * 5);
                mClockfacePaint.getTextBounds(number, 0, number.length(), mClockfaceRect);
                double angle = Math.PI / 6 * (i - 3);
                float x = (float) (centerX + Math.cos(angle) * (radius + mClockfaceRect.width() / 2));
                float y = (float) (centerY + Math.sin(angle) * (radius + mClockfaceRect.height() / 2) + mClockfaceRect.height() / 2);
                canvas.drawText(number, x, y, mClockfacePaint);
            }
        }

        if (mEnabled) {
            // Draw the thumb nail
            canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
            mThumb.draw(canvas);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int min = Math.min(width, height);
        float arcDiameter;

        mTranslateX = width * 0.5f;
        mTranslateY = height * 0.5f;

        arcDiameter = min - getPaddingLeft() - ((mShowClockface && mClockfaceSide == 1) ? mClockfacePadding * 2 : 0);
        mArcRadius = arcDiameter / 2;

        float arcStart = mProgressSweep + mStartAngle + mRotation + 90;
        mThumbXPos = (float) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (float) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

        setTouchInSide(mTouchInside);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchInProgress = true;
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                    mTouchInProgress = false;
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mTouchInProgress = false;
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
        }
    }

    private void updateOnTouch(MotionEvent event) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) {
            return;
        }
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        int progress = getProgressForAngle(mTouchAngle);
        if (leapProgress(progress)) {
            if (!(isLeapEnabled && onTrackingLeap((progress - mProgress) < 0))) {
                progress = getCorrectedProgress();
            }
        }
        onProgressRefresh(progress, true);
    }

    private boolean onTrackingLeap(boolean rising) {
        if (mOnSeekArcChangeListener != null) {
            return mOnSeekArcChangeListener.onTrackingLeap(this, rising);
        } else {
            return true;
        }
    }

    public int getCorrectedProgress() {
        if (mProgress > (mProgressMax * 0.9f)) {
            return mProgressMax;
        } else {
            return mProgressMin;
        }
    }

    private boolean leapProgress(int progress) {
        return mTouchInProgress && Math.abs(mProgress - progress) > mLeapProgressValue;
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mProgressMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        return (float) mProgressMax / mSweepAngle;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
    }

    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = (progress > mProgressMax) ? mProgressMax : progress;

        if (fromUser) {
            progress = (progress < mProgressMin) ? mProgressMin : progress;
        }
        mProgress = progress;

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, progress, fromUser);
        }

        mProgressSweep = (float) progress / mProgressMax * mSweepAngle;

        updateThumbPosition();

        invalidate();
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l The seek bar notification listener
     * @see SeekArc.OnSeekBarChangeListener
     */
    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
        setProgress(mProgress);
    }

    public void setProgress(int progress) {
        updateProgress(progress, false);
    }

    public int getProgress() {
        return mProgress;
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public int getArcRotation() {
        return mRotation;
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateThumbPosition();
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateThumbPosition();
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        updateThumbPosition();
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = mArcRadius
                    - Math.min(thumbHalfWidth, thumbHalfheight);
        }
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public boolean isClockwise() {
        return mClockwise;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public void setLeapEnabled(boolean leapEnabled) {
        isLeapEnabled = leapEnabled;
    }

    public void setShowClockface(boolean show, boolean outside) {
        this.mShowClockface = show;
        mClockfaceSide = outside ? 1 : -1;
    }

    public int getProgressColor() {
        return mProgressPaint.getColor();
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return mArcPaint.getColor();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidate();
    }

    public float getArcRadius() {
        return mArcRadius;
    }

    public int getMax() {
        return mProgressMax;
    }

    public void setMax(int mMax) {
        this.mProgressMax = mMax;
        mLeapProgressValue = 0.5f * mMax;
    }
}

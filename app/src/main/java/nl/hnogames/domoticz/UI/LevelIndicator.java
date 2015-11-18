package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

@SuppressWarnings("unused")
public class LevelIndicator extends SeekBar {

    private int duration = 1000;
    private int min = 5;

    public LevelIndicator(Context context) {
        super(context);
        disableTouch();
    }

    public LevelIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        disableTouch();
    }

    public LevelIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        disableTouch();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void disableTouch() {
        View.OnTouchListener listener = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        };
        this.setOnTouchListener(listener);
    }

    @Override
    public void setProgress(int progress) {
        ProgressBarAnimation anim = new ProgressBarAnimation(this, min, progress);
        this.startAnimation(anim);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWidth(int width) {
        float[] outR = new float[]{6, 6, 6, 6, 6, 6, 6, 6};
        ShapeDrawable thumb = new ShapeDrawable(new RoundRectShape(outR, null, null));
        thumb.setIntrinsicWidth(dpToPx(width));
        super.setThumb(thumb);
    }
}
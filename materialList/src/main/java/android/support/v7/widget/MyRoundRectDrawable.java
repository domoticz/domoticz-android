package android.support.v7.widget;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class MyRoundRectDrawable extends RoundRectDrawable {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyRoundRectDrawable(ColorStateList backgroundColor, float radius) {
        super(backgroundColor, radius);
    }
}
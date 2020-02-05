/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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

    @SuppressWarnings("FieldCanBeLocal")
    private int duration = 1000;
    @SuppressWarnings("FieldCanBeLocal")
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
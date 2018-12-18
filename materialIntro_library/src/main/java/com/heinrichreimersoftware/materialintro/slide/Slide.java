package com.heinrichreimersoftware.materialintro.slide;

import androidx.annotation.ColorRes;
import androidx.fragment.app.Fragment;

public interface Slide {
    Fragment getFragment();

    @ColorRes
    int getBackground();

    @ColorRes
    int getBackgroundDark();

    boolean canGoForward();

    boolean canGoBackward();
}

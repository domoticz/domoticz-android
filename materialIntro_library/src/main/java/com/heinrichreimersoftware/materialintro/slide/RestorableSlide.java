package com.heinrichreimersoftware.materialintro.slide;

import androidx.fragment.app.Fragment;

public interface RestorableSlide extends Slide {
    void setFragment(Fragment fragment);
}

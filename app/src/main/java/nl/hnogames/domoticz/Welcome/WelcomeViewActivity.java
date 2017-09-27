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

package nl.hnogames.domoticz.Welcome;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class WelcomeViewActivity extends IntroActivity {

    private static final int WELCOME_WIZARD = 1;
    private int p = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        setFullscreen(true);
        super.onCreate(savedInstanceState);

        setFinishEnabled(false);
        setSkipEnabled(false);
        UsefulBits.checkAPK(this, new SharedPrefUtil(this));

        addSlide(new SimpleSlide.Builder()
                .image(R.mipmap.ic_launcher)
                .title(R.string.app_name_domoticz)
                .description(R.string.welcome_info_domoticz)
                .background(R.color.black)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.welcome2_background)
                .fragment(WelcomePage2.newInstance())
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(!mSharedPrefs.darkThemeEnabled() ? R.color.welcome4_background : R.color.primary_dark)
                .fragment(WelcomePage3.newInstance(WELCOME_WIZARD))
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(!mSharedPrefs.darkThemeEnabled() ? R.color.welcome4_background : R.color.primary_dark)
                .fragment(WelcomePage4.newInstance())
                .build());

        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                p = position;
                if (position == 3) {
                    setFinishEnabled(true);
                } else {
                    setFinishEnabled(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (p > 0) {
            previousSlide();
            disableFinishButton(false);
        } else {
            finishWithResult(false);
        }
    }

    public void finishWithResult(boolean success) {
        Bundle conData = new Bundle();
        conData.putBoolean("RESULT", success);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    public void disableFinishButton(boolean disable) {
        setFinishEnabled(!disable);
    }

    private void endWelcomeWizard() {
        finishWithResult(true);
    }
}

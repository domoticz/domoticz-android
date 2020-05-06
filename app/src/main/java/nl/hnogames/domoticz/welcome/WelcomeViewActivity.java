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

package nl.hnogames.domoticz.welcome;


import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class WelcomeViewActivity extends AppIntro2 {
    private static final int WELCOME_WIZARD = 1;
    private int p = 0;
    private SharedPrefUtil mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        skipButtonEnabled = false;
        UsefulBits.checkAPK(this, new SharedPrefUtil(this));

        SliderPage sliderPage = new SliderPage();
        sliderPage.setTitle(getString(R.string.wizard_welcome));
        sliderPage.setDescription(getString(R.string.welcome_info_domoticz));
        sliderPage.setImageDrawable(R.drawable.domoticz);
        sliderPage.setBgColor(R.color.black);

        addSlide(AppIntroFragment.newInstance(sliderPage));
        addSlide(WelcomePage2.newInstance());
        addSlide(WelcomePage3.newInstance(WELCOME_WIZARD));
        addSlide(WelcomePage4.newInstance());
    }

    public void setDemoAccount() {
        ServerUtil mServerUtil = new ServerUtil(this);
        mServerUtil.getActiveServer().setServerName("Demo");
        mServerUtil.getActiveServer().setRemoteServerUsername("admin");
        mServerUtil.getActiveServer().setRemoteServerPassword("D@m@t1czCl0ud");
        mServerUtil.getActiveServer().setRemoteServerUrl("gandalf.domoticz.com");
        mServerUtil.getActiveServer().setRemoteServerPort("1883");
        mServerUtil.getActiveServer().setRemoteServerDirectory("");
        mServerUtil.getActiveServer().setRemoteServerSecure(true);
        mServerUtil.getActiveServer().setLocalSameAddressAsRemote();
        mServerUtil.getActiveServer().setIsLocalServerAddressDifferent(false);
        mServerUtil.saveDomoticzServers(true);
        mSharedPrefs.setWelcomeWizardSuccess(true);
        endWelcomeWizard();
    }

    @Override
    public void onBackPressed() {
        if (p > 0) {
            setProgressButtonEnabled(false);
        } else {
            finishWithResult(false);
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finishWithResult(true);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (newFragment instanceof WelcomePage4) {
            setProgressButtonEnabled(false);
        } else {
            setProgressButtonEnabled(true);
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
        setProgressButtonEnabled(!disable);
    }

    private void endWelcomeWizard() {
        finishWithResult(true);
    }
}

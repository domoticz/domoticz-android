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

package nl.hnogames.domoticz.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingViewModel viewModel;
    private NavController navController;
    private SharedPrefUtil sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = new SharedPrefUtil(this);

        // Apply language preference
        if (!UsefulBits.isEmpty(sharedPrefs.getDisplayLanguage())) {
            UsefulBits.setDisplayLanguage(this, sharedPrefs.getDisplayLanguage());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    public OnboardingViewModel getViewModel() {
        return viewModel;
    }

    public void finishOnboarding(boolean success) {
        Bundle resultData = new Bundle();
        resultData.putBoolean("RESULT", success);
        Intent intent = new Intent();
        intent.putExtras(resultData);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (navController != null && !navController.popBackStack()) {
            // If we can't go back in navigation, finish with no result
            finishOnboarding(false);
        }
    }
}


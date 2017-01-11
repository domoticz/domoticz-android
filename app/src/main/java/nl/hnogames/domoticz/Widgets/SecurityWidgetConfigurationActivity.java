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

package nl.hnogames.domoticz.Widgets;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.WidgetsAdapter;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class SecurityWidgetConfigurationActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final int iWelcomeResultCode = 885;
    int mAppWidgetId;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private WidgetsAdapter adapter;
    private SearchView searchViewAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_security_configuration);
        setResult(RESULT_CANCELED);

        if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
            Toast.makeText(this, getString(R.string.wizard_widgets) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
            this.finish();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        //1) Is domoticz connected?
        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            //2) Show list of switches to choose from
            initListViews();
        }
    }

    /* Called when the second activity's finished */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        initListViews();
                    }
                    break;
            }
        }
    }

    public void initListViews() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            if (domoticz == null)
                domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
            domoticz.getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(final ArrayList<DevicesInfo> mDevicesInfo) {
                    if (mDevicesInfo == null)
                        SecurityWidgetConfigurationActivity.this.finish();
                    for (DevicesInfo d : mDevicesInfo) {
                        if (!UsefulBits.isEmpty(d.getSwitchType()) &&
                                d.getSwitchType().equals(DomoticzValues.Device.Type.Name.SECURITY)) {
                            if (d.getSubType().equals(DomoticzValues.Device.SubType.Name.SECURITYPANEL)) {
                                showAppWidget(d, getApplicationContext().getString(R.string.status) + ": " +
                                        String.valueOf(d.getData()));
                            }
                        }
                    }
                }

                @Override
                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SecurityWidgetConfigurationActivity.this, R.string.failed_to_get_switches, Toast.LENGTH_SHORT).show();
                    SecurityWidgetConfigurationActivity.this.finish();
                }
            }, 0, null);
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void showAppWidget(DevicesInfo mSelectedSwitch, String value) {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int idx = mSelectedSwitch.getIdx();
        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            mSharedPrefs.setSecurityWidgetIDX(mAppWidgetId, idx, value);
            Intent startService = new Intent(SecurityWidgetConfigurationActivity.this,
                    SecurityWidgetProvider.UpdateSecurityWidgetService.class);
            startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService.setAction("FROM CONFIGURATION ACTIVITY");
            startService(startService);
            setResult(RESULT_OK, startService);
            finish();
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            Log.i(TAG, "I am invalid");
            finish();
        }
    }

}
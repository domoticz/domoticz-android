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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import java.util.ArrayList;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SettingsInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SettingsReceiver;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class SecurityWidgetConfigurationActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final int iWelcomeResultCode = 885;
    int mAppWidgetId;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;

    private TextView txtTitle;
    private TextView txtStatus;
    private Button btnConfig;
    private FloatingLabelEditText editPin;

    private SettingsInfo mSettings;
    private DevicesInfo sSecurityPanel;

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

        if (domoticz == null)
            domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());

        txtStatus = (TextView) this.findViewById(R.id.status);
        txtTitle = (TextView) this.findViewById(R.id.title);
        btnConfig = (Button) this.findViewById(R.id.checkpin);

        editPin = (FloatingLabelEditText) this.findViewById(R.id.securitypin);
        editPin.getInputWidget().setTransformationMethod(PasswordTransformationMethod.getInstance());

        if (mSharedPrefs.darkThemeEnabled()) {
            btnConfig.setBackground(ContextCompat.getDrawable(this, R.drawable.button_status_dark));
            editPin.setInputWidgetTextColor(ContextCompat.getColor(this, R.color.white));
            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
            txtTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm =
                        (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editPin.getWindowToken(), 0);
                final String password =
                        UsefulBits.getMd5String(editPin.getInputWidgetText().toString());
                if (UsefulBits.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.security_wrong_code), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mSettings == null) {
                    domoticz.getSettings(new SettingsReceiver() {
                        @Override
                        public void onReceiveSettings(SettingsInfo settings) {
                            mSettings = settings;
                            if (validatePassword(password)) {
                                if (sSecurityPanel != null) {
                                    getBackground(sSecurityPanel, password, getApplicationContext().getString(R.string.status) + ": " +
                                            String.valueOf(sSecurityPanel.getData()));
                                }
                            } else
                                Toast.makeText(getApplicationContext(), getString(R.string.security_wrong_code), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, domoticz.getErrorMessage(error));
                        }
                    });
                } else {
                    if (validatePassword(password)) {
                        if (sSecurityPanel != null) {
                            getBackground(sSecurityPanel, password, getApplicationContext().getString(R.string.status) + ": " +
                                    String.valueOf(sSecurityPanel.getData()));
                        }
                    } else
                        Toast.makeText(getApplicationContext(), getString(R.string.security_wrong_code), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
            Toast.makeText(this, getString(R.string.wizard_widgets) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
            this.finish();
        }

        if (!mSharedPrefs.IsWidgetsEnabled()) {
            Toast.makeText(this, getString(R.string.wizard_widgets) + " " + getString(R.string.widget_disabled), Toast.LENGTH_LONG).show();
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
            initListViews();

        }
    }

    private void getBackground(final DevicesInfo mSelectedSwitch, final String password, final String value) {
        new MaterialDialog.Builder(this)
                .title(this.getString(R.string.widget_background))
                .items(new String[]{this.getString(R.string.widget_dark), this.getString(R.string.widget_light), this.getString(R.string.widget_transparent_dark), this.getString(R.string.widget_transparent_light)})
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        showAppWidget(mSelectedSwitch, value, password, getWidgetLayout(String.valueOf(text)));
                        return true;
                    }
                })
                .positiveText(R.string.ok)
                .show();
    }

    private int getWidgetLayout(String background) {
        int layout = R.layout.widget_layout;
        String backgroundWidget = String.valueOf(background);

        if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_dark))) {
            layout = R.layout.widget_security_layout_dark;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_light))) {
            layout = R.layout.widget_security_layout;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_light))) {
            layout = R.layout.widget_security_layout_transparent;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_dark))) {
            layout = R.layout.widget_security_layout_transparent_dark;
        }

        return layout;
    }

    public boolean validatePassword(String password) {
        if (mSettings != null)
            return password.equals(mSettings.getSecPassword());
        else
            return false;
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
            domoticz.getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(final ArrayList<DevicesInfo> mDevicesInfo) {
                    if (mDevicesInfo == null)
                        SecurityWidgetConfigurationActivity.this.finish();

                    boolean deviceFound = false;
                    for (DevicesInfo d : mDevicesInfo) {
                        if (!UsefulBits.isEmpty(d.getSwitchType()) &&
                                d.getSwitchType().equals(DomoticzValues.Device.Type.Name.SECURITY)) {
                            if (d.getSubType().equals(DomoticzValues.Device.SubType.Name.SECURITYPANEL)) {
                                sSecurityPanel = d;
                                txtTitle.setText(sSecurityPanel.getName());
                                txtStatus.setText(getApplicationContext().getString(R.string.status) + ": " +
                                        String.valueOf(sSecurityPanel.getData()));
                                deviceFound = true;
                            }
                        }
                    }

                    if (!deviceFound) {
                        Toast.makeText(SecurityWidgetConfigurationActivity.this, R.string.failed_to_get_securitypanel, Toast.LENGTH_LONG).show();
                        SecurityWidgetConfigurationActivity.this.finish();
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

    private void showAppWidget(DevicesInfo mSelectedSwitch, String value, String pin, int layout) {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int idx = mSelectedSwitch.getIdx();

        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            mSharedPrefs.setSecurityWidgetIDX(mAppWidgetId, idx, value, pin, layout);
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
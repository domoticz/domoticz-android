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

package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SettingsInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.SettingsReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class SecurityPanelDialog implements DialogInterface.OnDismissListener {

    private static final String TAG = SecurityPanelDialog.class.getSimpleName();

    private final MaterialDialog.Builder mdb;
    private final Context mContext;
    private final SharedPrefUtil mSharedPrefs;
    private final DevicesInfo panelInfo;
    private final Domoticz domoticz;
    private DismissListener dismissListener;
    private SettingsInfo mSettings;
    private MaterialDialog md;
    private androidx.appcompat.widget.AppCompatEditText editPinCode;
    private TextView txtCountDown;
    private Button btnDisarm;
    private Button btnArmHome;
    private Button btnArmAway;
    private CountDownTimer countDownTimer;

    public SecurityPanelDialog(Context c, Domoticz mDomoticz, DevicesInfo panelInfo) {
        this.mContext = c;
        this.domoticz = mDomoticz;
        this.panelInfo = panelInfo;
        mSharedPrefs = new SharedPrefUtil(c);
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_security, true)
                .negativeText(android.R.string.cancel);

        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    public void show() {
        mdb.title(panelInfo.getName());
        md = mdb.build();
        View view = md.getCustomView();

        if (view != null) {
            editPinCode = view.findViewById(R.id.securitypin);
            btnDisarm = view.findViewById(R.id.disarm);
            btnArmHome = view.findViewById(R.id.armhome);
            btnArmAway = view.findViewById(R.id.armaway);
            txtCountDown = view.findViewById(R.id.countdown);
        }
        domoticz.getSettings(new SettingsReceiver() {
            @Override
            public void onReceiveSettings(SettingsInfo settings) {
                mSettings = settings;
                md.show();
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, domoticz.getErrorMessage(error));
                md.dismiss();
            }
        });

        btnDisarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processRequest(DomoticzValues.Security.Status.DISARM);
            }
        });
        btnArmAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processRequest(DomoticzValues.Security.Status.ARMAWAY);
            }
        });
        btnArmHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processRequest(DomoticzValues.Security.Status.ARMHOME);
            }
        });
    }

    private void setFields(boolean enabled) {
        btnDisarm.setEnabled(enabled);
        btnArmAway.setEnabled(enabled);
        btnArmHome.setEnabled(enabled);
        editPinCode.setEnabled(enabled);
    }

    private void processRequest(final int status) {
        setFields(false);
        InputMethodManager imm =
                (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editPinCode.getWindowToken(), 0);

        final String password =
                UsefulBits.getMd5String(editPinCode.getText().toString());

        if (validatePassword(password)) {
            if (mSettings.getSecOnDelay() <= 0 || status == DomoticzValues.Security.Status.DISARM) {
                //don't set delay
                domoticz.setSecurityPanelAction(status, password, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        dismissListener.onDismiss();
                        md.dismiss();
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, domoticz.getErrorMessage(error));
                        Toast.makeText(mContext,
                                mContext.getString(R.string.security_generic_error),
                                Toast.LENGTH_SHORT).show();
                        setFields(true);
                    }
                });
            } else {
                countDownTimer = new CountDownTimer((mSettings.getSecOnDelay() * 1000L), 1000) {
                    public void onTick(long millisUntilFinished) {
                        txtCountDown.setText(String.valueOf((millisUntilFinished / 1000)));
                    }

                    public void onFinish() {
                        txtCountDown.setText("");
                        domoticz.setSecurityPanelAction(status, password, new setCommandReceiver() {
                            @Override
                            public void onReceiveResult(String result) {
                                dismissListener.onDismiss();
                                md.dismiss();
                            }

                            @Override
                            public void onError(Exception error) {
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.security_generic_error),
                                        Toast.LENGTH_SHORT).show();
                                setFields(true);
                            }
                        });
                    }
                }.start();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
            setFields(true);
        }
    }

    public boolean validatePassword(String password) {
        return password.equals(mSettings.getSecPassword());
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss();
    }
}

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
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Domoticz;

public class PasswordDialog implements DialogInterface.OnDismissListener {

    private static final String TAG = PasswordDialog.class.getSimpleName();

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private Domoticz domoticz;
    private MaterialDialog md;
    private SharedPrefUtil mSharedPrefs;
    private FloatingLabelEditText editPassword;
    private CheckBox showPassword;

    public PasswordDialog(Context c, Domoticz mDomoticz) {
        this.mContext = c;
        this.domoticz = mDomoticz;
        mSharedPrefs = new SharedPrefUtil(c);
        if ((new SharedPrefUtil(mContext)).darkThemeEnabled()) {
            mdb = new MaterialDialog.Builder(mContext)
                    .titleColorRes(R.color.white)
                    .contentColor(Color.WHITE) // notice no 'res' postfix for literal color
                    .dividerColorRes(R.color.white)
                    .backgroundColorRes(R.color.primary)
                    .positiveColorRes(R.color.white)
                    .neutralColorRes(R.color.white)
                    .negativeColorRes(R.color.white)
                    .widgetColorRes(R.color.white)
                    .buttonRippleColorRes(R.color.white);
        } else
            mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_password, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .theme(mSharedPrefs.darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        if (dismissListener != null)
                            dismissListener.onDismiss(editPassword.getInputWidgetText().toString());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        if (dismissListener != null)
                            dismissListener.onCancel();
                    }
                });
        mdb.dismissListener(this);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.welcome_remote_server_password));
        md = mdb.build();
        View view = md.getCustomView();

        editPassword = (FloatingLabelEditText) view.findViewById(R.id.password);
        showPassword = (CheckBox) view.findViewById(R.id.showpassword);

        if (mSharedPrefs.darkThemeEnabled()) {
            showPassword.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            editPassword.setInputWidgetTextColor(ContextCompat.getColor(mContext, R.color.white));
            int[][] states = new int[][]{new int[]{android.R.attr.state_activated}, new int[]{-android.R.attr.state_activated}};
            int[] colors = new int[]{Color.WHITE, Color.WHITE};
            editPassword.setLabelTextColor(new ColorStateList(states, colors));
        }

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // show password
                    editPassword.getInputWidget().setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    editPassword.getInputWidget().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(String password);

        void onCancel();
    }
}

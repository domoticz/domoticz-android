/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;

public class PasswordDialog implements DialogInterface.OnDismissListener {

    private static final String TAG = PasswordDialog.class.getSimpleName();

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private Domoticz domoticz;
    private MaterialDialog md;
    private FloatingLabelEditText editPasscode;

    public PasswordDialog(Context c) {
        this.mContext = c;
        this.domoticz = new Domoticz(mContext);
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_password, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        if (dismissListener != null)
                            dismissListener.onDismiss(editPasscode.getInputWidgetText().toString());
                    }
                });
        mdb.dismissListener(this);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.welcome_remote_server_password));
        md = mdb.build();
        View view = md.getCustomView();
        editPasscode = (FloatingLabelEditText) view.findViewById(R.id.password);
        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {}

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(String password);
    }
}

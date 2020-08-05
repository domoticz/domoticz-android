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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.NotificationTypeInfo;

public class SendNotificationDialog {
    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private List<NotificationTypeInfo> info;
    private Context context;
    private AppCompatEditText subject, message;
    private MultiSelectionSpinner systems;

    public SendNotificationDialog(Context mContext,
                                  List<NotificationTypeInfo> info) {
        this.info = info;
        this.context = mContext;
        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;

        //noinspection ConstantConditions
        mdb.customView(R.layout.dialog_send_notification, wrapInScrollView)
                .positiveText(R.string.send)
                .autoDismiss(false)
                .negativeText(android.R.string.cancel);

        mdb.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                List<String> selectedSystems = systems.getSelectedStrings();
                if (UsefulBits.isEmpty(subject.getText()))
                    Toast.makeText(context, R.string.notification_error_subject, Toast.LENGTH_LONG).show();
                else if (UsefulBits.isEmpty(message.getText()))
                    Toast.makeText(context, R.string.notification_error_message, Toast.LENGTH_LONG).show();
                else if (selectedSystems == null || selectedSystems.size() <= 0)
                    Toast.makeText(context, R.string.notification_error_system, Toast.LENGTH_LONG).show();
                else {
                    String systems = "";
                    for (String s : selectedSystems) {
                        if (UsefulBits.isEmpty(systems))
                            systems = s;
                        else systems += ";" + s;
                    }
                    NotificationInfo notification = new NotificationInfo(-1, String.valueOf(subject.getText()), String.valueOf(message.getText()), 0, new Date(), true, DeviceUtils.getUniqueID(context));
                    notification.setSystems(systems);
                    dismissListener.OnSend(notification);
                    dialog.dismiss();
                }
            }
        });
        mdb.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
    }

    public void show() {
        mdb.title(context.getString(R.string.send_notification));
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        subject = view.findViewById(R.id.notification_subject);
        message = view.findViewById(R.id.notification_message);
        systems = view.findViewById(R.id.notification_systems);

        List<String> subSystems = new ArrayList<>();
        subSystems.add("fcm"); // default value for mobile
        for (NotificationTypeInfo t : info)
            if (!t.equals("fcm"))
                subSystems.add(t.getName());
        systems.setItems(subSystems);
        systems.setSelection(0);
        md.show();
    }

    public void onDismissListener(SendNotificationDialog.DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void OnSend(NotificationInfo message);
    }
}
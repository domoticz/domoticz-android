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
import android.graphics.Color;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.NotificationsAdapter;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.NotificationInfo;

public class NotificationInfoDialog {

    private final MaterialDialog.Builder mdb;
    private ArrayList<NotificationInfo> info;
    private Context mContext;

    public NotificationInfoDialog(Context c,
                                  ArrayList<NotificationInfo> _info) {
        this.info = _info;
        this.mContext = c;

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
        mdb.customView(R.layout.dialog_switch_timer, true)
                .theme((new SharedPrefUtil(mContext)).darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(R.string.category_notification);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);
        NotificationsAdapter adapter = new NotificationsAdapter(mContext, info);
        listView.setAdapter(adapter);

        md.show();
    }
}

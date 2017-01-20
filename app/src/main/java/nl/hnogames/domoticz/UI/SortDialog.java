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
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class SortDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private String[] names;

    public SortDialog(Context c,
                      int layout) {
        this.mContext = c;

        names = new String[]{mContext.getString(R.string.filterOn_on), mContext.getString(R.string.filterOn_off), mContext.getString(R.string.filterOn_static), mContext.getString(R.string.filterOn_all)};
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
        mdb.customView(layout, true)
                .negativeText(android.R.string.cancel);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mdb.title(R.string.filter_devices);
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, names);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dismissListener != null)
                    dismissListener.onDismiss(names[position]);
                md.dismiss();
            }
        });

        listView.setAdapter(adapter);
        md.show();
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(String selectedSort);
    }
}

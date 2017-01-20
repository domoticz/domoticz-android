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
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class TextDialog implements
        DialogInterface.OnDismissListener,
        DialogInterface.OnCancelListener,
        MaterialDialog.SingleButtonCallback {

    private final MaterialDialog.Builder mdb;
    private Context context;
    private DismissListener dismissListener;
    private NegativeListener negativeListener;
    private PositiveListener positiveListener;
    private String negativeText;
    private String positiveText;
    private String titleText;
    private String text;

    public TextDialog(Context context) {

        this.context = context;

        if ((new SharedPrefUtil(context)).darkThemeEnabled()) {
            mdb = new MaterialDialog.Builder(context)
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
            mdb = new MaterialDialog.Builder(context);
        mdb.customView(R.layout.dialog_text, true).negativeText(android.R.string.cancel)
                .theme((new SharedPrefUtil(context)).darkThemeEnabled() ? Theme.DARK : Theme.LIGHT);
        mdb.dismissListener(this);
        mdb.cancelListener(this);
        mdb.onPositive(this);
    }

    public void setTitle(String titleText) {
        this.titleText = titleText;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setText(int resText) {
        this.text = context.getResources().getString(resText);
    }

    public void setNegativeText(String negativeText) {
        this.negativeText = negativeText;
    }

    public void setNegativeText(int resNegativeText) {
        this.negativeText = context.getResources().getString(resNegativeText);
    }

    public void setPositiveText(String positiveText) {
        this.positiveText = positiveText;
    }

    public void setPositiveText(int resPositiveText) {
        this.positiveText = context.getResources().getString(resPositiveText);
    }

    public void show() {
        mdb.title(titleText);
        if (!negativeText.isEmpty()) mdb.negativeText(negativeText);
        if (!positiveText.isEmpty()) mdb.positiveText(positiveText);

        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView dialogText = (TextView) view.findViewById(R.id.textDialog_text);
        dialogText.setText(text);

        md.show();
    }


    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void onPositiveListener(PositiveListener positiveListener) {
        this.positiveListener = positiveListener;
    }

    public void onNegativeListener(NegativeListener negativeListener) {
        this.negativeListener = negativeListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dismissListener.onDismiss();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        negativeListener.onNegative();
    }

    @Override
    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
        if (dialogAction == DialogAction.POSITIVE) positiveListener.onPositive();
        if (dialogAction == DialogAction.NEGATIVE) negativeListener.onNegative();

    }

    public interface DismissListener {
        void onDismiss();
    }

    public interface NegativeListener {
        void onNegative();
    }

    public interface PositiveListener {
        void onPositive();
    }

}
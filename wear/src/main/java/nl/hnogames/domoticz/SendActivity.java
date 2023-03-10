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

package nl.hnogames.domoticz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.wear.activity.ConfirmationActivity;
import androidx.wear.widget.CircularProgressLayout;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SendActivity extends DomoticzActivity implements androidx.wear.widget.CircularProgressLayout.OnTimerFinishedListener, View.OnClickListener {

    private androidx.wear.widget.CircularProgressLayout circularProgress;
    private String selectedSwitch = "";

    // Sample dataset for the list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            selectedSwitch = extras.getString("SWITCH", "");

        circularProgress = findViewById(R.id.delayed_confirm);
        circularProgress.setOnTimerFinishedListener(this);
        circularProgress.setOnClickListener(this);
        circularProgress.setTotalTime(3000);
        circularProgress.startTimer();
    }

    @Override
    public void onTimerFinished(CircularProgressLayout layout) {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.triggered));
        startActivity(intent);
        sendMessage(SEND_SWITCH, selectedSwitch);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.equals(circularProgress)) {
            // User canceled, abort the action
            circularProgress.stopTimer();
            this.finish();
        }
    }
}
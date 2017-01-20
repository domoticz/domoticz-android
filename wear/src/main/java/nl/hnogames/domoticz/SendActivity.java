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
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SendActivity extends DomoticzActivity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private DelayedConfirmationView delayedConfirmationView;
    private String selectedSwitch = "";

    // Sample dataset for the list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Bundle extras = getIntent().getExtras();
        selectedSwitch = extras.getString("SWITCH", "");

        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.setImageResource(R.drawable.ic_stop);
        delayedConfirmationView.setTotalTimeMs(3000);
        delayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        //process message
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.triggered));
        startActivity(intent);

        sendMessage(SEND_SWITCH, selectedSwitch);
        this.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        delayedConfirmationView.reset();
        this.finish();
    }
}
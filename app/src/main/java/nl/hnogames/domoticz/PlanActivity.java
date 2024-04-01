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

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.ftinc.scoop.Scoop;

import java.util.Timer;
import java.util.TimerTask;

import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.fragments.Plan;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;

public class PlanActivity extends AppCompatAssistActivity {
    private Timer autoRefreshTimer = null;
    private Plan dash;
    private SharedPrefUtil mSharedPrefs;
    private Toolbar toolbar;

    public ConfigInfo getConfig() {
        return StaticHelper.getServerUtil(this) != null && StaticHelper.getServerUtil(this).getActiveServer() != null ?
                StaticHelper.getServerUtil(this).getActiveServer().getConfigInfo(this) :
                null;
    }

    private void setupAutoRefresh() {
        if (mSharedPrefs.getAutoRefresh() && autoRefreshTimer == null) {
            autoRefreshTimer = new Timer("autorefresh", true);
            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override

                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dash.refreshFragment();
                        }
                    });
                }
            }, 0, (mSharedPrefs.getAutoRefreshTimer() * 1000L));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //noinspection SpellCheckingInspection
            String selectedPlan = bundle.getString("PLANNAME");
            //noinspection SpellCheckingInspection
            int selectedPlanID = bundle.getInt("PLANID");
            this.setTitle(selectedPlan);

            dash = new Plan();
            dash.selectedPlan(selectedPlanID, selectedPlan);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.main, dash);
            tx.commit();
            setupAutoRefresh();
        } else this.finish();
    }

    private void stopAutoRefreshTimer() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer.purge();
            autoRefreshTimer = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override

    public void onDestroy() {
        stopAutoRefreshTimer();
        super.onDestroy();
    }

    @Override

    public void onPause() {
        stopAutoRefreshTimer();
        super.onPause();
    }

    @Override

    public void onBackPressed() {
        stopAutoRefreshTimer();
        this.finish();
    }

    @Override

    public void onResume() {
        super.onResume();
        setupAutoRefresh();
    }
}
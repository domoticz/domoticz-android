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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.ftinc.scoop.Scoop;

import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.fragments.Logs;
import nl.hnogames.domoticz.ui.SortDialog;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class LogsActivity extends AppCompatPermissionsActivity {
    private Logs fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getString(R.string.title_logs));

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new Logs();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logs_sort, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    SortDialog infoDialog = new SortDialog(
                            this,
                            R.layout.dialog_switch_logs,
                            new String[]{getString(R.string.filter_all), getString(R.string.filter_normal), getString(R.string.filter_status), getString(R.string.filter_error)});

                    infoDialog.onDismissListener(selectedSort -> {
                        Log.i("Logs", "Sorting: " + selectedSort);
                        fragment.sortFragment(selectedSort);
                    });

                    infoDialog.show();
                    return true;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return super.onOptionsItemSelected(item);
    }
}
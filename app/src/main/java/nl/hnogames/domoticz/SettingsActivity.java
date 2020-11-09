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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.ftinc.scoop.Scoop;

import androidx.appcompat.widget.Toolbar;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.preference.PreferenceFragment;
import nl.hnogames.domoticz.ui.ScoopSettingsActivity;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class SettingsActivity extends AppCompatPermissionsActivity {
    private final int THEME_CHANGED = 55;
    private final int EXPORT_SETTINGS = 66;
    private final int IMPORT_SETTINGS = 66;
    private Toolbar toolbar;
    private PreferenceFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishWithResult();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }

    public void reloadSettings() {
        recreate();
    }

    public void openThemePicker() {
        startActivityForResult(ScoopSettingsActivity.createIntent(this, getString(R.string.config_theme)), THEME_CHANGED);
    }

    public void exportSettings() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("plain/txt");
        intent.putExtra(Intent.EXTRA_TITLE, "settings.txt");
        startActivityForResult(intent, EXPORT_SETTINGS);
    }

    public void importSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "settings.txt");
        startActivityForResult(intent, IMPORT_SETTINGS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == THEME_CHANGED) {
            recreate();
        }
        if (requestCode == EXPORT_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        if ((new SharedPrefUtil(this)).saveSharedPreferencesToFile(data.getData()))
                            fragment.showSnackbar(getString(R.string.settings_exported));
                        else
                            fragment.showSnackbar(getString(R.string.settings_export_failed));
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
        if (requestCode == IMPORT_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        if ((new SharedPrefUtil(this)).loadSharedPreferencesFromFile(data.getData()))
                            fragment.showSnackbar(getString(R.string.settings_imported));
                        else
                            fragment.showSnackbar(getString(R.string.settings_import_failed));
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private void finishWithResult() {
        Bundle conData = new Bundle();
        conData.putBoolean("RESULT", true);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
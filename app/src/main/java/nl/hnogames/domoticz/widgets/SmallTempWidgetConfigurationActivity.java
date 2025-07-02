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

package nl.hnogames.domoticz.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.adapters.TemperatureWidgetAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.widgets.database.WidgetContract;
import nl.hnogames.domoticz.widgets.database.WidgetDbHelper;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.Interfaces.TemperatureReceiver;

public class SmallTempWidgetConfigurationActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final int iWelcomeResultCode = 885;
    public CoordinatorLayout coordinatorLayout;
    int mAppWidgetId;
    private SharedPrefUtil mSharedPrefs;
    private TemperatureWidgetAdapter adapter;
    private SearchView searchViewAction;
    private Toolbar toolbar;
    private WidgetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);

        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configuration);
        setResult(RESULT_CANCELED);
        mDbHelper = new WidgetDbHelper(this);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getString(R.string.pick_device_title));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        //1) Is domoticz connected?
        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            //2) Show list of switches to choose from
            initListViews();
        }
    }

    /* Called when the second activity's finished */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        initListViews();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initListViews() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            Log.i(TAG, "Showing switches for widget");
            StaticHelper.getDomoticz(SmallTempWidgetConfigurationActivity.this).getTemperatures(new TemperatureReceiver() {
                @Override
                public void onReceiveTemperatures(ArrayList<TemperatureInfo> mDevicesInfo) {
                    ArrayList<TemperatureInfo> mNewDevicesInfo = new ArrayList<TemperatureInfo>();
                    for (TemperatureInfo d : mDevicesInfo)
                        mNewDevicesInfo.add(d);

                    ListView listView = findViewById(R.id.list);
                    adapter = new TemperatureWidgetAdapter(SmallTempWidgetConfigurationActivity.this, StaticHelper.getDomoticz(SmallTempWidgetConfigurationActivity.this), StaticHelper.getDomoticz(SmallTempWidgetConfigurationActivity.this).getServerUtil(), mNewDevicesInfo);
                    listView.setOnItemClickListener((parent, view, position, id) -> {

                        if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                            UsefulBits.showSnackbarWithAction(SmallTempWidgetConfigurationActivity.this, coordinatorLayout, getString(R.string.wizard_widgets) + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG, null,
                                    v -> UsefulBits.openPremiumAppStore(SmallTempWidgetConfigurationActivity.this, IsPremiumEnabled -> recreate()), getString(R.string.upgrade));
                            return;
                        }

                        final TemperatureInfo mDeviceInfo = (TemperatureInfo) adapter.getItem(position);
                        if (mDeviceInfo.isProtected()) {
                            PasswordDialog passwordDialog = new PasswordDialog(
                                    SmallTempWidgetConfigurationActivity.this, StaticHelper.getDomoticz(SmallTempWidgetConfigurationActivity.this));
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    showThemeSelectionDialog(mDeviceInfo, password);
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        } else {
                            showThemeSelectionDialog(mDeviceInfo, null);
                        }
                    });
                    listView.setAdapter(adapter);
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(SmallTempWidgetConfigurationActivity.this, R.string.failed_to_get_switches, Toast.LENGTH_SHORT).show();
                    SmallTempWidgetConfigurationActivity.this.finish();
                }
            });
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private int getWidgetLayout(String background) {
        int layout = R.layout.widget_small_layout;
        String backgroundWidget = String.valueOf(background);
        if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_dark))) {
            layout = R.layout.widget_small_temp_layout_dark;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_light))) {
            layout = R.layout.widget_small_temp_layout;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_light))) {
            layout = R.layout.widget_small_temp_layout_transparent;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_dark))) {
            layout = R.layout.widget_small_temp_layout_transparent_dark;
        }
        return layout;
    }

    private void showThemeSelectionDialog(final TemperatureInfo mDeviceInfo, final String password) {
        new MaterialDialog.Builder(this)
                .title(this.getString(R.string.widget_background))
                .items(new String[]{
                        this.getString(R.string.widget_dark),
                        this.getString(R.string.widget_light),
                        this.getString(R.string.widget_transparent_dark),
                        this.getString(R.string.widget_transparent_light)})
                .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
                    saveWidgetConfiguration(mDeviceInfo, password, getWidgetLayout(String.valueOf(text)));
                    return true;
                })
                .positiveText(R.string.ok)
                .show();
    }

    private void saveWidgetConfiguration(TemperatureInfo mDeviceInfo, String password, int layoutId) {
        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int idx = mDeviceInfo.getIdx();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            ContentValues values = new ContentValues();
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX, idx);
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE, false);
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD, password);
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID, layoutId);

            mDbHelper.saveWidgetConfiguration(mAppWidgetId, values);

            Intent startService = new Intent(SmallTempWidgetConfigurationActivity.this, WidgetProviderSmallTemp.UpdateWidgetService.class);
            startService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService.setAction("FROM CONFIGURATION ACTIVITY");
            startService(startService);

            setResult(RESULT_OK, startService);
            finish();
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.i(TAG, "Invalid AppWidget ID");
            finish();
        }
    }

    public void Filter(String text) {
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchViewAction = (SearchView) MenuItemCompat
                .getActionView(searchMenuItem);
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
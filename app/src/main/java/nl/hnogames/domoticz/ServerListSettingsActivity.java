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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import nl.hnogames.domoticz.adapters.ServerAdapter;
import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.ServerClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ServerInfo;


public class ServerListSettingsActivity extends AppCompatAssistActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_ADD_SERVER = 54;
    private final int REQUEST_EDIT_SERVER = 55;

    @SuppressWarnings("unused")
    private final String TAG = ServerListSettingsActivity.class.getSimpleName();
    private CoordinatorLayout coordinatorLayout;
    private ServerAdapter adapter;
    private ArrayList<ServerInfo> mServerList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);
        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.server_settings);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        createListView();
    }

    private void createListView() {
        mServerList = StaticHelper.getServerUtil(getApplicationContext()).getServerList();
        adapter = new ServerAdapter(this, mServerList, new ServerClickListener() {
            @Override
            public boolean onEnableClick(ServerInfo server, boolean checked) {
                server.setEnabled(checked);
                StaticHelper.getServerUtil(getApplicationContext()).updateServerInfo(server);
                return false;
            }

            @Override
            public void onRemoveClick(ServerInfo server) {
                showRemoveSnackbar(server);
            }
        });

        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener((adapterView, view, item, id) -> {
            String servername = String.valueOf(((TextView) view.findViewById(R.id.server_name)).getText());
            boolean active = false;
            for (ServerInfo s : mServerList) {
                if (s.getServerName().equals(servername)) {
                    if (StaticHelper.getServerUtil(getApplicationContext()).getActiveServer().getServerName().equals(servername)) {
                        active = true;
                        break;
                    }
                }
            }

            showEditServerActivity(servername, active);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            createListView(); //reload total list
        }
    }

    private void showRemoveSnackbar(final ServerInfo serverInfo) {
        if (mServerList.size() > 1) {
            removeServerFromListView(serverInfo);
            removeServerFromSettings(serverInfo);
            UsefulBits.showSnackbar(this, coordinatorLayout, String.format(getString(R.string.something_deleted), getString(R.string.server)), Snackbar.LENGTH_SHORT);
        } else
            UsefulBits.showSnackbar(this, coordinatorLayout, getString(R.string.remove_last_server), Snackbar.LENGTH_SHORT);
    }

    private void removeServerFromListView(ServerInfo serverInfo) {
        adapter.data.remove(serverInfo);
        adapter.notifyDataSetChanged();
    }

    private void removeServerFromSettings(ServerInfo serverInfo) {
        StaticHelper.getServerUtil(getApplicationContext()).removeServer(serverInfo);
    }

    private void addServerToListView(ServerInfo serverInfo) {
        adapter.data.add(serverInfo);
        adapter.notifyDataSetChanged();
    }

    public void showAddServerActivity() {
        Intent i = new Intent(ServerListSettingsActivity.this, ServerSettingsActivity.class);
        //noinspection SpellCheckingInspection
        i.putExtra("ADDSERVER", true);
        startActivityForResult(i, REQUEST_ADD_SERVER);
    }

    public void showEditServerActivity(String name, boolean active) {
        Intent i = new Intent(ServerListSettingsActivity.this, ServerSettingsActivity.class);
        i.putExtra("ADDSERVER", false);
        i.putExtra("SERVERNAME", name);
        i.putExtra("SERVERACTIVE", active);
        startActivityForResult(i, REQUEST_EDIT_SERVER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_server, menu);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add:
                showAddServerActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
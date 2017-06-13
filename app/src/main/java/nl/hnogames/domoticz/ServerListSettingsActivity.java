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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.ServerAdapter;
import nl.hnogames.domoticz.Interfaces.ServerClickListener;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Utils.ServerUtil;


public class ServerListSettingsActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_ADD_SERVER = 54;
    @SuppressWarnings("unused")
    private String TAG = ServerListSettingsActivity.class.getSimpleName();
    private ServerUtil mServerUtil;
    private CoordinatorLayout coordinatorLayout;
    private ServerAdapter adapter;
    private ArrayList<ServerInfo> mServerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.server_settings);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }

        createListView();
    }

    private void createListView() {
        mServerUtil = new ServerUtil(this);
        mServerList = mServerUtil.getServerList();
        adapter = new ServerAdapter(this, mServerList, new ServerClickListener() {
            @Override
            public boolean onEnableClick(ServerInfo server, boolean checked) {
                if (server.getServerName().equals(Domoticz.DOMOTICZ_DEFAULT_SERVER)) {
                    if (server.isEnabled())
                        UsefulBits.showSnackbar(ServerListSettingsActivity.this, coordinatorLayout, R.string.cant_remove_default_server, Snackbar.LENGTH_SHORT);
                    else {
                        server.setEnabled(true);
                        mServerUtil.updateServerInfo(server);
                    }
                    createListView();                           //reset values
                } else {
                    server.setEnabled(checked);
                    mServerUtil.updateServerInfo(server);
                }
                return false;
            }

            @Override
            public void onRemoveClick(ServerInfo server) {
                if (server.getServerName().equals(Domoticz.DOMOTICZ_DEFAULT_SERVER)) {
                    if (server.isEnabled())
                        UsefulBits.showSnackbar(ServerListSettingsActivity.this, coordinatorLayout, R.string.cant_remove_default_server, Snackbar.LENGTH_SHORT);
                    else {
                        server.setEnabled(true);
                        mServerUtil.updateServerInfo(server);
                    }
                } else
                    showRemoveUndoSnackbar(server);
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        if ((new SharedPrefUtil(this)).darkThemeEnabled()) {
            listView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }

        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {
                String servername = String.valueOf(((TextView) view.findViewById(R.id.server_name)).getText());
                boolean active = false;
                for (ServerInfo s : mServerList) {
                    if (s.getServerName().equals(servername)) {
                        if (mServerUtil.getActiveServer().getServerName().equals(servername)) {
                            active = true;
                            break;
                        }
                    }
                }

                showEditServerActivity(servername, active);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            createListView(); //reload total list
        }
    }

    private void showRemoveUndoSnackbar(final ServerInfo serverInfo) {
        // remove location from list view
        removeServerFromListView(serverInfo);

        UsefulBits.showSnackbarWithAction(this, coordinatorLayout, String.format(getString(R.string.something_deleted),
                getString(R.string.server)), Snackbar.LENGTH_SHORT, new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                switch (event) {
                    case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                    case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                    case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                    case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                        removeServerFromSettings(serverInfo);
                        break;
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServerToListView(serverInfo);
            }
        }, this.getString(R.string.undo));

    }

    private void removeServerFromListView(ServerInfo serverInfo) {
        adapter.data.remove(serverInfo);
        adapter.notifyDataSetChanged();
    }

    private void removeServerFromSettings(ServerInfo serverInfo) {
        mServerUtil.removeServer(serverInfo);
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
        startActivityForResult(i, REQUEST_ADD_SERVER);
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
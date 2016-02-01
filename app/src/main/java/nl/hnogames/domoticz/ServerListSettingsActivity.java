/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.ServerAdapter;
import nl.hnogames.domoticz.Containers.ServerInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.ServerClickListener;
import nl.hnogames.domoticz.Utils.ServerUtil;


public class ServerListSettingsActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private String TAG = ServerListSettingsActivity.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_ADD_SERVER = 54;
    private ServerUtil mServerUtil;
    private CoordinatorLayout coordinatorLayout;
    private ServerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.server_settings);

        mServerUtil = new ServerUtil(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        createListView();
    }

    private void createListView() {
        ArrayList<ServerInfo> mServerList = mServerUtil.getServerList();
        adapter = new ServerAdapter(this, mServerList, new ServerClickListener() {
            @Override
            public boolean onEnableClick(ServerInfo server, boolean checked) {
                if (server.getServerName().equals(Domoticz.DOMOTICZ_DEFAULT_SERVER)) {
                    showSimpleSnackbar(getString(R.string.cant_disable_default_server));
                    server.setEnabled(!checked);
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
                    showSimpleSnackbar(getString(R.string.cant_remove_default_server));
                } else
                    showRemoveUndoSnackbar(server);
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {
                //edit function to be build
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

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.server));
        Snackbar.make(coordinatorLayout,
                text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addServerToListView(serverInfo);
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        switch (event) {
                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                            case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                            case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                                removeServerFromSettings(serverInfo);
                                break;
                        }
                    }
                })
                .show();
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
        //TODO: Add Server Activity
        Intent i = new Intent(ServerListSettingsActivity.this, ServerSettingsActivity.class);
        //noinspection SpellCheckingInspection
        i.putExtra("ADDSERVER", true);
        startActivityForResult(i, REQUEST_ADD_SERVER);
    }

    private void showSimpleSnackbar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
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
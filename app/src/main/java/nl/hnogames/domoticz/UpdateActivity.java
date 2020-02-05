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
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Containers.VersionInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.DownloadUpdateServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateDomoticzServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateDownloadReadyReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateVersionReceiver;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class UpdateActivity extends AppCompatAssistActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private final int SERVER_UPDATE_TIME = 3; // Time in minutes
    @SuppressWarnings("unused")
    private String TAG = UpdateActivity.class.getSimpleName();
    private Domoticz mDomoticz;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MaterialDialog progressDialog;
    private Button buttonUpdateServer;
    private TextView currentServerVersionValue;
    private TextView updateServerVersionValue;
    private TextView updateSummary;
    private ServerUtil serverUtil;
    private SharedPrefUtil mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        serverUtil = new ServerUtil(this);
        mDomoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());

        initViews();
    }

    private void initViews() {
        currentServerVersionValue = findViewById(R.id.currentServerVersion_value);
        updateServerVersionValue = findViewById(R.id.updateServerVersion_value);
        updateSummary = findViewById(R.id.updateSummary);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        // Get latest Domoticz server version
        mDomoticz.getServerVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(VersionInfo serverVersion) {
                if (serverVersion == null)
                    return;
                if (serverUtil.getActiveServer() != null &&
                    serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this) != null) {
                    currentServerVersionValue.setText(serverVersion.getVersion());

                    if (serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).isUpdateAvailable()) {
                        updateSummary.setText(R.string.server_update_available);
                        updateServerVersionValue.setText(serverUtil.getActiveServer()
                            .getServerUpdateInfo(UpdateActivity.this)
                            .getUpdateRevisionNumber());
                    } else if (mSharedPrefs.isDebugEnabled()) {
                        String message = "Debugging: " + getString(R.string.server_update_available);
                        updateSummary.setText(message);
                    } else
                        updateSummary.setText(R.string.server_update_not_available);

                    buttonUpdateServer = findViewById(R.id.buttonUpdateServer);
                    buttonUpdateServer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showServerUpdateWarningDialog();
                        }
                    });
                    if (!serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).isUpdateAvailable()
                        && !mSharedPrefs.isDebugEnabled())
                        buttonUpdateServer.setEnabled(false);
                }
            }

            @Override
            public void onError(Exception error) {
                mSwipeRefreshLayout.setRefreshing(false);
                String message = String.format(
                    getString(R.string.error_couldNotCheckForUpdates),
                    mDomoticz.getErrorMessage(error));
                showSnackbar(message);
                if (serverUtil != null &&
                    serverUtil.getActiveServer() != null &&
                    serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this) != null)
                    serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).setCurrentServerVersion("");
                currentServerVersionValue.setText(R.string.not_available);
            }
        });
    }

    private void refreshData() {
        checkServerUpdateVersion();
        getCurrentServerVersion();
    }

    private void showServerUpdateWarningDialog() {
        new MaterialDialog.Builder(this)
            .title(R.string.server_update)
            .content(getString(R.string.update_server_warning)
                + UsefulBits.newLine()
                + UsefulBits.newLine()
                + getString(R.string.continue_question))
            .positiveText(R.string.yes)
            .negativeText(R.string.no)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    updateServer();
                }
            })
            .show();
    }

    private void updateServer() {
        // Cancel the check prerequisites dialog
        if (progressDialog != null)
            progressDialog.cancel();

        final boolean showMinMax = false;
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
            .title(R.string.msg_please_wait)
            .content(getString(R.string.please_wait_while_server_updated)
                + UsefulBits.newLine()
                + getString(R.string.this_take_minutes))
            .cancelable(false)
            .progress(false, SERVER_UPDATE_TIME * 60, showMinMax)
            .show();

        CountDownTimer mCountDownTimer = new CountDownTimer(SERVER_UPDATE_TIME * 60 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                dialog.incrementProgress(1);
            }

            @Override
            public void onFinish() {
                dialog.cancel();
                showMessageUpdateSuccess();
                refreshData();
            }
        };
        mCountDownTimer.start();

        if (mSharedPrefs.isDebugEnabled() || serverUtil.getActiveServer().getServerUpdateInfo(this).isUpdateAvailable()) {
            mDomoticz.getDownloadUpdate(new DownloadUpdateServerReceiver() {
                @Override
                public void onDownloadStarted(boolean updateSuccess) {
                    if (updateSuccess) {
                        showSnackbar("Downloading the new update for the server");
                        mDomoticz.getUpdateDownloadReady(new UpdateDownloadReadyReceiver() {
                            @Override
                            public void onUpdateDownloadReady(boolean downloadOk) {
                                showSnackbar("Download finished, starting to update Domoticz");
                                mDomoticz.updateDomoticzServer(new UpdateDomoticzServerReceiver() {
                                    @Override
                                    public void onUpdateFinish(boolean updateSuccess) {
                                        if (updateSuccess)
                                            showSnackbar("Your system is updating at this moment");
                                        else {
                                            showSnackbar(getString(R.string.update_not_started_unknown_error));
                                            dialog.cancel();
                                            showMessageUpdateFailed();
                                            refreshData();
                                        }
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                        if (error.getMessage().contains("Time"))
                                            showSnackbar("Your system is updating at this moment");
                                        else {
                                            showSnackbar("Could not update the domoticz server via the script " + error.getMessage());
                                            dialog.cancel();
                                            showMessageUpdateFailed();
                                            refreshData();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception error) {
                                showSnackbar(getString(R.string.update_server_downloadNotReady1));
                                dialog.cancel();
                                showMessageUpdateFailed();
                                refreshData();
                            }
                        });
                    } else {
                        showSnackbar(getString(R.string.update_not_started_unknown_error));
                        dialog.cancel();
                        showMessageUpdateFailed();
                        refreshData();
                    }
                }

                @Override
                public void onError(Exception error) {
                    showSnackbar("Could not download the update " + error.getMessage());
                    dialog.cancel();
                    showMessageUpdateFailed();
                    refreshData();
                }
            });
        }
    }

    private void showMessageUpdateSuccess() {
        String message = "Update was successful";
        String title = "Update successful";
        showSimpleDialog(title, message);
    }

    @SuppressWarnings("unused")
    private void showMessageUpdateFailed() {
        String message = "Update failed. Please login to your server and/or review the logs there";
        String title = "Update failed";
        showSimpleDialog(title, message);
    }

    @SuppressWarnings("unused")
    private void showMessageUpdateNotStarted() {
        String message = getString(R.string.update_not_started_unknown_error);
        String title = getString(R.string.update_not_started);
        showSimpleDialog(title, message);
    }

    private void checkServerUpdateVersion() {
        mSwipeRefreshLayout.setRefreshing(true);

        // Get latest Domoticz version update
        mDomoticz.getUpdate(new UpdateVersionReceiver() {
            @Override
            public void onReceiveUpdate(ServerUpdateInfo serverUpdateInfo) {
                // Write update version to shared preferences
                boolean haveUpdate = serverUpdateInfo.isUpdateAvailable();

                serverUtil.getActiveServer().setServerUpdateInfo(UpdateActivity.this, serverUpdateInfo);
                serverUtil.saveDomoticzServers(false);
                if (!mSharedPrefs.isDebugEnabled()) buttonUpdateServer.setEnabled(haveUpdate);

                if (haveUpdate) {
                    updateServerVersionValue.setText(serverUpdateInfo.getUpdateRevisionNumber());
                    updateSummary.setText(R.string.server_update_available);
                } else updateSummary.setText(R.string.server_update_not_available);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Exception error) {
                String message = String.format(
                    getString(R.string.error_couldNotCheckForUpdates),
                    mDomoticz.getErrorMessage(error));
                showSnackbar(message);
                serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).setUpdateRevisionNumber("");
                updateServerVersionValue.setText(R.string.not_available);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getCurrentServerVersion() {
        mSwipeRefreshLayout.setRefreshing(true);

        // Get latest Domoticz server version
        mDomoticz.getServerVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(VersionInfo serverVersion) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (serverVersion != null && !UsefulBits.isEmpty(serverVersion.getVersion())) {
                    if (serverUtil != null &&
                        serverUtil.getActiveServer() != null &&
                        serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this) != null)
                        serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).setCurrentServerVersion(serverVersion.getVersion());
                    currentServerVersionValue.setText(serverVersion.getVersion());
                } else currentServerVersionValue.setText(R.string.not_available);
            }

            @Override
            public void onError(Exception error) {
                mSwipeRefreshLayout.setRefreshing(false);
                String message = String.format(
                    getString(R.string.error_couldNotCheckForUpdates),
                    mDomoticz.getErrorMessage(error));
                showSnackbar(message);
                if (serverUtil != null &&
                    serverUtil.getActiveServer() != null &&
                    serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this) != null)
                    serverUtil.getActiveServer().getServerUpdateInfo(UpdateActivity.this).setCurrentServerVersion("");
                currentServerVersionValue.setText(R.string.not_available);
            }
        });
    }

    private void showSnackbar(String message) {
        CoordinatorLayout fragmentCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        if (fragmentCoordinatorLayout != null) {
            UsefulBits.showSnackbar(this, fragmentCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        }
    }

    private void showSimpleDialog(String title, String message) {
        new MaterialDialog.Builder(this)
            .title(title)
            .content(message)
            .positiveText(R.string.ok)
            .show();
    }
}
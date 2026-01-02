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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ftinc.scoop.Scoop;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.utils.FirebaseConfigHelper;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class FirebaseSettingsActivity extends AppCompatPermissionsActivity {
    private static final String TAG = "FirebaseSettings";
    private static final int PICK_JSON_FILE = 1001;

    private SharedPrefUtil mSharedPrefs;
    private TextInputEditText editProjectId;
    private TextInputEditText editAppId;
    private TextInputEditText editApiKey;
    private TextInputEditText editSenderId;
    private Button btnSave;
    private Button btnUploadJson;
    private Button btnClear;
    private Button btnTest;
    private LinearLayout layoutManualConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);

        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firebase_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setTitle(R.string.firebase_settings_title);

        initViews();
        loadExistingConfig();
        setupListeners();
    }

    private void initViews() {
        layoutManualConfig = findViewById(R.id.layoutManualConfig);
        editProjectId = findViewById(R.id.editProjectId);
        editAppId = findViewById(R.id.editAppId);
        editApiKey = findViewById(R.id.editApiKey);
        editSenderId = findViewById(R.id.editSenderId);
        btnSave = findViewById(R.id.btnSave);
        btnUploadJson = findViewById(R.id.btnUploadJson);
        btnClear = findViewById(R.id.btnClear);
        btnTest = findViewById(R.id.btnTest);
    }

    private void loadExistingConfig() {
        if (mSharedPrefs.hasFirebaseConfig()) {
            editProjectId.setText(mSharedPrefs.getFcmProjectId());
            editAppId.setText(mSharedPrefs.getFcmAppId());
            editApiKey.setText(mSharedPrefs.getFcmApiKey());
            editSenderId.setText(mSharedPrefs.getFcmSenderId());
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveConfiguration());
        btnUploadJson.setOnClickListener(v -> openJsonFilePicker());
        btnClear.setOnClickListener(v -> clearConfiguration());
        btnTest.setOnClickListener(v -> testConfiguration());
    }

    private void saveConfiguration() {
        String projectId = editProjectId.getText() != null ? editProjectId.getText().toString().trim() : "";
        String appId = editAppId.getText() != null ? editAppId.getText().toString().trim() : "";
        String apiKey = editApiKey.getText() != null ? editApiKey.getText().toString().trim() : "";
        String senderId = editSenderId.getText() != null ? editSenderId.getText().toString().trim() : "";

        if (UsefulBits.isEmpty(projectId) || UsefulBits.isEmpty(appId) ||
            UsefulBits.isEmpty(apiKey) || UsefulBits.isEmpty(senderId)) {
            showSnackbar(getString(R.string.firebase_config_incomplete));
            return;
        }

        mSharedPrefs.saveFirebaseConfig(projectId, appId, apiKey, senderId);

        // Reinitialize Firebase with new configuration
        FirebaseConfigHelper.initializeFirebase(this, mSharedPrefs);

        showSnackbar(getString(R.string.firebase_config_saved));

        // Restart the app to apply changes
        new MaterialDialog.Builder(this)
                .title(R.string.firebase_config_saved)
                .content(R.string.firebase_restart_required)
                .positiveText(R.string.restart_now)
                .negativeText(R.string.firebase_restart_later)
                .onPositive((dialog, which) -> {
                    AppController.getInstance().restartApp();
                })
                .show();
    }

    private void openJsonFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, PICK_JSON_FILE);
    }

    private void clearConfiguration() {
        new MaterialDialog.Builder(this)
                .title(R.string.firebase_clear_config)
                .content(R.string.firebase_clear_config_confirm)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> {
                    mSharedPrefs.clearFirebaseConfig();
                    editProjectId.setText("");
                    editAppId.setText("");
                    editApiKey.setText("");
                    editSenderId.setText("");
                    showSnackbar(getString(R.string.firebase_config_cleared));
                })
                .show();
    }

    private void testConfiguration() {
        if (!mSharedPrefs.hasFirebaseConfig()) {
            showSnackbar(getString(R.string.firebase_config_not_configured));
            return;
        }

        FirebaseConfigHelper.testConfiguration(this, new FirebaseConfigHelper.TestCallback() {
            @Override
            public void onSuccess(String token) {
                showSnackbar(getString(R.string.firebase_test_success) + "\n" + token);
            }

            @Override
            public void onError(String error) {
                showSnackbar(getString(R.string.firebase_test_failed) + ": " + error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_JSON_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                parseGoogleServicesJson(data.getData());
            }
        }
    }

    private void parseGoogleServicesJson(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();

            String json = stringBuilder.toString();
            JSONObject obj = new JSONObject(json);

            // Extract project info
            JSONObject projectInfo = obj.getJSONObject("project_info");
            String projectId = projectInfo.getString("project_id");
            String projectNumber = projectInfo.getString("project_number");

            // Extract client info
            JSONArray clientArray = obj.getJSONArray("client");
            JSONObject client = clientArray.getJSONObject(0);

            JSONObject clientInfo = client.getJSONObject("client_info");
            String appId = clientInfo.getString("mobilesdk_app_id");

            // Extract API key
            JSONArray apiKeyArray = client.getJSONArray("api_key");
            String apiKey = apiKeyArray.getJSONObject(0).getString("current_key");

            // Set the values
            editProjectId.setText(projectId);
            editAppId.setText(appId);
            editApiKey.setText(apiKey);
            editSenderId.setText(projectNumber);

            showSnackbar(getString(R.string.firebase_json_parsed));

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse google-services.json", e);
            showSnackbar(getString(R.string.firebase_json_parse_failed) + ": " + e.getMessage());
        }
    }

    private void showSnackbar(String message) {
        UsefulBits.showSnackbar(this, layoutManualConfig, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


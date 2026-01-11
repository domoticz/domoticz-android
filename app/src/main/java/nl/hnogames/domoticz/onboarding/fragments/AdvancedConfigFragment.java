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

package nl.hnogames.domoticz.onboarding.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.isupatches.wisefy.WiseFy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.onboarding.OnboardingViewModel;
import nl.hnogames.domoticz.ui.MultiSelectionSpinner;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class AdvancedConfigFragment extends Fragment implements OnPermissionCallback {

    private OnboardingViewModel viewModel;
    private SwitchMaterial switchAdvanced;
    private View advancedSettingsLayout;
    private MultiSelectionSpinner wifiSpinner;
    private TextInputEditText editLocalAddress;
    private TextInputEditText editLocalPort;
    private TextInputEditText editLocalUsername;
    private TextInputEditText editLocalPassword;
    private AutoCompleteTextView spinnerLocalProtocol;
    private PermissionFragmentHelper permissionFragmentHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_advanced_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((OnboardingActivity) requireActivity()).getViewModel();
        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);

        switchAdvanced = view.findViewById(R.id.switch_advanced);
        advancedSettingsLayout = view.findViewById(R.id.advanced_settings_layout);
        wifiSpinner = view.findViewById(R.id.local_wifi_spinner);
        editLocalAddress = view.findViewById(R.id.edit_local_server_address);
        editLocalPort = view.findViewById(R.id.edit_local_port);
        editLocalUsername = view.findViewById(R.id.edit_local_username);
        editLocalPassword = view.findViewById(R.id.edit_local_password);
        spinnerLocalProtocol = view.findViewById(R.id.spinner_local_protocol);

        View toggleContainer = view.findViewById(R.id.toggle_container);
        MaterialButton btnTestConnection = view.findViewById(R.id.btn_test_connection);
        MaterialButton btnSkip = view.findViewById(R.id.btn_skip);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().onBackPressed()
        );

        setupProtocolSpinner();

        // Make the entire container clickable to toggle the switch
        toggleContainer.setOnClickListener(v -> {
            switchAdvanced.setChecked(!switchAdvanced.isChecked());
        });

        switchAdvanced.setOnCheckedChangeListener((buttonView, isChecked) -> {
            advancedSettingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            viewModel.setUseDifferentLocalAddress(isChecked);
            if (isChecked) {
                loadWifiNetworks();
            }
        });

        observeViewModel();

        btnTestConnection.setOnClickListener(v -> {
            saveToViewModel();
            Navigation.findNavController(v).navigate(R.id.action_advanced_config_to_connection_test);
        });

        btnSkip.setOnClickListener(v -> {
            viewModel.setUseDifferentLocalAddress(false);
            Navigation.findNavController(v).navigate(R.id.action_advanced_config_to_connection_test);
        });
    }

    private void setupProtocolSpinner() {
        String[] protocols = new String[]{"HTTP", "HTTPS"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, protocols);
        spinnerLocalProtocol.setAdapter(adapter);
        spinnerLocalProtocol.setText("HTTP", false);

        spinnerLocalProtocol.setOnItemClickListener((parent, view, position, id) -> {
            boolean useHttps = position == 1;
            viewModel.setLocalUseHttps(useHttps);
        });
    }

    private void loadWifiNetworks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionsUtil.canAccessLocation(getActivity())) {
                permissionFragmentHelper.request(PermissionsUtil.INITIAL_LOCATION_PERMS);
            } else {
                scanWifiNetworks();
            }
        } else {
            scanWifiNetworks();
        }
    }

    private void scanWifiNetworks() {
        try {
            Set<String> ssidFromPrefs = viewModel.getLocalWifiSsids().getValue();
            final ArrayList<String> ssids = new ArrayList<>();
            final ArrayList<String> selectedSsids = new ArrayList<>();

            if (ssidFromPrefs != null && ssidFromPrefs.size() > 0) {
                selectedSsids.addAll(ssidFromPrefs);
                ssids.addAll(ssidFromPrefs);
            }

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                WiseFy wisefy = new WiseFy.Brains(getActivity()).getSmarts();
                List<ScanResult> nearbyAccessPoints = wisefy.getNearbyAccessPoints(true);

                if (nearbyAccessPoints != null && nearbyAccessPoints.size() > 0) {
                    for (ScanResult ssid : nearbyAccessPoints) {
                        if (!UsefulBits.isEmpty(ssid.SSID) && !ssids.contains(ssid.SSID)) {
                            ssids.add(ssid.SSID);
                        }
                    }
                }
            }

            if (ssids.isEmpty()) {
                ssids.add(getString(R.string.welcome_msg_no_ssid_found));
                wifiSpinner.setEnabled(false);
            } else {
                wifiSpinner.setEnabled(true);
            }

            wifiSpinner.setTitle(R.string.local_wifi_networks);
            wifiSpinner.setItems(ssids);
            wifiSpinner.setSelection(selectedSsids);

        } catch (Exception ex) {
            Log.e("AdvancedConfig", "Error scanning WiFi", ex);
        }
    }

    private void observeViewModel() {
        viewModel.getUseDifferentLocalAddress().observe(getViewLifecycleOwner(), useDifferent -> {
            switchAdvanced.setChecked(useDifferent);
            advancedSettingsLayout.setVisibility(useDifferent ? View.VISIBLE : View.GONE);
        });

        viewModel.getLocalServerAddress().observe(getViewLifecycleOwner(), address -> {
            if (editLocalAddress.getText() == null ||
                !editLocalAddress.getText().toString().equals(address)) {
                editLocalAddress.setText(address);
            }
        });

        viewModel.getLocalServerPort().observe(getViewLifecycleOwner(), port -> {
            if (editLocalPort.getText() == null ||
                !editLocalPort.getText().toString().equals(port)) {
                editLocalPort.setText(port);
            }
        });

        viewModel.getLocalUseHttps().observe(getViewLifecycleOwner(), useHttps -> {
            spinnerLocalProtocol.setText(useHttps ? "HTTPS" : "HTTP", false);
        });

        viewModel.getLocalUsername().observe(getViewLifecycleOwner(), username -> {
            if (editLocalUsername.getText() == null ||
                !editLocalUsername.getText().toString().equals(username)) {
                editLocalUsername.setText(username);
            }
        });

        viewModel.getLocalPassword().observe(getViewLifecycleOwner(), password -> {
            if (editLocalPassword.getText() == null ||
                !editLocalPassword.getText().toString().equals(password)) {
                editLocalPassword.setText(password);
            }
        });
    }

    private void saveToViewModel() {
        if (switchAdvanced.isChecked()) {
            if (editLocalAddress.getText() != null) {
                viewModel.setLocalServerAddress(editLocalAddress.getText().toString().trim());
            }
            if (editLocalPort.getText() != null) {
                viewModel.setLocalServerPort(editLocalPort.getText().toString().trim());
            }
            if (editLocalUsername.getText() != null) {
                viewModel.setLocalUsername(editLocalUsername.getText().toString().trim());
            }
            if (editLocalPassword.getText() != null) {
                viewModel.setLocalPassword(editLocalPassword.getText().toString().trim());
            }

            // Save selected WiFi SSIDs
            List<String> selectedSsids = wifiSpinner.getSelectedStrings();
            if (selectedSsids != null && !selectedSsids.isEmpty()) {
                viewModel.setLocalWifiSsids(new HashSet<>(selectedSsids));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToViewModel();
    }

    // Permission callbacks
    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        String[] neededPermission = PermissionFragmentHelper.declinedPermissions(this, PermissionsUtil.INITIAL_LOCATION_PERMS);
        AlertDialog alert = PermissionsUtil.getAlertDialog(getActivity(), permissionFragmentHelper,
                getString(R.string.permission_title),
                getString(R.string.permission_desc_location), neededPermission);
        if (alert != null && !alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.i("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.i("onNoPermissionNeeded", "Permission(s) not needed");
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        if (PermissionsUtil.canAccessLocation(getActivity())) {
            scanWifiNetworks();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionFragmentHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


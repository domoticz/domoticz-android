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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Set;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.onboarding.OnboardingViewModel;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.VersionInfo;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;

public class ConnectionTestFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private View progressLayout;
    private View successLayout;
    private View errorLayout;
    private TextView successMessage;
    private TextView errorMessage;
    private MaterialButton btnFinish;
    private MaterialButton btnRetry;
    private MaterialButton btnEditSettings;
    private SharedPrefUtil sharedPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_connection_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((OnboardingActivity) requireActivity()).getViewModel();
        sharedPrefs = new SharedPrefUtil(requireContext());

        progressLayout = view.findViewById(R.id.progress_layout);
        successLayout = view.findViewById(R.id.success_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        successMessage = view.findViewById(R.id.success_message);
        errorMessage = view.findViewById(R.id.error_message);
        btnFinish = view.findViewById(R.id.btn_finish);
        btnRetry = view.findViewById(R.id.btn_retry);
        btnEditSettings = view.findViewById(R.id.btn_edit_settings);

        btnFinish.setOnClickListener(v -> finishOnboarding(true));
        btnRetry.setOnClickListener(v -> testConnection());
        btnEditSettings.setOnClickListener(v -> requireActivity().onBackPressed());

        observeViewModel();

        // Auto-start test
        testConnection();
    }

    private void observeViewModel() {
        viewModel.getValidationState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case IDLE:
                    showProgress();
                    break;
                case VALIDATING:
                    showProgress();
                    break;
                case SUCCESS:
                    String msg = viewModel.getValidationMessage().getValue();
                    showSuccess(msg != null ? msg : getString(R.string.connection_successful));
                    break;
                case ERROR:
                    String errMsg = viewModel.getValidationMessage().getValue();
                    showError(errMsg != null ? errMsg : getString(R.string.connection_failed));
                    break;
            }
        });
    }

    private void testConnection() {
        viewModel.setValidationState(OnboardingViewModel.ValidationState.VALIDATING);

        // Save configuration to server
        saveConfiguration();

        // Test connection
        StaticHelper.getDomoticz(requireContext()).getServerVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(VersionInfo version) {
                if (isAdded()) {
                    String tempMessage = getString(R.string.welcome_msg_serverVersion) + ": " + version.getVersion();

                    StaticHelper.getDomoticz(requireContext()).getDevices(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                            if (isAdded()) {
                                String fullMessage = tempMessage + "\n" +
                                        String.format(getString(R.string.welcome_msg_numberOfDevices), mDevicesInfo.size());
                                viewModel.setValidationMessage(fullMessage);
                                viewModel.setValidationState(OnboardingViewModel.ValidationState.SUCCESS);
                                sharedPrefs.setWelcomeWizardSuccess(true);
                            }
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo mDevicesInfo) {}

                        @Override
                        public void onError(Exception error) {
                            if (isAdded()) {
                                viewModel.setValidationMessage(tempMessage);
                                viewModel.setValidationState(OnboardingViewModel.ValidationState.SUCCESS);
                                sharedPrefs.setWelcomeWizardSuccess(true);
                            }
                        }
                    }, 0, null);
                }
            }

            @Override
            public void onError(Exception error) {
                if (isAdded()) {
                    String errorMsg = StaticHelper.getDomoticz(requireContext()).getErrorMessage(error);
                    viewModel.setValidationMessage(errorMsg);
                    viewModel.setValidationState(OnboardingViewModel.ValidationState.ERROR);
                    sharedPrefs.setWelcomeWizardSuccess(false);
                }
            }
        });
    }

    private void saveConfiguration() {
        // Save to server config
        String serverName = viewModel.getServerName().getValue();
        String serverAddress = viewModel.getServerAddress().getValue();
        String serverPort = viewModel.getServerPort().getValue();
        Boolean useHttps = viewModel.getUseHttps().getValue();
        String serverDirectory = viewModel.getServerDirectory().getValue();
        String username = viewModel.getUsername().getValue();
        String password = viewModel.getPassword().getValue();
        Boolean useDifferentLocal = viewModel.getUseDifferentLocalAddress().getValue();

        StaticHelper.getServerUtil(requireContext()).getActiveServer().setServerName(serverName);
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerUrl(serverAddress);
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerPort(serverPort);
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerSecure(useHttps != null && useHttps);
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerDirectory(serverDirectory != null ? serverDirectory : "");
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerUsername(username != null ? username : "");
        StaticHelper.getServerUtil(requireContext()).getActiveServer().setRemoteServerPassword(password != null ? password : "");

        if (useDifferentLocal != null && useDifferentLocal) {
            // Save local server settings
            String localAddress = viewModel.getLocalServerAddress().getValue();
            String localPort = viewModel.getLocalServerPort().getValue();
            Boolean localUseHttps = viewModel.getLocalUseHttps().getValue();
            String localDirectory = viewModel.getLocalServerDirectory().getValue();
            String localUsername = viewModel.getLocalUsername().getValue();
            String localPassword = viewModel.getLocalPassword().getValue();
            Set<String> localSsids = viewModel.getLocalWifiSsids().getValue();

            StaticHelper.getServerUtil(requireContext()).getActiveServer().setIsLocalServerAddressDifferent(true);
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerUrl(localAddress != null ? localAddress : serverAddress);
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerPort(localPort != null ? localPort : serverPort);
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerSecure(localUseHttps != null && localUseHttps);
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerDirectory(localDirectory != null ? localDirectory : "");
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerUsername(localUsername != null ? localUsername : "");
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerPassword(localPassword != null ? localPassword : "");

            if (localSsids != null && !localSsids.isEmpty()) {
                StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalServerSsid(new ArrayList<>(localSsids));
            }
        } else {
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setLocalSameAddressAsRemote();
            StaticHelper.getServerUtil(requireContext()).getActiveServer().setIsLocalServerAddressDifferent(false);
        }

        StaticHelper.getServerUtil(requireContext()).saveDomoticzServers(true);
    }

    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
        successLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        progressLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        successMessage.setText(message);
    }

    private void showError(String message) {
        progressLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
    }

    private void finishOnboarding(boolean success) {
        ((OnboardingActivity) requireActivity()).finishOnboarding(success);
    }
}


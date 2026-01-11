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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.onboarding.OnboardingViewModel;

public class BasicConfigFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private TextInputEditText editServerName;
    private TextInputEditText editServerAddress;
    private TextInputEditText editServerPort;
    private AutoCompleteTextView spinnerProtocol;
    private TextInputEditText editServerDirectory;
    private MaterialButton btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_basic_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((OnboardingActivity) requireActivity()).getViewModel();

        editServerName = view.findViewById(R.id.edit_server_name);
        editServerAddress = view.findViewById(R.id.edit_server_address);
        editServerPort = view.findViewById(R.id.edit_server_port);
        spinnerProtocol = view.findViewById(R.id.spinner_protocol);
        editServerDirectory = view.findViewById(R.id.edit_server_directory);
        btnNext = view.findViewById(R.id.btn_next);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().onBackPressed()
        );

        setupProtocolSpinner();
        setupTextWatchers();
        observeViewModel();

        btnNext.setOnClickListener(v -> {
            saveToViewModel();
            Navigation.findNavController(v).navigate(R.id.action_basic_config_to_auth_config);
        });
    }

    private void setupProtocolSpinner() {
        String[] protocols = new String[]{"HTTP", "HTTPS"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, protocols);
        spinnerProtocol.setAdapter(adapter);

        spinnerProtocol.setOnItemClickListener((parent, view, position, id) -> {
            boolean useHttps = position == 1;
            viewModel.setUseHttps(useHttps);
        });
    }

    private void setupTextWatchers() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInput();
            }
        };

        editServerName.addTextChangedListener(validationWatcher);
        editServerAddress.addTextChangedListener(validationWatcher);
        editServerPort.addTextChangedListener(validationWatcher);
    }

    private void observeViewModel() {
        viewModel.getServerName().observe(getViewLifecycleOwner(), name -> {
            if (editServerName.getText() == null ||
                !editServerName.getText().toString().equals(name)) {
                editServerName.setText(name);
            }
        });

        viewModel.getServerAddress().observe(getViewLifecycleOwner(), address -> {
            if (editServerAddress.getText() == null ||
                !editServerAddress.getText().toString().equals(address)) {
                editServerAddress.setText(address);
            }
        });

        viewModel.getServerPort().observe(getViewLifecycleOwner(), port -> {
            if (editServerPort.getText() == null ||
                !editServerPort.getText().toString().equals(port)) {
                editServerPort.setText(port);
            }
        });

        viewModel.getUseHttps().observe(getViewLifecycleOwner(), useHttps -> {
            spinnerProtocol.setText(useHttps ? "HTTPS" : "HTTP", false);
        });

        viewModel.getServerDirectory().observe(getViewLifecycleOwner(), directory -> {
            if (editServerDirectory.getText() == null ||
                !editServerDirectory.getText().toString().equals(directory)) {
                editServerDirectory.setText(directory);
            }
        });
    }

    private void validateInput() {
        saveToViewModel();
        btnNext.setEnabled(viewModel.isBasicConfigValid());
    }

    private void saveToViewModel() {
        if (editServerName.getText() != null) {
            viewModel.setServerName(editServerName.getText().toString().trim());
        }
        if (editServerAddress.getText() != null) {
            viewModel.setServerAddress(editServerAddress.getText().toString().trim());
        }
        if (editServerPort.getText() != null) {
            viewModel.setServerPort(editServerPort.getText().toString().trim());
        }
        if (editServerDirectory.getText() != null) {
            viewModel.setServerDirectory(editServerDirectory.getText().toString().trim());
        }
    }
}


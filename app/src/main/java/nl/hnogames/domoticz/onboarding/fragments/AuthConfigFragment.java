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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.onboarding.OnboardingViewModel;

public class AuthConfigFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_auth_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((OnboardingActivity) requireActivity()).getViewModel();

        editUsername = view.findViewById(R.id.edit_username);
        editPassword = view.findViewById(R.id.edit_password);
        MaterialButton btnNext = view.findViewById(R.id.btn_next);
        MaterialButton btnSkip = view.findViewById(R.id.btn_skip);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().onBackPressed()
        );

        observeViewModel();

        btnNext.setOnClickListener(v -> {
            saveToViewModel();
            Navigation.findNavController(v).navigate(R.id.action_auth_config_to_advanced_config);
        });

        btnSkip.setOnClickListener(v -> {
            viewModel.setUsername("");
            viewModel.setPassword("");
            Navigation.findNavController(v).navigate(R.id.action_auth_config_to_advanced_config);
        });
    }

    private void observeViewModel() {
        viewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            if (editUsername.getText() == null ||
                !editUsername.getText().toString().equals(username)) {
                editUsername.setText(username);
            }
        });

        viewModel.getPassword().observe(getViewLifecycleOwner(), password -> {
            if (editPassword.getText() == null ||
                !editPassword.getText().toString().equals(password)) {
                editPassword.setText(password);
            }
        });
    }

    private void saveToViewModel() {
        if (editUsername.getText() != null) {
            viewModel.setUsername(editUsername.getText().toString().trim());
        }
        if (editPassword.getText() != null) {
            viewModel.setPassword(editPassword.getText().toString().trim());
        }
    }
}


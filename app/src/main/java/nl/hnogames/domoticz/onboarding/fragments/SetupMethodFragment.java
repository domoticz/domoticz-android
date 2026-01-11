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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.onboarding.OnboardingViewModel;

public class SetupMethodFragment extends Fragment {

    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_setup_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((OnboardingActivity) requireActivity()).getViewModel();

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().onBackPressed()
        );

        MaterialCardView cardDemo = view.findViewById(R.id.card_demo);
        // MaterialCardView cardQrCode = view.findViewById(R.id.card_qr_code); // Disabled until Domoticz supports QR generation
        MaterialCardView cardManual = view.findViewById(R.id.card_manual);

        cardDemo.setOnClickListener(v -> {
            viewModel.setDemoConfiguration();
            Navigation.findNavController(v).navigate(R.id.action_setup_method_to_connection_test);
        });

        // QR Code scanning disabled - Domoticz doesn't generate QR codes yet
        // cardQrCode.setOnClickListener(v ->
        //     Navigation.findNavController(v).navigate(R.id.action_setup_method_to_qr_scanner)
        // );

        cardManual.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_setup_method_to_basic_config)
        );
    }
}


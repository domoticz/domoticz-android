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

package nl.hnogames.domoticz.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Set;

public class OnboardingViewModel extends ViewModel {

    // Server configuration state
    private final MutableLiveData<String> serverName = new MutableLiveData<>("");
    private final MutableLiveData<String> serverAddress = new MutableLiveData<>("");
    private final MutableLiveData<String> serverPort = new MutableLiveData<>("8080");
    private final MutableLiveData<Boolean> useHttps = new MutableLiveData<>(false);
    private final MutableLiveData<String> serverDirectory = new MutableLiveData<>("");

    // Authentication
    private final MutableLiveData<String> username = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");

    // Advanced settings
    private final MutableLiveData<Boolean> useDifferentLocalAddress = new MutableLiveData<>(false);
    private final MutableLiveData<String> localServerAddress = new MutableLiveData<>("");
    private final MutableLiveData<String> localServerPort = new MutableLiveData<>("8080");
    private final MutableLiveData<Boolean> localUseHttps = new MutableLiveData<>(false);
    private final MutableLiveData<String> localServerDirectory = new MutableLiveData<>("");
    private final MutableLiveData<String> localUsername = new MutableLiveData<>("");
    private final MutableLiveData<String> localPassword = new MutableLiveData<>("");
    private final MutableLiveData<Set<String>> localWifiSsids = new MutableLiveData<>();

    // Validation state
    private final MutableLiveData<ValidationState> validationState = new MutableLiveData<>(ValidationState.IDLE);
    private final MutableLiveData<String> validationMessage = new MutableLiveData<>("");

    // Navigation state
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);

    // Getters
    public LiveData<String> getServerName() { return serverName; }
    public LiveData<String> getServerAddress() { return serverAddress; }
    public LiveData<String> getServerPort() { return serverPort; }
    public LiveData<Boolean> getUseHttps() { return useHttps; }
    public LiveData<String> getServerDirectory() { return serverDirectory; }
    public LiveData<String> getUsername() { return username; }
    public LiveData<String> getPassword() { return password; }
    public LiveData<Boolean> getUseDifferentLocalAddress() { return useDifferentLocalAddress; }
    public LiveData<String> getLocalServerAddress() { return localServerAddress; }
    public LiveData<String> getLocalServerPort() { return localServerPort; }
    public LiveData<Boolean> getLocalUseHttps() { return localUseHttps; }
    public LiveData<String> getLocalServerDirectory() { return localServerDirectory; }
    public LiveData<String> getLocalUsername() { return localUsername; }
    public LiveData<String> getLocalPassword() { return localPassword; }
    public LiveData<Set<String>> getLocalWifiSsids() { return localWifiSsids; }
    public LiveData<ValidationState> getValidationState() { return validationState; }
    public LiveData<String> getValidationMessage() { return validationMessage; }
    public LiveData<Integer> getCurrentStep() { return currentStep; }

    // Setters
    public void setServerName(String value) { serverName.setValue(value); }
    public void setServerAddress(String value) { serverAddress.setValue(value); }
    public void setServerPort(String value) { serverPort.setValue(value); }
    public void setUseHttps(boolean value) {
        useHttps.setValue(value);
        // Auto-update port if using default values
        if (value && "8080".equals(serverPort.getValue())) {
            serverPort.setValue("443");
        } else if (!value && "443".equals(serverPort.getValue())) {
            serverPort.setValue("8080");
        }
    }
    public void setServerDirectory(String value) { serverDirectory.setValue(value); }
    public void setUsername(String value) { username.setValue(value); }
    public void setPassword(String value) { password.setValue(value); }
    public void setUseDifferentLocalAddress(boolean value) { useDifferentLocalAddress.setValue(value); }
    public void setLocalServerAddress(String value) { localServerAddress.setValue(value); }
    public void setLocalServerPort(String value) { localServerPort.setValue(value); }
    public void setLocalUseHttps(boolean value) {
        localUseHttps.setValue(value);
        if (value && "8080".equals(localServerPort.getValue())) {
            localServerPort.setValue("443");
        } else if (!value && "443".equals(localServerPort.getValue())) {
            localServerPort.setValue("8080");
        }
    }
    public void setLocalServerDirectory(String value) { localServerDirectory.setValue(value); }
    public void setLocalUsername(String value) { localUsername.setValue(value); }
    public void setLocalPassword(String value) { localPassword.setValue(value); }
    public void setLocalWifiSsids(Set<String> value) { localWifiSsids.setValue(value); }
    public void setValidationState(ValidationState state) { validationState.setValue(state); }
    public void setValidationMessage(String message) { validationMessage.setValue(message); }
    public void setCurrentStep(int step) { currentStep.setValue(step); }

    // Load from QR code
    public void loadFromQrCode(String qrData) {
        // Parse QR code data (format: domoticz://setup?url=...&port=...&user=...)
        // This is a simplified parser - enhance as needed
        if (qrData != null && qrData.startsWith("domoticz://setup?")) {
            String params = qrData.substring("domoticz://setup?".length());
            String[] pairs = params.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    switch (key) {
                        case "url":
                            setServerAddress(value);
                            break;
                        case "port":
                            setServerPort(value);
                            break;
                        case "user":
                            setUsername(value);
                            break;
                        case "pass":
                            setPassword(value);
                            break;
                        case "https":
                            setUseHttps("true".equalsIgnoreCase(value) || "1".equals(value));
                            break;
                        case "name":
                            setServerName(value);
                            break;
                    }
                }
            }
        }
    }

    // Set demo server configuration
    public void setDemoConfiguration() {
        setServerName("Demo");
        setServerAddress("gandalf.domoticz.com");
        setServerPort("443");
        setUseHttps(true);
        setServerDirectory("");
        setUsername("demo");
        setPassword("@domoticz");
        setUseDifferentLocalAddress(false);
    }

    // Validation
    public boolean isBasicConfigValid() {
        String name = serverName.getValue();
        String address = serverAddress.getValue();
        String port = serverPort.getValue();

        return name != null && !name.trim().isEmpty() &&
               address != null && !address.trim().isEmpty() &&
               port != null && !port.trim().isEmpty() &&
               isValidPort(port);
    }

    private boolean isValidPort(String port) {
        try {
            int p = Integer.parseInt(port);
            return p > 0 && p <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public enum ValidationState {
        IDLE,
        VALIDATING,
        SUCCESS,
        ERROR
    }
}


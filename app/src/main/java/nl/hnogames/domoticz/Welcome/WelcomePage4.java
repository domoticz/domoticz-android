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

package nl.hnogames.domoticz.Welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class WelcomePage4 extends Fragment {

    private LinearLayout please_wait_layout;
    private TextView result;
    private LinearLayout result_layout;
    private String tempText = "";

    public static WelcomePage4 newInstance() {
        return new WelcomePage4();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome4, container, false);

        please_wait_layout = (LinearLayout) v.findViewById(R.id.layout_please_wait);
        result_layout = (LinearLayout) v.findViewById(R.id.layout_result);
        result = (TextView) v.findViewById(R.id.result);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            resetLayout();
            checkConnectionData();
            disableFinishButton();
        }
    }

    private void checkConnectionData() {
        ServerUtil mServerUtil = new ServerUtil(getActivity());
        final Domoticz mDomoticz = new Domoticz(getActivity(), AppController.getInstance().getRequestQueue());

        if (!mDomoticz.isConnectionDataComplete(mServerUtil.getActiveServer())) {
            setResultText(getString(R.string.welcome_msg_connectionDataIncomplete) + "\n\n"
                    + getString(R.string.welcome_msg_correctOnPreviousPage));
        } else if (!mDomoticz.isUrlValid(mServerUtil.getActiveServer())) {
            setResultText(getString(R.string.welcome_msg_connectionDataInvalid) + "\n\n"
                    + getString(R.string.welcome_msg_correctOnPreviousPage));
        } else {
            mDomoticz.getServerVersion(new VersionReceiver() {
                @Override
                public void onReceiveVersion(String version) {
                    if (isAdded()) {
                        tempText = getString(R.string.welcome_msg_serverVersion) + ": " + version;

                        mDomoticz.getDevices(new DevicesReceiver() {
                            @Override
                            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                                if (isAdded()) {
                                    tempText += "\n";
                                    String formatted = String.format(getString(R.string.welcome_msg_numberOfDevices), mDevicesInfo.size());
                                    tempText += formatted;
                                    setSuccessText(tempText);
                                }
                            }

                            @Override
                            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                            }

                            @Override
                            public void onError(Exception error) {
                                if (isAdded())
                                    setSuccessText(tempText);//no devices found
                            }
                        }, 0, null);
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (isAdded())
                        setErrorText(mDomoticz.getErrorMessage(error));
                }
            });
        }
    }

    private void setErrorText(String errorMessage) {
        try {
            tempText = tempText + "\n";
            tempText = tempText + errorMessage;
            tempText = tempText + "\n\n";
            tempText = tempText + getString(R.string.welcome_msg_correctOnPreviousPage);
            disableFinishButton();
            setResultText(tempText);

            SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
            mSharedPrefs.setWelcomeWizardSuccess(false);
            tempText = "";
        } catch (Exception ex) {
        }
    }

    private void setSuccessText(String message) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
        mSharedPrefs.setWelcomeWizardSuccess(true);
        enableFinishButton();
        setResultText(message);
        tempText = "";
    }

    private void setResultText(String text) {
        if (please_wait_layout != null)
            please_wait_layout.setVisibility(View.GONE);
        if (result_layout != null)
            result_layout.setVisibility(View.VISIBLE);
        if (result != null)
            result.setText(text);
    }

    private void resetLayout() {
        if (please_wait_layout != null)
            please_wait_layout.setVisibility(View.VISIBLE);
        if (result_layout != null)
            result_layout.setVisibility(View.GONE);
        if (result != null)
            result.setText("");
    }

    private void disableFinishButton() {
        WelcomeViewActivity activity = (WelcomeViewActivity) getActivity();
        activity.disableFinishButton(true);
    }

    private void enableFinishButton() {
        WelcomeViewActivity activity = (WelcomeViewActivity) getActivity();
        activity.disableFinishButton(false);
    }
}


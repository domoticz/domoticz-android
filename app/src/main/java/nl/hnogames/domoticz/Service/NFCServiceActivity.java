/**
 * Copyright (C) 2015 Domoticz
 * <p/>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package nl.hnogames.domoticz.Service;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;


public class NFCServiceActivity extends AppCompatActivity {

    private Domoticz domoticz;
    private String TAG = NFCSettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);

        if (mSharedPrefs.isNFCEnabled()) {
            ArrayList<NFCInfo> nfcList = mSharedPrefs.getNFCList();
            //if (getIntent().getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            NFCInfo foundNFC = null;
            final String tagID = UsefulBits.ByteArrayToHexString(getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Log.i(TAG, "NFC ID Found: " + tagID);

            if (nfcList != null && nfcList.size() > 0) {
                for (NFCInfo n : nfcList) {
                    if (n.getId().equals(tagID))
                        foundNFC = n;
                }
            }
            if (foundNFC != null && foundNFC.isEnabled()) {
                handleSwitch(foundNFC.getSwitchIdx(), foundNFC.getSwitchPassword(), -1, foundNFC.getValue());
            } else {
                finish();
            }
            //}
        } else {
            finish();
        }
    }

    private void handleSwitch(final int idx, final String password, final int inputJSONAction, final String value) {
        if (domoticz == null)
            domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                int jsonAction;
                int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                int jsonValue = 0;

                if (inputJSONAction < 0) {
                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                        if (!mDevicesInfo.getStatusBoolean())
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        }
                    } else {
                        if (!mDevicesInfo.getStatusBoolean()) {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        } else
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                    }
                } else {
                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                        if (inputJSONAction == 1)
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        }
                    } else {
                        if (inputJSONAction == 1) {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        } else
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                    }
                }

                switch (mDevicesInfo.getSwitchTypeVal()) {
                    case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                        break;
                    case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        break;
                }

                domoticz.setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    @DebugLog
                    public void onReceiveResult(String result) {
                        Log.d(TAG, result);
                        finish();
                    }

                    @Override
                    @DebugLog
                    public void onError(Exception error) {
                        Log.d(TAG, error.getMessage());
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                Log.d(TAG, error.getMessage());
                finish();
            }

        }, idx, false);
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            String[] levelNames = mDevicesInfo.getLevelNames();
            int counter = 10;
            for (String l : levelNames) {
                if (l.equals(value))
                    break;
                else
                    counter += 10;
            }
            jsonValue = counter;
        }
        return jsonValue;
    }

    private void onErrorHandling(Exception error) {
        if (error != null) {
            Toast.makeText(
                    this,
                    "Domoticz: " +
                            getString(R.string.unable_to_get_switches),
                    Toast.LENGTH_SHORT).show();

            if (domoticz != null && UsefulBits.isEmpty(domoticz.getErrorMessage(error)))
                Log.e(TAG, domoticz.getErrorMessage(error));
        }
    }
}
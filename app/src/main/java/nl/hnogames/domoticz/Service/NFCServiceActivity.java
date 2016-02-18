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

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class NFCServiceActivity extends Activity {

    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private ArrayList<NFCInfo> nfcList;
    private String TAG = NFCSettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = new SharedPrefUtil(this);
        nfcList = mSharedPrefs.getNFCList();

        if (getIntent().getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            NFCInfo foundNFC = null;
            final String tagID = UsefulBits.ByteArrayToHexString(getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Log.i(TAG, "NFC ID Found: " + tagID);

            if (nfcList != null && nfcList.size() > 0) {
                for (NFCInfo n : nfcList) {
                    if (n.getId().equals(tagID))
                        foundNFC = n;
                }
            }

            if (foundNFC != null) {
                //toggle switch??
                handleSwitch(foundNFC.getSwitchIdx(), foundNFC.getSwitchPassword());
                finish();
            }
        }
    }

    private void handleSwitch(final int idx, final String password) {
        domoticz = new Domoticz(this, null);
        domoticz.getSwitches(new SwitchesReceiver() {
                                 @Override
                                 public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                                     for (SwitchInfo s : switches) {
                                         if (s.getIdx() == idx) {
                                             domoticz.getStatus(idx, new StatusReceiver() {
                                                 @Override
                                                 public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {

                                                     int jsonAction;
                                                     int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
                                                     if (extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                                                             extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                                                         if (!extendedStatusInfo.getStatusBoolean())
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                     } else {
                                                         if (!extendedStatusInfo.getStatusBoolean())
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                     }

                                                     domoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                                                         @Override
                                                         public void onReceiveResult(String result) {
                                                             Log.d(TAG, result);
                                                         }

                                                         @Override
                                                         public void onError(Exception error) {
                                                             if (error != null)
                                                                 onErrorHandling(error);
                                                         }
                                                     });
                                                 }

                                                 @Override
                                                 public void onError(Exception error) {
                                                     if (error != null)
                                                         onErrorHandling(error);
                                                 }
                                             });
                                         }
                                     }
                                 }

                                 @Override
                                 public void onError(Exception error) {
                                     if (error != null)
                                         onErrorHandling(error);
                                 }
                             }
        );
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
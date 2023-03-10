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

package nl.hnogames.domoticz.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.MultiSelectListPreference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class AutoMultiSelectListPreference extends MultiSelectListPreference {
    private static final String TAG = AutoMultiSelectListPreference.class.getName();
    private final boolean selectAllValuesByDefault;
    private CharSequence[] mEntryValues;

    public AutoMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomListPreference);
        selectAllValuesByDefault = typedArray.getBoolean(R.styleable.CustomListPreference_selectAllValuesByDefault, false);
        typedArray.recycle();
        initSwitches();
    }

    private void initSwitches() {
        StaticHelper.getDomoticz(getContext()).getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
                for (DevicesInfo mExtendedStatusInfo : mDevicesInfo) {
                    String name = mExtendedStatusInfo.getName();
                    if (!name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                        supportedSwitches.add(mExtendedStatusInfo);
                    }
                }
                if (supportedSwitches.size() > 0)
                    processSwitches(supportedSwitches);
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            public void onError(Exception error) {
                if (error != null && error.getMessage() != null && error.getMessage().length() > 0)
                    Log.e(TAG, error.getMessage());
            }
        }, 0, "all");
    }

    private void processSwitches(ArrayList<DevicesInfo> switches) {
        CharSequence[] mEntries = getEntries();
        mEntryValues = getEntryValues();

        if (switches != null) {
            List<String> entries = new ArrayList<>();
            List<String> entryValues = new ArrayList<>();

            for (DevicesInfo s : switches) {
                entryValues.add(s.getIdx() + "");
                entries.add(s.getIdx() + " - " + s.getName());
            }

            if (entries != null && entryValues != null && !entries.isEmpty() && !entryValues.isEmpty()) {
                CharSequence[] dynamicEntries = entries.toArray(new CharSequence[entries.size()]);
                CharSequence[] dynamicEntryValues = entryValues.toArray(new CharSequence[entryValues.size()]);

                //if either of the android attributes for specifying the entries and their values have been left empty, then ignore both and use only the dynamic providers
                if (mEntries == null || mEntryValues == null) {
                    mEntries = dynamicEntries;
                    mEntryValues = dynamicEntryValues;
                } else {
                    CharSequence[] fullEntriesList = new CharSequence[mEntries.length + dynamicEntries.length];
                    CharSequence[] fullEntryValuesList = new CharSequence[mEntryValues.length + dynamicEntryValues.length];

                    int i, j;
                    for (i = 0; i <= mEntries.length - 1; i++) {
                        fullEntriesList[i] = mEntries[i];
                        fullEntryValuesList[i] = mEntryValues[i];
                    }

                    try {
                        for (i = mEntries.length, j = 0; j <= dynamicEntries.length - 1; i++, j++) {
                            fullEntriesList[i] = dynamicEntries[j];
                            fullEntryValuesList[i] = dynamicEntryValues[j];
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }

                    //replace the entries and entryValues arrays with the new lists
                    mEntries = fullEntriesList;
                    mEntryValues = fullEntryValuesList;

                    setEntries(mEntries);
                    setEntryValues(mEntryValues);
                }
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (!restoreValue && selectAllValuesByDefault && mEntryValues != null) {
            final Set<String> result = new HashSet<>();

            for (CharSequence mEntryValue : mEntryValues) {
                result.add(mEntryValue.toString());
            }
            setValues(result);
            return;
        }
        super.onSetInitialValue(restoreValue, defaultValue);
    }
}
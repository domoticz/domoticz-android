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

package nl.hnogames.domoticz.interfaces;

import android.view.View;

import nl.hnogames.domoticzapi.Containers.DevicesInfo;

public interface switchesClickListener {

    void onSwitchClick(DevicesInfo device, boolean action);

    void onBlindClick(DevicesInfo device, int action);

    void onDimmerChange(DevicesInfo device, int value, boolean selector);

    void onButtonClick(DevicesInfo device, boolean action);

    void onLogButtonClick(DevicesInfo device);

    void onLikeButtonClick(DevicesInfo device, boolean checked);

    void onColorButtonClick(DevicesInfo device);

    void onTimerButtonClick(DevicesInfo device);

    void onNotificationButtonClick(DevicesInfo device);

    void onThermostatClick(DevicesInfo device);

    void onSetTemperatureClick(DevicesInfo device);

    void onSecurityPanelButtonClick(DevicesInfo device);

    void onStateButtonClick(DevicesInfo device, int itemsRes, int[] itemIds);

    void onSelectorDimmerClick(DevicesInfo device, String[] levelNames);

    void onSelectorChange(DevicesInfo device, int l);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(DevicesInfo device);

    void onCameraFullScreenClick(DevicesInfo device, String name);

    void OnModeChanged(DevicesInfo utility, int id, String mode);
}
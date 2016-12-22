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

package nl.hnogames.domoticz.Interfaces;

import android.view.View;

public interface switchesClickListener {

    void onSwitchClick(int idx, boolean action);

    void onBlindClick(int idx, int action);

    void onDimmerChange(int idx, int value, boolean selector);

    void onButtonClick(int idx, boolean action);

    void onLogButtonClick(int idx);

    void onLikeButtonClick(int idx, boolean checked);

    void onColorButtonClick(int idx);

    void onTimerButtonClick(int idx);

    void onNotificationButtonClick(int idx);

    void onThermostatClick(int idx);

    void onSetTemperatureClick(int idx);

    void onSecurityPanelButtonClick(int idx);

    void onStateButtonClick(int idx, int itemsRes, int[] itemIds);

    void onSelectorDimmerClick(int idx, String[] levelNames);

    void onSelectorChange(int idx, int l);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);
}
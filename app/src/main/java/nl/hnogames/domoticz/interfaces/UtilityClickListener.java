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

import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;

public interface UtilityClickListener {
    void onClick(UtilitiesInfo utility);

    void OnModeChanged(UtilitiesInfo utility, int id, String mode);

    void onLogClick(UtilitiesInfo utility, String range);

    void onThermostatClick(int idx);

    void onLogButtonClick(int idx);

    void onLikeButtonClick(int idx, boolean checked);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);
}
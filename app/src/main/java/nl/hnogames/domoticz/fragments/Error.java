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

package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;

public class Error extends DomoticzRecyclerFragment implements DomoticzFragmentListener {
    @Override
    public void onConnectionFailed() {
    }

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override

    public void onConnectionOk() {
        super.showSpinner(true);
        if (getActivity() instanceof MainActivity) {
            if (((MainActivity) getActivity()).configException != null)
                errorHandling(((MainActivity) getActivity()).configException);
        }
    }

    @Override

    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                super.errorHandling(error);
            }
        }
    }
}
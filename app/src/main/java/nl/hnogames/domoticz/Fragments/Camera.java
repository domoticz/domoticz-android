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

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.MjpegViewer.MjpegView;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;

public class Camera extends Fragment {

    private nl.hnogames.domoticz.UI.MjpegViewer.MjpegView root;
    private String name = "";
    private SharedPrefUtil mSharedPrefs;

    private Domoticz mDomoticz;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        RelativeLayout group = (RelativeLayout) inflater.inflate(R.layout.camera_default, null);
        if (mSharedPrefs.darkThemeEnabled())
            group.findViewById(R.id.row_global_wrapper).setBackgroundColor(getResources().getColor(R.color.background_dark));
        root = (MjpegView) group.findViewById(R.id.image);

        mDomoticz = new Domoticz(getActivity(), AppController.getInstance().getRequestQueue());
        mDomoticz.getCameras(new CameraReceiver() {
            @Override
            public void OnReceiveCameras(ArrayList<CameraInfo> Cameras) {

                for (CameraInfo c : Cameras) {
                    if (c.getName().equals(name)) {
                        root.Start(c.getTotalImageURL());
                    }
                }
            }

            @Override
            public void onError(Exception error) {
            }
        });
        return group;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSharedPrefs = new SharedPrefUtil(context);
    }

    public void setImage(String image) {
        this.name = image;

    }
}
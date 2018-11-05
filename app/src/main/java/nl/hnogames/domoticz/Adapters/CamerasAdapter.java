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

package nl.hnogames.domoticz.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Utils.RequestUtil;

@SuppressWarnings("unused")
public class CamerasAdapter extends RecyclerView.Adapter<CamerasAdapter.DataObjectHolder> {
    private static onClickListener onClickListener;
    private final Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private ArrayList<CameraInfo> mDataset;
    private Domoticz domoticz;
    private boolean refreshTimer;

    public CamerasAdapter(ArrayList<CameraInfo> data, Context mContext, Domoticz domoticz, boolean refreshTimer) {
        setData(data);
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
        this.refreshTimer = refreshTimer;
        this.domoticz = domoticz;
    }

    public void setData(ArrayList<CameraInfo> data) {
        this.mDataset = data;
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        CamerasAdapter.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.camera_row, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((view.findViewById(R.id.card_global_wrapper)) != null)
                view.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(mContext, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background_dark));
        }
        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataObjectHolder holder, int position) {
        if (mDataset != null && mDataset.size() > 0) {
            CameraInfo cameraInfo = mDataset.get(position);
            String name = cameraInfo.getName();
            String address = cameraInfo.getAddress();
            String imageUrl = cameraInfo.getSnapShotURL();

            int numberOfDevices = cameraInfo.getDevices();
            String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);
            holder.name.setText(name);

            ImageLoader imageLoader = RequestUtil.getImageLoader(domoticz,
                domoticz.getUserCredentials(Domoticz.Authentication.USERNAME),
                domoticz.getUserCredentials(Domoticz.Authentication.USERNAME),
                domoticz.getSessionUtil(),
                true,
                mContext);
            holder.camera.setImageUrl(imageUrl, imageLoader);

            if (!refreshTimer)
                holder.camera.setDefaultImageResId(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setRefreshTimer(boolean timer) {
        this.refreshTimer = timer;
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        TextView name;
        com.android.volley.toolbox.NetworkImageView camera;

        public DataObjectHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            camera = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onItemClick(getLayoutPosition(), v);
        }
    }
}
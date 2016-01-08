/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.hnogames.domoticz.Containers.CameraInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.BitmapLruCache;
import nl.hnogames.domoticz.Utils.SessionUtil;


@SuppressWarnings("unused")
public class CamerasAdapter extends RecyclerView.Adapter<CamerasAdapter.DataObjectHolder> {
    private static onClickListener onClickListener;
    private final Context mContext;
    private ArrayList<CameraInfo> mDataset;
    private Domoticz domoticz;

    public CamerasAdapter(ArrayList<CameraInfo> data, Context mContext) {
        this.mDataset = data;
        this.mContext = mContext;
        this.domoticz = new Domoticz(mContext);
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        CamerasAdapter.onClickListener = onClickListener;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.camera_row, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        if (mDataset != null && mDataset.size() > 0) {
            CameraInfo cameraInfo = mDataset.get(position);
            String name = cameraInfo.getName();
            String address = cameraInfo.getAddress();
            String imageUrl = domoticz.getSnapshotUrl(cameraInfo);

            int numberOfDevices = cameraInfo.getDevices();
            String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);
            holder.name.setText(name);

            final SessionUtil sessionUtil = new SessionUtil(mContext);
            ImageLoader.ImageCache imageCache = new BitmapLruCache();
            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(mContext), imageCache){
                @Override
                protected com.android.volley.Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight,
                                                                              ImageView.ScaleType scaleType, final String cacheKey) {
                    return new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            onGetImageSuccess(cacheKey, response);
                        }
                    }, maxWidth, maxHeight,
                            Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            onGetImageError(cacheKey, error);
                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = super.getHeaders();

                            if (headers == null
                                    || headers.equals(Collections.emptyMap())) {
                                headers = new HashMap<String, String>();
                            }

                            String credentials = domoticz.getUserCredentials(Domoticz.Authentication.USERNAME) + ":" + domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD);
                            String base64EncodedCredentials =
                                    Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                            headers.put("Authorization", "Basic " + base64EncodedCredentials);
                            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                            headers.put("Accept-Language", "en-US,en;q=0.7,nl;q=0.3");
                            headers.put("Accept-Encoding", "gzip, deflate");

                            sessionUtil.addSessionCookie(headers);
                            return headers;
                        }

                    };
                }
            };

            holder.camera.setImageUrl(imageUrl, imageLoader);
            holder.camera.setDefaultImageResId(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
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
            name = (TextView) itemView.findViewById(R.id.name);
            camera = (com.android.volley.toolbox.NetworkImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onItemClick(getLayoutPosition(), v);
        }
    }
}
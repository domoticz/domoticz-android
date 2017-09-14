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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class Camera extends Fragment {

    private ImageView root;
    private String url = "";
    private SharedPrefUtil mSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout group = (RelativeLayout) inflater.inflate(R.layout.camera_default, null);
        if (mSharedPrefs.darkThemeEnabled()) {
            group.findViewById(R.id.row_global_wrapper).setBackgroundColor(getResources().getColor(R.color.background_dark));
        }
        root = (ImageView) group.findViewById(R.id.image);
        FloatingActionButton fabButton = (FloatingActionButton) group.findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage();
            }
        });

        if (this.url.length() > 0)
            setImage(this.url);
        return group;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSharedPrefs = new SharedPrefUtil(context);
    }

    private void processImage() {
        // Get access to the URI for the bitmap
        File file = new File(url);
        Uri bmpUri = Uri.fromFile(file);
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }

    public void setImage(String url) {
        this.url = url;
        if (root != null && !root.equals(null)) {
            File file = new File(url);
            Uri uri = Uri.fromFile(file);
            Picasso.with(getActivity())
                .load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(root);
        }
    }
}
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

package nl.hnogames.domoticz.Adapter;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.R;

public class ListAdapter extends WearableListView.Adapter {
    private ArrayList<ExtendedStatusInfo> mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(Context context, ArrayList<ExtendedStatusInfo> dataset) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setData(ArrayList<ExtendedStatusInfo> dataset)
    {

        mDataset = dataset;
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        private TextView statusView;
        private ImageView imageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            textView = (TextView) itemView.findViewById(R.id.name);
            statusView = (TextView) itemView.findViewById(R.id.status);
            imageView = (ImageView) itemView.findViewById(R.id.circle);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        TextView status = itemHolder.statusView;

        view.setText(mDataset.get(position).getName());
        status.setText(mDataset.get(position).getData());

        String imageType = mDataset.get(position).getTypeImg();
        if(imageType!=null && imageType.length()>0) {
            Picasso.with(this.mContext).load(
                    getDrawableIcon(mDataset.get(position).getTypeImg(),
                            mDataset.get(position).getType(),
                            mDataset.get(position).getSwitchType(),
                            mDataset.get(position).getStatusBoolean(),
                            mDataset.get(position).getUseCustomImage(),
                            mDataset.get(position).getImage()))
                    .into(itemHolder.imageView);

            if (!mDataset.get(position).getStatusBoolean())
                itemHolder.imageView.setAlpha(0.5f);
            else
                itemHolder.imageView.setAlpha(1f);
        }

        // replace list item's metadata
        holder.itemView.setTag(position);
    }


    public int getDrawableIcon(String imgType, String Type , String switchType, boolean State, boolean useCustomImage, String CustomImage) {
        int standardImage = getDrawableIcon(imgType, Type, switchType, State);

        if(useCustomImage && CustomImage!=null && CustomImage.length()>0)
        {
            switch (CustomImage) {
                case "Alarm":
                    return R.drawable.alarm;
                case "Amplifier":
                    return R.drawable.volume;
                case "Computer":
                case "ComputerPC":
                    return R.drawable.computer;
                case "Cooling":
                    return R.drawable.cooling;
                case "ChristmasTree":
                    return R.drawable.christmastree;
                case "Fan":
                    return R.drawable.wind;
                case "Fireplace":
                    return R.drawable.flame;
                case "Generic":
                    return R.drawable.generic;
                case "Harddisk":
                    return R.drawable.harddisk;
                case "Heating":
                    return R.drawable.heating;
                case "Light":
                    return R.drawable.lights;
                case "Media":
                    return R.drawable.video;
                case "Phone":
                    return R.drawable.phone;
                case "Speaker":
                    return R.drawable.sub;
                case "Printer":
                    return R.drawable.printer;
                case "TV":
                    return R.drawable.tv;
                case "WallSocket":
                    return R.drawable.wall;
                case "Water":
                    return R.drawable.water;
            }
        }

        return standardImage;
    }

    private int getDrawableIcon(String imgType, String Type , String switchType, boolean State) {
        int test = R.drawable.defaultimage;
        switch (imgType.toLowerCase()) {
            case "scene":
                return R.drawable.generic;
            case "group":
                return R.drawable.generic;
            case "wind":
                return R.drawable.wind;
            case "doorbell":
                return R.drawable.door;
            case "door":
                return R.drawable.door;
            case "lightbulb":
                if (switchType != null && switchType.length() > 0 && switchType.equals("Dusk Sensor"))
                    if (State)
                        return R.drawable.uvdark;
                    else
                        return R.drawable.uvsunny;
                else
                    return R.drawable.lights;
            case "push":
                return R.drawable.pushoff;
            case "pushoff":
                return R.drawable.pushoff;
            case "siren":
                return R.drawable.siren;
            case "smoke":
                return R.drawable.smoke;
            case "uv":
                return R.drawable.uv;
            case "contact":
                return R.drawable.contact;
            case "logitechMediaServer":
                return R.drawable.media;
            case "media":
                return R.drawable.media;
            case "blinds":
                return R.drawable.down;
            case "dimmer":
                if (switchType != null && switchType.length() > 0 && switchType.startsWith("RGB"))
                    return R.drawable.rgb;
                else
                    return R.drawable.dimmer;
            case "motion":
                return R.drawable.motion;
            case "security":
                return R.drawable.security;
            case "temperature":
                if (State)
                    return R.drawable.heating;
                else
                    return R.drawable.cooling;
            case "counter":
                if (Type != null && Type.length() > 0 && Type.equals("P1 Smart Meter"))
                    return R.drawable.wall;
                else
                    return R.drawable.up;
            case "visibility":
                return R.drawable.visibility;
            case "radiation":
                return R.drawable.radiation;
            case "moisture":
            case "rain":
                return R.drawable.rain;
            case "leaf":
                return R.drawable.leaf;
            case "hardware":
                return R.drawable.computer;
            case "fan":
                return R.drawable.fan;
            case "speaker":
                return R.drawable.speaker;
            case "current":
                return R.drawable.wall;
            case "text":
                return R.drawable.text;
            case "alert":
                return R.drawable.siren;
            case "gauge":
                return R.drawable.gauge;
            case "clock":
                return R.drawable.clock48;
            case "mode":
                return R.drawable.defaultimage;
            case "utility":
                return R.drawable.scale;
        }

        switch (Type.toLowerCase()) {
            case "thermostat":
                return R.drawable.flame;
        }

        return test;
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
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
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;

public class ListAdapter extends WearableListView.Adapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private ArrayList<DevicesInfo> mDataset;
    private Domoticz mDomoticz;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(Context context, ArrayList<DevicesInfo> dataset) {
        mContext = context;
        mDomoticz = new Domoticz();
        mInflater = LayoutInflater.from(context);
        setData(dataset);
    }

    public void setData(ArrayList<DevicesInfo> dataset) {
        mDataset = dataset;
        Collections.sort(dataset, new Comparator<DevicesInfo>() {
            @Override
            public int compare(DevicesInfo left, DevicesInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
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

        DevicesInfo mDeviceInfo = mDataset.get(position);
        view.setText(mDeviceInfo.getName());
        status.setText(mDeviceInfo.getData());

        if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0)
            status.setText(mContext.getString(R.string.usage) + ": " + mDeviceInfo.getUsage());
        if (mDeviceInfo.getCounterToday() != null && mDeviceInfo.getCounterToday().length() > 0)
            status.append(" " + mContext.getString(R.string.today) + ": " + mDeviceInfo.getCounterToday());
        if (mDeviceInfo.getCounter() != null && mDeviceInfo.getCounter().length() > 0 &&
                !mDeviceInfo.getCounter().equals(mDeviceInfo.getData()))
            status.append(" " + mContext.getString(R.string.total) + ": " + mDeviceInfo.getCounter());

        String imageType = mDataset.get(position).getTypeImg();
        if (imageType != null && imageType.length() > 0) {
            Picasso.with(this.mContext).load(
                    mDomoticz.getDrawableIcon(mDataset.get(position).getTypeImg(),
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

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
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
}
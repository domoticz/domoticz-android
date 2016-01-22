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

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Interfaces.LocationClickListener;
import nl.hnogames.domoticz.R;

public class LocationAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = LocationAdapter.class.getSimpleName();
    public ArrayList<LocationInfo> data = null;
    private Context context;

    private LocationClickListener listener;

    public LocationAdapter(Context context,
                           ArrayList<LocationInfo> data,
                           LocationClickListener l) {
        super();

        this.context = context;
        this.data = data;
        this.listener = l;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        int layoutResourceId;

        final LocationInfo mLocationInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.geo_row_location;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.enable = (CheckBox) convertView.findViewById(R.id.enableSwitch);
        holder.name = (TextView) convertView.findViewById(R.id.location_name);
        holder.radius = (TextView) convertView.findViewById(R.id.location_radius);
        holder.country = (TextView) convertView.findViewById(R.id.location_country);
        holder.address = (TextView) convertView.findViewById(R.id.location_address);
        holder.connectedSwitch = (TextView) convertView.findViewById(R.id.location_connectedSwitch);
        holder.remove = (Button) convertView.findViewById(R.id.remove_button);

        if (mLocationInfo.getAddress() != null) {
            Address address = mLocationInfo.getAddress();

            String addressString;
            String countryString;

            if (address != null) {
                addressString = address.getAddressLine(0) + ", " + address.getLocality();
                countryString = address.getCountryName();
            } else {
                addressString = context.getString(R.string.unknown);
                countryString = context.getString(R.string.unknown);
            }
            holder.address.setText(addressString);
            holder.country.setText(countryString);
        }

        holder.name.setText(mLocationInfo.getName());
        String text = context.getString(R.string.radius) + ": " + mLocationInfo.getRadius();
        holder.radius.setText(text);

        if (mLocationInfo.getSwitchidx() > 0) {
            text = context.getString(R.string.connectedSwitch) + ": " + mLocationInfo.getSwitchidx();
            holder.connectedSwitch.setText(text);
        } else {
            text = context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available);
            holder.connectedSwitch.setText(text);
        }

        holder.remove.setId(mLocationInfo.getID());
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LocationInfo removeLocation = null;
                for (LocationInfo l : data) {
                    if (l.getID() == v.getId()){
                        removeLocation=l;
                    }
                }
                if(removeLocation != null)
                    handleRemoveButtonClick(removeLocation);
            }
        });

        holder.enable.setId(mLocationInfo.getID());
        holder.enable.setChecked(mLocationInfo.getEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (LocationInfo locationInfo : data) {
                    if (locationInfo.getID() == buttonView.getId()) {
                        if (!handleEnableChanged(locationInfo, holder.enable.isChecked())) {
                            buttonView.setChecked(false);
                        } else {
                            buttonView.setChecked(true);
                        }
                        break;
                    }
                }
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(LocationInfo removeLocation) {
        listener.onRemoveClick(removeLocation);
    }

    private boolean handleEnableChanged(LocationInfo location, boolean enabled) {
        return listener.onEnableClick(location, enabled);
    }

    static class ViewHolder {
        TextView name;
        TextView address;
        TextView radius;
        TextView country;
        TextView connectedSwitch;
        CheckBox enable;
        Button remove;
    }
}
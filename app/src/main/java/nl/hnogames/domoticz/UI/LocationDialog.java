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

package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import java.util.Random;

import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.GeoUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class LocationDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private final GeoUtil mGeoUtil;
    private Context mContext;
    private Address foundLocation;
    private FloatingLabelEditText editAddress;
    private FloatingLabelEditText editName;
    private FloatingLabelEditText editLatitude;
    private FloatingLabelEditText editLongitude;
    private TextView resolvedAddress;
    private TextView resolvedCountry;
    private DismissListener dismissListener;
    private Location currentLocation;
    private LocationInfo locationToEdit;
    private FloatingLabelEditText radiusText;
    private int radius;
    private LatLng mLatLong;

    @SuppressWarnings("FieldCanBeLocal")
    private int radiusDefaultValue = 120;
    private Button editModeButton;
    private LinearLayout layout_latLong;
    private String title;

    public LocationDialog(final Context mContext, int layout) {
        this.mContext = mContext;

        mGeoUtil = new GeoUtil(mContext);

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        //noinspection ConstantConditions
        mdb.customView(layout, wrapInScrollView)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel);

        mdb.dismissListener(this);
        mdb.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog,
                                @NonNull DialogAction dialogAction) {

                if (dismissListener != null && valuesAreValid()) {
                    if (locationToEdit != null) {
                        // In edit mode
                        locationToEdit.setName(getEditName());
                        if (mLatLong != null) locationToEdit.setLocation(mLatLong);
                        locationToEdit.setRadius(radius);

                        dismissListener.onDismiss(locationToEdit);

                    } else {
                        // In add mode
                        dismissListener.onDismiss(
                                new LocationInfo(new Random().nextInt(999999),
                                        getEditName(),
                                        mLatLong,
                                        radius));
                    }
                } else
                    Toast.makeText(mContext,
                        R.string.location_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean valuesAreValid() {

        if (!UsefulBits.isEmpty(radiusText.getInputWidgetText().toString())) {
            try {
                radius = Integer.parseInt(radiusText.getInputWidgetText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                radius = radiusDefaultValue;
            }
        } else radius = radiusDefaultValue;

        String name = getEditName();

        if (UsefulBits.isEmpty(name)) return false;

        if (foundLocation != null) {
            mLatLong = new LatLng(foundLocation.getLatitude(), foundLocation.getLongitude());
            return true;
        } else if (currentLocation != null) {
            mLatLong = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            return true;
        } else if (locationToEdit != null) {
            mLatLong = new LatLng(
                    locationToEdit.getLocation().latitude,
                    locationToEdit.getLocation().longitude);
            return true;
        }

        return false;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setLocationToEdit(LocationInfo locationToEdit) {
        this.locationToEdit = locationToEdit;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void show() {

        if (UsefulBits.isEmpty(title)) mdb.title(R.string.title_add_location);
        else mdb.title(title);

        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        Button getLocation = (Button) view.findViewById(R.id.get_address);

        initViews(view);

        if (radius <= 0) radius = radiusDefaultValue;
        setRadiusText(String.valueOf(radius));

        if (currentLocation != null) {
            // Adding a new location
            setAddressName(mContext.getString(R.string.currentLocation));

            Address currentAddress = mGeoUtil.getAddressFromLocation(currentLocation);
            if (currentAddress != null)
                setAddressData(currentAddress);
            else {
                resolvedAddress.setText(R.string.unknown);
                resolvedCountry.setText(R.string.unknown);
            }
        } else if (locationToEdit != null) {
            // Editing a location
            setAddressName(locationToEdit.getName());

            Address addressToEdit = mGeoUtil.getAddressFromLocationInfo(locationToEdit);
            if (addressToEdit != null)
                setAddressData(addressToEdit);
            else {
                resolvedAddress.setText(R.string.unknown);
                resolvedCountry.setText(R.string.unknown);
            }

        }

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inManualMode()) {
                    foundLocation = mGeoUtil.getAddressFromString(
                            String.valueOf(editAddress.getInputWidgetText().toString()));
                    if (foundLocation == null)
                        Toast.makeText(mContext,
                                R.string.could_not_find_location,
                                Toast.LENGTH_SHORT).show();
                    else setAddressData(foundLocation);
                } else {
                    try {
                        double latitude =
                                Double.valueOf(editLatitude.getInputWidgetText().toString());
                        double longitude =
                                Double.valueOf(editLongitude.getInputWidgetText().toString());
                        LatLng mLatLng = new LatLng(latitude, longitude);
                        foundLocation = mGeoUtil.getAddressFromLatLng(mLatLng);
                        if (foundLocation == null)
                            Toast.makeText(mContext,
                                    R.string.could_not_find_location,
                                    Toast.LENGTH_SHORT).show();
                        else setAddressData(foundLocation);

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext,
                                R.string.no_valid_latLong,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        editModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inManualMode()) setEditModeToManual(false);
                else setEditModeToManual(true);
            }
        });
        md.show();
    }

    private String getEditName() {
        return editName.getInputWidgetText().toString();
    }

    private void setAddressName(String name) {
        editName.setInputWidgetText(name);
    }

    private void setAddressData(Address foundLocation) {
        String address = foundLocation.getAddressLine(0) + ", " + foundLocation.getLocality();
        resolvedAddress.setText(address);
        resolvedCountry.setText(foundLocation.getCountryName());
    }

    private void setRadiusText(String radius) {
        radiusText.setInputWidgetText(radius);
    }

    private boolean inManualMode() {
        return layout_latLong.getVisibility() == View.GONE;
    }

    private void setEditModeToManual(boolean manualMode) {
        if (manualMode) {
            layout_latLong.setVisibility(View.GONE);
            editAddress.setVisibility(View.VISIBLE);
            editModeButton.setText(R.string.manual);
        } else {
            layout_latLong.setVisibility(View.VISIBLE);
            editAddress.setVisibility(View.GONE);
            editModeButton.setText(R.string.locationAddress);
        }
    }

    public void initViews(View view) {
        radiusText = (FloatingLabelEditText) view.findViewById(R.id.radius);

        resolvedAddress = (TextView) view.findViewById(R.id.resolvedAddress);
        resolvedCountry = (TextView) view.findViewById(R.id.resolvedCountry);

        editAddress = (FloatingLabelEditText) view.findViewById(R.id.address);
        editName = (FloatingLabelEditText) view.findViewById(R.id.name);

        editModeButton = (Button) view.findViewById(R.id.edit_mode_button);
        layout_latLong = (LinearLayout) view.findViewById(R.id.layout_latLong);

        editLatitude = (FloatingLabelEditText) view.findViewById(R.id.latitude);
        editLongitude = (FloatingLabelEditText) view.findViewById(R.id.longitude);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dismissListener != null)
            dismissListener.onDismissEmpty();
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(LocationInfo location);

        void onDismissEmpty();
    }
}
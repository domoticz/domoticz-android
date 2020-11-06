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

package nl.hnogames.domoticz.adapters;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ftinc.scoop.Flavor;
import com.ftinc.scoop.R;
import com.ftinc.scoop.Scoop;
import com.ftinc.scoop.util.AttrUtils;
import com.ftinc.scoop.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Project: ThemeEngineTest
 * Package: com.ftinc.themeenginetest.adapter
 * Created by drew.heavner on 6/8/16.
 */

public class FlavorRecyclerAdapter extends RecyclerView.Adapter<FlavorRecyclerAdapter.FlavorViewHolder> {

    /***********************************************************************************************
     *
     * Variables
     *
     */

    private final LayoutInflater mInflater;
    private final List<Flavor> mItems;

    private OnItemClickListener mItemClickListener;
    private Flavor mCurrentFlavor;

    public FlavorRecyclerAdapter(Activity activity) {
        setHasStableIds(true);
        mInflater = activity.getLayoutInflater();
        mItems = new ArrayList<>();
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setCurrentFlavor(Flavor currentFlavor) {
        int previousIndex = -1;
        if (mCurrentFlavor != null) previousIndex = mItems.indexOf(mCurrentFlavor);

        // Set the current flavor
        mCurrentFlavor = currentFlavor;

        // Update old position if existed
        if (previousIndex != -1) {
            notifyItemChanged(previousIndex);
        }

        // Update the new position
        int index = mItems.indexOf(mCurrentFlavor);
        if (index != -1) {
            notifyItemChanged(index);
        }

    }

    public void addAll(List<Flavor> flavors) {
        mItems.clear();
        mItems.addAll(flavors);
        notifyDataSetChanged();
    }

    /***********************************************************************************************
     *
     * Adapter Methods
     *
     */

    @Override
    public FlavorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return FlavorViewHolder.create(mInflater, parent);
    }

    @Override
    public void onBindViewHolder(final FlavorViewHolder holder, final int position) {
        final Flavor item = mItems.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null)
                    mItemClickListener.onItemClicked(v, item, holder.getAdapterPosition());
            }
        });

        holder.bind(item, item.equals(mCurrentFlavor));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        Flavor item = mItems.get(position);
        if (item != null) {
            return item.hashCode();
        }
        return super.getItemId(position);
    }

    /***********************************************************************************************
     *
     * Listeners
     *
     */

    public interface OnItemClickListener {
        void onItemClicked(View view, Flavor item, int position);
    }

    /***********************************************************************************************
     *
     * ViewHolder
     *
     */

    static class FlavorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitle;
        ImageView mPrimary, mDark, mAccent;
        ImageView mIndicator;
        ViewGroup mOptionsGroup;
        com.google.android.material.button.MaterialButton mOptAuto, mOptSystem, mOptOff, mOptOn;

        FlavorViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mPrimary = itemView.findViewById(R.id.primaryColor);
            mDark = itemView.findViewById(R.id.primaryColorDark);
            mAccent = itemView.findViewById(R.id.accentColor);
            mIndicator = itemView.findViewById(R.id.indicator);
            mOptionsGroup = itemView.findViewById(R.id.daynight_options);
            mOptAuto = mOptionsGroup.findViewById(R.id.opt_auto);
            mOptSystem = mOptionsGroup.findViewById(R.id.opt_system);
            mOptOff = mOptionsGroup.findViewById(R.id.opt_off);
            mOptOn = mOptionsGroup.findViewById(R.id.opt_on);
        }

        static FlavorViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.item_layout_flavor, parent, false);
            return new FlavorViewHolder(view);
        }

        void bind(Flavor flavor, boolean isCurrentFlavor) {
            mTitle.setText(flavor.getName());

            // Pull colors
            int primary = AttrUtils.getColorAttr(itemView.getContext(), flavor.getStyleResource(), R.attr.colorPrimary);
            int primaryDark = AttrUtils.getColorAttr(itemView.getContext(), flavor.getStyleResource(), R.attr.colorPrimaryDark);
            int accent = AttrUtils.getColorAttr(itemView.getContext(), flavor.getStyleResource(), R.attr.colorAccent);

            // Set Colors
            mPrimary.setBackground(generateDrawable(primary));
            mDark.setBackground(generateDrawable(primaryDark));
            mAccent.setBackground(generateDrawable(accent));

            // Set indicator visibility
            mIndicator.setVisibility(isCurrentFlavor ? View.VISIBLE : View.GONE);
            mOptionsGroup.setVisibility(flavor.isDayNight() && isCurrentFlavor ? View.VISIBLE : View.GONE);

            // Set the current option as selected
            int mode = Scoop.getInstance().getDayNightMode();
            bindOptions(mode);

        }

        ShapeDrawable generateDrawable(@ColorInt int color) {
            ShapeDrawable d = new ShapeDrawable(new OvalShape());
            d.setIntrinsicWidth(Utils.dipToPx(itemView.getContext(), 24));
            d.setIntrinsicHeight(Utils.dipToPx(itemView.getContext(), 24));
            d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            return d;
        }

        void checkMode(com.google.android.material.button.MaterialButton btn, int mode, int desired) {
            int color = AttrUtils.getColorAttr(itemView.getContext(), mode == desired ? R.attr.colorAccent : android.R.attr.textColorPrimary);
            btn.setTextColor(color);
            btn.setOnClickListener(this);
        }

        void bindOptions(int mode) {
            checkMode(mOptAuto, mode, AppCompatDelegate.MODE_NIGHT_AUTO);
            checkMode(mOptSystem, mode, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            checkMode(mOptOff, mode, AppCompatDelegate.MODE_NIGHT_NO);
            checkMode(mOptOn, mode, AppCompatDelegate.MODE_NIGHT_YES);
        }

        @SuppressWarnings("WrongConstant")
        @Override
        public void onClick(View v) {
            int i = v.getId();
            int mode = 0;
            if (i == R.id.opt_auto) {
                mode = AppCompatDelegate.MODE_NIGHT_AUTO;
            } else if (i == R.id.opt_system) {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (i == R.id.opt_off) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (i == R.id.opt_on) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            AppCompatDelegate.setDefaultNightMode(mode);
            Scoop.getInstance().chooseDayNightMode(mode);
            bindOptions(mode);
        }
    }
}

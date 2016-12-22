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
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.DataObjectHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = SceneAdapter.class.getSimpleName();

    private final ScenesClickListener listener;
    public ArrayList<SceneInfo> filteredData = null;
    private Context context;
    private ArrayList<SceneInfo> data = null;
    private Domoticz domoticz;

    private SharedPrefUtil mSharedPrefs;
    private ItemFilter mFilter = new ItemFilter();

    public SceneAdapter(Context context,
                        Domoticz mDomoticz,
                        ArrayList<SceneInfo> data,
                        ScenesClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;
        setData(data);

        this.listener = listener;
    }

    public void setData(ArrayList<SceneInfo> data) {
        Collections.sort(data, new Comparator<SceneInfo>() {
            @Override
            public int compare(SceneInfo left, SceneInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
        this.data = data;
        this.filteredData = data;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scene_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) view.findViewById(R.id.card_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {

        if (filteredData != null && filteredData.size() > 0) {
            final SceneInfo mSceneInfo = filteredData.get(position);

            if (DomoticzValues.Scene.Type.SCENE.equalsIgnoreCase(mSceneInfo.getType())) {
                holder.isProtected = mSceneInfo.isProtected();

                setButtons(holder, Buttons.SCENE);
                if (mSharedPrefs.darkThemeEnabled()) {
                    if ((holder.itemView.findViewById(R.id.on_button)) != null)
                        (holder.itemView.findViewById(R.id.on_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.off_button)) != null)
                        (holder.itemView.findViewById(R.id.off_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.log_button)) != null)
                        (holder.itemView.findViewById(R.id.log_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.notifications_button)) != null)
                        (holder.itemView.findViewById(R.id.notifications_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.timer_button)) != null)
                        (holder.itemView.findViewById(R.id.timer_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
                }

                if (holder.buttonTimer != null)
                    holder.buttonTimer.setVisibility(View.GONE);
                if (holder.buttonNotifications != null)
                    holder.buttonNotifications.setVisibility(View.GONE);

                holder.switch_name.setText(mSceneInfo.getName());
                String text = context.getString(R.string.last_update)
                        + ": "
                        + UsefulBits.getFormattedDate(context,
                        mSceneInfo.getLastUpdateDateTime().getTime());
                holder.signal_level.setText(text);
                holder.switch_battery_level.setText(DomoticzValues.Scene.Type.SCENE);

                Picasso.with(context).load(DomoticzIcons.getDrawableIcon(
                        DomoticzValues.Scene.Type.SCENE.toLowerCase(),
                        null,
                        null,
                        false,
                        false,
                        null)).into(holder.iconRow);

                if (holder.buttonOn != null) {
                    holder.buttonOn.setId(mSceneInfo.getIdx());
                    //  holder.buttonOn.setText(context.getString(R.string.button_state_on));
                    holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleClick(view.getId(), true);
                        }
                    });
                    if (holder.isProtected) {
                        holder.buttonOn.setEnabled(false);
                    }
                }

                if (holder.likeButton != null) {
                    holder.likeButton.setId(mSceneInfo.getIdx());
                    holder.likeButton.setLiked(mSceneInfo.getFavoriteBoolean());
                    holder.likeButton.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), true);
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), false);
                        }
                    });
                }
                if (holder.buttonLog != null) {
                    holder.buttonLog.setId(mSceneInfo.getIdx());
                    holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleLogButtonClick(v.getId());
                        }
                    });
                }
            } else if (mSceneInfo.getType().equalsIgnoreCase(DomoticzValues.Scene.Type.GROUP)) {
                holder.isProtected = mSceneInfo.isProtected();

                setButtons(holder, Buttons.GROUP);
                if (mSharedPrefs.darkThemeEnabled()) {
                    if ((holder.itemView.findViewById(R.id.on_button)) != null)
                        (holder.itemView.findViewById(R.id.on_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.off_button)) != null)
                        (holder.itemView.findViewById(R.id.off_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.log_button)) != null)
                        (holder.itemView.findViewById(R.id.log_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.notifications_button)) != null)
                        (holder.itemView.findViewById(R.id.notifications_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
                    if ((holder.itemView.findViewById(R.id.timer_button)) != null)
                        (holder.itemView.findViewById(R.id.timer_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
                }

                if (holder.buttonTimer != null)
                    holder.buttonTimer.setVisibility(View.GONE);
                if (holder.buttonNotifications != null)
                    holder.buttonNotifications.setVisibility(View.GONE);

                holder.switch_name.setText(mSceneInfo.getName());

                String text = context.getString(R.string.last_update)
                        + ": "
                        + UsefulBits.getFormattedDate(context,
                        mSceneInfo.getLastUpdateDateTime().getTime());

                holder.signal_level.setText(text);
                holder.switch_battery_level.setText(DomoticzValues.Scene.Type.GROUP);
                if (holder.buttonOn != null) {
                    holder.buttonOn.setId(mSceneInfo.getIdx());
                    holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v.getId(), true);
                        }
                    });
                }

                if (holder.buttonOff != null) {
                    holder.buttonOff.setId(mSceneInfo.getIdx());
                    holder.buttonOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v.getId(), false);
                        }
                    });
                }

                Picasso.with(context).load(DomoticzIcons.getDrawableIcon(
                        DomoticzValues.Scene.Type.GROUP.toLowerCase(),
                        null,
                        null,
                        mSceneInfo.getStatusInBoolean(),
                        false,
                        null)).into(holder.iconRow);

                if (!mSceneInfo.getStatusInBoolean())
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);

                if (holder.likeButton != null) {
                    holder.likeButton.setId(mSceneInfo.getIdx());
                    holder.likeButton.setLiked(mSceneInfo.getFavoriteBoolean());
                    holder.likeButton.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), true);
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), false);
                        }
                    });
                }
                if (holder.buttonLog != null) {
                    holder.buttonLog.setId(mSceneInfo.getIdx());
                    holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleLogButtonClick(v.getId());
                        }
                    });
                }
            } else throw new NullPointerException("Scene type not supported in the adapter for:\n"
                    + mSceneInfo.toString());

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClicked(position);
                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void setButtons(DataObjectHolder holder, int button) {
        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.GONE);
        }
        if (holder.buttonTimer != null) {
            holder.buttonTimer.setVisibility(View.GONE);
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setVisibility(View.GONE);
        }
        if (holder.buttonOn != null) {
            holder.buttonOn.setVisibility(View.GONE);
        }

        switch (button) {
            case Buttons.SCENE:
                holder.buttonOn.setVisibility(View.VISIBLE);
                holder.buttonLog.setVisibility(View.VISIBLE);
                break;
            case Buttons.GROUP:
                holder.buttonOn.setVisibility(View.VISIBLE);
                holder.buttonOff.setVisibility(View.VISIBLE);
                holder.buttonLog.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    public void handleClick(int idx, boolean action) {
        listener.onSceneClick(idx, action);
    }

    interface Buttons {
        int SCENE = 0;
        int GROUP = 1;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView switch_name, signal_level, switch_battery_level;
        Boolean isProtected;
        ImageView iconRow;
        LikeButton likeButton;
        LinearLayout extraPanel;
        Button buttonOn, buttonLog, buttonTimer, buttonNotifications, buttonOff;

        public DataObjectHolder(View itemView) {
            super(itemView);

            buttonOn = (Button) itemView.findViewById(R.id.on_button);
            signal_level = (TextView) itemView.findViewById(R.id.switch_signal_level);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            switch_name = (TextView) itemView.findViewById(R.id.switch_name);
            switch_battery_level = (TextView) itemView.findViewById(R.id.switch_battery_level);

            buttonLog = (Button) itemView.findViewById(R.id.log_button);
            buttonTimer = (Button) itemView.findViewById(R.id.timer_button);
            buttonNotifications = (Button) itemView.findViewById(R.id.notifications_button);
            likeButton = (LikeButton) itemView.findViewById(R.id.fav_button);

            if (buttonTimer != null)
                buttonTimer.setVisibility(View.GONE);
            if (buttonNotifications != null)
                buttonNotifications.setVisibility(View.GONE);

            likeButton = (LikeButton) itemView.findViewById(R.id.fav_button);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            buttonLog = (Button) itemView.findViewById(R.id.log_button);
            buttonOff = (Button) itemView.findViewById(R.id.off_button);

            extraPanel = (LinearLayout) itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<SceneInfo> list = data;

            int count = list.size();
            final ArrayList<SceneInfo> sceneInfos = new ArrayList<>(count);

            SceneInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    sceneInfos.add(filterableObject);
                }
            }

            results.values = sceneInfos;
            results.count = sceneInfos.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<SceneInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}
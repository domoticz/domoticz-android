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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.DataObjectHolder> implements RVHAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = SceneAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
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
        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("scenes");
        setData(data);

        this.listener = listener;
    }

    public void setData(ArrayList<SceneInfo> data) {
        ArrayList<SceneInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.scene_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((view.findViewById(R.id.card_global_wrapper)) != null)
                view.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.on_button)) != null)
                ((MaterialButton) view.findViewById(R.id.on_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
            if ((view.findViewById(R.id.off_button)) != null)
                ((MaterialButton) view.findViewById(R.id.off_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        return new DataObjectHolder(view);
    }

    private ArrayList<SceneInfo> SortData(ArrayList<SceneInfo> data) {
        ArrayList<SceneInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            for (String s : mCustomSorting) {
                for (SceneInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())))
                        customdata.add(d);
                }
            }
            for (SceneInfo d : data) {
                if (!customdata.contains(d))
                    customdata.add(d);
            }
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        List<String> ids = new ArrayList<>();
        for (SceneInfo d : filteredData) {
            ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
        mSharedPrefs.saveSortingList("plans", ids);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final SceneInfo mSceneInfo = filteredData.get(position);

            holder.infoIcon.setTag(mSceneInfo.getIdx());
            holder.infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemLongClicked((int) v.getTag());
                }
            });

            if (DomoticzValues.Scene.Type.SCENE.equalsIgnoreCase(mSceneInfo.getType())) {
                holder.isProtected = mSceneInfo.isProtected();

                setButtons(holder, Buttons.SCENE);
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

                Picasso.get().load(DomoticzIcons.getDrawableIcon(
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

                Picasso.get().load(DomoticzIcons.getDrawableIcon(
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }

    private void remove(int position) {
        filteredData.remove(position);
        notifyItemRemoved(position);
    }

    private void swap(int firstPosition, int secondPosition) {
        Collections.swap(filteredData, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        SaveSorting();
    }

    interface Buttons {
        int SCENE = 0;
        int GROUP = 1;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView switch_name, signal_level, switch_battery_level;
        Boolean isProtected;
        ImageView iconRow;
        LikeButton likeButton;
        LinearLayout extraPanel;
        Button buttonOn, buttonOff;
        Chip buttonLog, buttonTimer, buttonNotifications;
        ImageView infoIcon;

        public DataObjectHolder(View itemView) {
            super(itemView);

            buttonOn = itemView.findViewById(R.id.on_button);
            signal_level = itemView.findViewById(R.id.switch_signal_level);
            iconRow = itemView.findViewById(R.id.rowIcon);
            switch_name = itemView.findViewById(R.id.switch_name);
            switch_battery_level = itemView.findViewById(R.id.switch_battery_level);
            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            buttonLog = itemView.findViewById(R.id.log_button);
            buttonTimer = itemView.findViewById(R.id.timer_button);
            buttonNotifications = itemView.findViewById(R.id.notifications_button);
            likeButton = itemView.findViewById(R.id.fav_button);

            if (buttonTimer != null)
                buttonTimer.setVisibility(View.GONE);
            if (buttonNotifications != null)
                buttonNotifications.setVisibility(View.GONE);

            likeButton = itemView.findViewById(R.id.fav_button);
            iconRow = itemView.findViewById(R.id.rowIcon);
            buttonLog = itemView.findViewById(R.id.log_button);
            buttonOff = itemView.findViewById(R.id.off_button);

            extraPanel = itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }

        @Override
        public void onItemSelected(int actionstate) {
            System.out.println("Item is selected");
        }

        @Override
        public void onItemClear() {
            System.out.println("Item is unselected");
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
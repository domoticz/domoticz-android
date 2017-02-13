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
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.Interfaces.EventsClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.EventInfo;
import nl.hnogames.domoticzapi.Domoticz;

@SuppressWarnings("unused")
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.DataObjectHolder> {
    private static final String TAG = EventsAdapter.class.getSimpleName();
    private final EventsClickListener listener;
    public Context context;
    private ArrayList<EventInfo> filteredData = null;
    private ArrayList<EventInfo> data = null;
    private Domoticz domoticz;
    private SharedPrefUtil mSharedPrefs;
    private ItemFilter mFilter = new ItemFilter();

    public EventsAdapter(Context context,
                         Domoticz mDomoticz,
                         ArrayList<EventInfo> data,
                         EventsClickListener listener) {
        super();

        this.context = context;
        this.domoticz = mDomoticz;
        this.listener = listener;
        mSharedPrefs = new SharedPrefUtil(context);
        setData(data);
    }

    public void setData(ArrayList<EventInfo> data) {
        Collections.reverse(data);

        this.data = data;
        this.filteredData = data;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_row_default, parent, false);

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
    public void onBindViewHolder(final DataObjectHolder holder, int position) {

        if (filteredData != null && filteredData.size() > 0) {
            final EventInfo mEventInfo = filteredData.get(position);

            if (holder.buttonON != null) {
                holder.buttonON.setId(mEventInfo.getId());
                holder.buttonON.setEnabled(true);
                holder.buttonON.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (EventInfo e : data) {
                            if (e.getId() == v.getId()) {
                                listener.onEventClick(e.getId(), !e.getStatusBoolean());
                            }
                        }
                    }
                });

                if (mEventInfo.getStatusBoolean()) {
                    holder.buttonON.setChecked(true);
                } else {
                    holder.buttonON.setChecked(false);
                }
            }

            if (holder.name != null)
                holder.name.setText(mEventInfo.getName());

            if (holder.message != null) {
                if (mEventInfo.getStatusBoolean()) {
                    holder.message.setText("Status: " + context.getString(R.string.button_state_on));
                } else {
                    holder.message.setText("Status: " + context.getString(R.string.button_state_off));
                }
            }

            Picasso.with(context).load(R.drawable.cone).into(holder.iconRow);
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView name;
        TextView message;
        Switch buttonON;
        ImageView iconRow;

        public DataObjectHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.logs_name);
            message = (TextView) itemView.findViewById(R.id.logs_message);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            buttonON = (Switch) itemView.findViewById(R.id.switch_button);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            Filter.FilterResults results = new FilterResults();

            final ArrayList<EventInfo> list = data;

            int count = list.size();
            final ArrayList<EventInfo> nlist = new ArrayList<EventInfo>(count);

            EventInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<EventInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}
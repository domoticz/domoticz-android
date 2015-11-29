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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.EventsClickListener;
import nl.hnogames.domoticz.R;

// Example used: http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
// And: http://www.survivingwithandroid.com/2013/02/android-listview-adapter-checkbox-item_7.html
public class EventsAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = EventsAdapter.class.getSimpleName();

    Context context;
    ArrayList<EventInfo> filteredData = null;
    ArrayList<EventInfo> data = null;
    Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();
    private final EventsClickListener listener;

    public EventsAdapter(Context context,
                         ArrayList<EventInfo> data,
                         EventsClickListener listener) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);

        Collections.reverse(data);
        this.data = data;
        this.filteredData = data;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void handleClick(int id, boolean action) {
        listener.onEventClick(id, action);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        int layoutResourceId;

        final EventInfo mEventInfo = filteredData.get(position);
        holder = new ViewHolder();
        layoutResourceId = R.layout.event_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.name = (TextView) convertView.findViewById(R.id.logs_name);
        holder.message = (TextView) convertView.findViewById(R.id.logs_message);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.buttonON = (Switch) convertView.findViewById(R.id.switch_button);

        if (holder.buttonON != null) {
            holder.buttonON.setId(mEventInfo.getId());
            holder.buttonON.setEnabled(true);
            holder.buttonON.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (EventInfo e : data) {
                        if (e.getId() == v.getId()) {
                            handleClick(e.getId(), !e.getStatusBoolean());

                            //reset switch to previous state (we can't handle toggles yet!)
                            if (mEventInfo.getStatusBoolean()) {
                                holder.buttonON.setChecked(true);
                            } else {
                                holder.buttonON.setChecked(false);
                            }
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
        convertView.setTag(holder);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView message;
        Switch buttonON;
        ImageView iconRow;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

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
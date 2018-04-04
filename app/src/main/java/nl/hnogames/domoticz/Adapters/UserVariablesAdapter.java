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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.Interfaces.UserVariablesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Domoticz;

@SuppressWarnings("unused")
public class UserVariablesAdapter extends RecyclerView.Adapter<UserVariablesAdapter.DataObjectHolder> {
    private static final String TAG = UserVariablesAdapter.class.getSimpleName();
    public ArrayList<UserVariableInfo> filteredData = null;
    private Context context;
    private ArrayList<UserVariableInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();
    private UserVariablesClickListener listener;

    private SharedPrefUtil mSharedPrefs;

    public UserVariablesAdapter(Context context,
                                Domoticz mDomoticz,
                                ArrayList<UserVariableInfo> data,
                                UserVariablesClickListener _listener) {
        super();

        listener = _listener;
        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;
        setData(data);
    }

    public void setData(ArrayList<UserVariableInfo> data) {
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
                .inflate(R.layout.vars_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) view.findViewById(R.id.card_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
            if ((view.findViewById(R.id.set_uservar)) != null)
                (view.findViewById(R.id.set_uservar)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, int position) {

        if (filteredData != null && filteredData.size() > 0) {
            final UserVariableInfo mUserVariableInfo = filteredData.get(position);

            holder.name.setText(mUserVariableInfo.getName());
            holder.message.setText("Value: " + mUserVariableInfo.getValue() + " (" + mUserVariableInfo.getTypeValue() + ")");
            holder.datetime.setText(mUserVariableInfo.getLastUpdate());
            holder.set.setId(mUserVariableInfo.getIdx());

            holder.set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (UserVariableInfo v : filteredData) {
                        if (v.getIdx() == view.getId())
                            listener.onUserVariableClick(v);
                    }
                }
            });

            Picasso.with(context).load(R.drawable.printer).into(holder.iconRow);
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
        TextView datetime;
        TextView message;
        ImageView iconRow;
        Button set;

        public DataObjectHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.logs_name);
            datetime = (TextView) itemView.findViewById(R.id.logs_datetime);
            message = (TextView) itemView.findViewById(R.id.logs_message);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            set = (Button) itemView.findViewById(R.id.set_uservar);

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

            FilterResults results = new FilterResults();

            final ArrayList<UserVariableInfo> list = data;

            int count = list.size();
            final ArrayList<UserVariableInfo> nlist = new ArrayList<UserVariableInfo>(count);

            UserVariableInfo filterableObject;

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
            filteredData = (ArrayList<UserVariableInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}
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

package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.listeners.OnDismissCallback;
import com.dexafree.materialList.view.MaterialListView;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.helpers.CustomIncomingMessageViewHolder;
import nl.hnogames.domoticz.helpers.CustomOutcomingMessageViewHolder;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class NotificationHistory extends Fragment {
    private final String TAG = NotificationHistory.class.getSimpleName();
    private ViewGroup root;
    private SharedPrefUtil mSharedPrefs;
    private Context context;

    @Override
    @DebugLog
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_notification_history, null);
        context = getActivity();
        mSharedPrefs = new SharedPrefUtil(context);
        List<NotificationInfo> notifications = mSharedPrefs.getLoggedNotifications();

        if(notifications != null && notifications.size()>0) {
            MessageHolders holdersConfig = new MessageHolders()
                    .setIncomingTextConfig(
                            CustomIncomingMessageViewHolder.class,
                            R.layout.item_custom_incoming_text_message)
                    .setOutcomingTextConfig(
                            CustomOutcomingMessageViewHolder.class,
                            R.layout.item_custom_outcoming_text_message);

            MessagesList messagesList = root.findViewById(R.id.messagesList);
            MessagesListAdapter<NotificationInfo> adapter = new MessagesListAdapter<>(DeviceUtils.getUniqueID(context), holdersConfig, null);
            adapter.addToEnd(notifications, false);
            messagesList.setAdapter(adapter);
        }
        return root;
    }
}
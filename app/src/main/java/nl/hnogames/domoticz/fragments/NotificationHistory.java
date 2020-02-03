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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.helpers.CustomIncomingMessageViewHolder;
import nl.hnogames.domoticz.helpers.CustomOutcomingMessageViewHolder;
import nl.hnogames.domoticz.ui.SendNotificationDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.NotificationTypeInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.NotificationTypesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SendNotificationReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class NotificationHistory extends Fragment {
    private ViewGroup root;
    private SharedPrefUtil mSharedPrefs;
    private Context context;
    private Domoticz mDomoticz;
    private ArrayList<NotificationTypeInfo> mNotificationTypes;
    private MessagesListAdapter<NotificationInfo> adapter;
    private CoordinatorLayout coordinatorLayout;
    private UserInfo user = null;
    private ServerUtil mServerUtil = null;
    private ConfigInfo mConfigInfo = null;

    @Override
    @DebugLog
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_notification_history, null);

        context = getActivity();
        mServerUtil = new ServerUtil(context);
        mConfigInfo = mServerUtil.getActiveServer().getConfigInfo(context);

        mSharedPrefs = new SharedPrefUtil(context);
        mDomoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

        List<NotificationInfo> notifications = mSharedPrefs.getLoggedNotifications();
        if (notifications != null && notifications.size() > 0) {
            MessageHolders holdersConfig = new MessageHolders()
                    .setIncomingTextConfig(
                            CustomIncomingMessageViewHolder.class,
                            R.layout.item_custom_incoming_text_message)
                    .setOutcomingTextConfig(
                            CustomOutcomingMessageViewHolder.class,
                            R.layout.item_custom_outcoming_text_message);

            MessagesList messagesList = root.findViewById(R.id.messagesList);
            adapter = new MessagesListAdapter<>(DeviceUtils.getUniqueID(context), holdersConfig, null);
            adapter.addToEnd(notifications, true);
            messagesList.setAdapter(adapter);
        }
        coordinatorLayout = root.findViewById(R.id.coordinatorLayout);
        mDomoticz.GetNotificationSystems(new NotificationTypesReceiver() {
            @Override
            public void onReceive(ArrayList<NotificationTypeInfo> notificationTypes) {
                mNotificationTypes = notificationTypes;
                if(getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onError(Exception error) {
            }
        });

        return root;
    }

    public UserInfo getCurrentUser(Domoticz domoticz) {
        try {
            ConfigInfo config = mConfigInfo;
            if (config != null) {
                for (UserInfo user : config.getUsers()) {
                    if (user.getUsername().equals(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME)))
                        return user;
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mNotificationTypes != null) {
            UserInfo user = getCurrentUser(mDomoticz);
            if (user != null && user.getRights() >= 2) {
                inflater.inflate(R.menu.menu_notification, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity() != null)
                    getActivity().finish();
                return true;
            case R.id.action_add:
                SendNotificationDialog dialog = new SendNotificationDialog(context, mNotificationTypes);
                dialog.onDismissListener(new SendNotificationDialog.DismissListener() {
                    @Override
                    public void OnSend(final NotificationInfo message) {
                        mDomoticz.SendNotification(message.getTitle(), message.getText(), message.getSystems(), new SendNotificationReceiver() {
                            @Override
                            public void onSuccess() {
                                adapter.addToStart(message, true);
                                Snackbar.make(coordinatorLayout, R.string.notification_send, Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Exception error) {
                                Snackbar.make(coordinatorLayout, R.string.notification_error_send, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                dialog.show();
                return true;
            default:
                break;
        }
        return false;
    }
}
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

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.SwitchesAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.ColorPickerDialog;
import nl.hnogames.domoticz.UI.NotificationInfoDialog;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.UI.SecurityPanelDialog;
import nl.hnogames.domoticz.UI.SwitchInfoDialog;
import nl.hnogames.domoticz.UI.SwitchLogInfoDialog;
import nl.hnogames.domoticz.UI.SwitchTimerInfoDialog;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.NotificationInfo;
import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;
import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.NotificationReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Switches extends DomoticzRecyclerFragment implements DomoticzFragmentListener,
    switchesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Switches.class.getSimpleName();
    private Context mContext;
    private SwitchesAdapter adapter;
    private ArrayList<DevicesInfo> extendedStatusSwitches;
    private Parcelable state = null;
    private boolean busy = false;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override
    @DebugLog
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        getSwitchesData();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_switches);
        initAnimation();
    }

    @Override
    @DebugLog
    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @DebugLog
    public void onConnectionOk() {
        super.showSpinner(true);
        getSwitchesData();
    }

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    private void getSwitchesData() {
        busy = true;
        if (extendedStatusSwitches != null && extendedStatusSwitches.size() > 0) {
            state = gridView.getLayoutManager().onSaveInstanceState();
        }
        //switch toggled, refresh listview
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        WidgetUtils.RefreshWidgets(mContext);

        new GetCachedDataTask().execute();

    }

    // add dynamic list view
    // https://github.com/nhaarman/ListViewAnimations
    private void createListView(ArrayList<DevicesInfo> switches) {
        if (getView() != null) {
            try {
                ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
                final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
                final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

                for (DevicesInfo mDevicesInfo : switches) {
                    String name = mDevicesInfo.getName();
                    int switchTypeVal = mDevicesInfo.getSwitchTypeVal();
                    String switchType = mDevicesInfo.getSwitchType();

                    if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        appSupportedSwitchesValues.contains(switchTypeVal) &&
                        appSupportedSwitchesNames.contains(switchType)) {
                        if (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) {
                            supportedSwitches.add(mDevicesInfo);
                        } else {
                            if (mContext != null) {
                                UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.filter_on) + ": " + super.getSort(), Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(mContext.getString(R.string.filter_on) + ": " + super.getSort());
                                if ((super.getSort().equals(mContext.getString(R.string.filterOn_on)) && mDevicesInfo.getStatusBoolean()) &&
                                    mDomoticz.isOnOffSwitch(mDevicesInfo)) {
                                    supportedSwitches.add(mDevicesInfo);
                                }
                                if ((super.getSort().equals(mContext.getString(R.string.filterOn_off)) && !mDevicesInfo.getStatusBoolean()) &&
                                    mDomoticz.isOnOffSwitch(mDevicesInfo)) {
                                    supportedSwitches.add(mDevicesInfo);
                                }
                                if (super.getSort().equals(mContext.getString(R.string.filterOn_static)) &&
                                    !mDomoticz.isOnOffSwitch(mDevicesInfo)) {
                                    supportedSwitches.add(mDevicesInfo);
                                }
                            }
                        }
                    }
                }

                if (adapter == null) {
                    final switchesClickListener listener = this;
                    adapter = new SwitchesAdapter(mContext, getServerUtil(), supportedSwitches, listener);
                    alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                    gridView.setAdapter(alphaSlideIn);
                } else {
                    adapter.setData(supportedSwitches);
                    adapter.notifyDataSetChanged();
                    alphaSlideIn.notifyDataSetChanged();
                }

                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    @DebugLog
                    public void onRefresh() {
                        getSwitchesData();
                    }
                });

                if (state != null) {
                    gridView.getLayoutManager().onRestoreInstanceState(state);
                }

                this.Filter(filter);
                busy = false;
            } catch (Exception ex) {
                errorHandling(ex);
            }
        }

        super.showSpinner(false);
    }

    private void showInfoDialog(final DevicesInfo mSwitch) {
        SwitchInfoDialog infoDialog = new SwitchInfoDialog(
            mContext,
            mSwitch,
            R.layout.dialog_switch_info);
        infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
        infoDialog.setLastUpdate(mSwitch.getLastUpdate());
        infoDialog.setSignalLevel(String.valueOf(mSwitch.getSignalLevel()));
        infoDialog.setBatteryLevel(String.valueOf(mSwitch.getBatteryLevel()));
        infoDialog.setIsFavorite(mSwitch.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new SwitchInfoDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) changeFavorite(mSwitch, isFavorite);
            }
        });
    }

    private void showLogDialog(ArrayList<SwitchLogInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(mContext, "No logs found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchLogInfoDialog infoDialog = new SwitchLogInfoDialog(
                mContext,
                switchLogs,
                R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    private void showTimerDialog(ArrayList<SwitchTimerInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(mContext, "No timer found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchTimerInfoDialog infoDialog = new SwitchTimerInfoDialog(
                mContext,
                switchLogs,
                R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    private void showNotificationDialog(ArrayList<NotificationInfo> notificationInfo) {
        if (notificationInfo.size() <= 0) {
            Toast.makeText(mContext, "No notifications found.", Toast.LENGTH_LONG).show();
        } else {
            NotificationInfoDialog infoDialog = new NotificationInfoDialog(
                mContext,
                notificationInfo);
            infoDialog.show();
        }
    }

    private void changeFavorite(final DevicesInfo mSwitch, final boolean isFavorite) {
        if (busy)
            return;

        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSwitch.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
        } else {
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSwitch.setFavoriteBoolean(isFavorite);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_favorite, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_favorite);
            }
        });
    }

    @Override
    @DebugLog
    public void onLogButtonClick(int idx) {

        mDomoticz.getSwitchLogs(idx, new SwitchLogReceiver() {
            @Override
            @DebugLog
            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switchesLogs) {
                showLogDialog(switchesLogs);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_logs, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_logs);
            }
        });
    }

    @Override
    @DebugLog
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getSwitch(idx), checked);
    }

    @Override
    @DebugLog
    public void onColorButtonClick(final int idx) {
        ColorPickerDialog colorDialog = new ColorPickerDialog(
            mContext, idx);
        colorDialog.show();

        colorDialog.onDismissListener(new ColorPickerDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss(final int selectedColor) {
                if (getSwitch(idx).isProtected()) {
                    PasswordDialog passwordDialog = new PasswordDialog(
                        mContext, mDomoticz);
                    passwordDialog.show();
                    passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                        @Override
                        @DebugLog
                        public void onDismiss(String password) {
                            setColor(selectedColor, idx, password);
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                } else
                    setColor(selectedColor, idx, null);
            }

            @Override
            @DebugLog
            public void onChangeColor(final int selectedColor) {
                if (!getSwitch(idx).isProtected()) {
                    setColor(selectedColor, idx, null);
                }
            }
        });
    }

    private void setColor(int selectedColor, final int idx, final String password) {
        double[] hsv = UsefulBits.rgb2hsv(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
        Log.v(TAG, "Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2] + " color: " + selectedColor);
        if (hsv.length <= 0)
            return;

        boolean isWhite = false;
        long hue = Math.round(hsv[0]);
        if (selectedColor == -1) {
            isWhite = true;
        }

        mDomoticz.setRGBColorAction(idx,
            DomoticzValues.Json.Url.Set.RGBCOLOR,
            hue,
            getSwitch(idx).getLevel(),
            isWhite,
            password,
            new setCommandReceiver() {
                @Override
                @DebugLog
                public void onReceiveResult(String result) {
                    UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.color_set) + ": " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.color_set);
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_color, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.error_color);
                    }
                }
            });
    }

    @Override
    @DebugLog
    public void onTimerButtonClick(int idx) {
        mDomoticz.getSwitchTimers(idx, new SwitchTimerReceiver() {
            @Override
            @DebugLog
            public void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers) {
                if (switchTimers != null)
                    showTimerDialog(switchTimers);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_timer, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_timer);
            }
        });
    }

    @Override
    @DebugLog
    public void onNotificationButtonClick(int idx) {
        mDomoticz.getNotifications(idx, new NotificationReceiver() {
            @Override
            @DebugLog
            public void onReceiveNotifications(ArrayList<NotificationInfo> mNotificationInfos) {
                if (mNotificationInfos != null)
                    showNotificationDialog(mNotificationInfos);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_notifications, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_notifications);
            }
        });
    }

    @Override
    @DebugLog
    public void onThermostatClick(int idx) {
    }

    @Override
    @DebugLog
    public void onSetTemperatureClick(int idx) {
    }

    @Override
    @DebugLog
    public void onSecurityPanelButtonClick(int idx) {
        SecurityPanelDialog securityDialog = new SecurityPanelDialog(
            mContext, mDomoticz,
            getSwitch(idx));
        securityDialog.show();

        securityDialog.onDismissListener(new SecurityPanelDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss() {
                getSwitchesData();//refresh
            }
        });
    }

    @Override
    @DebugLog
    public void onStateButtonClick(final int idx, int itemsRes, final int[] stateIds) {
        new MaterialDialog.Builder(mContext)
            .title(R.string.choose_status)
            .items(itemsRes)
            .itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                @DebugLog
                public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                    if (getSwitch(idx).isProtected()) {
                        PasswordDialog passwordDialog = new PasswordDialog(
                            mContext, mDomoticz);
                        passwordDialog.show();
                        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                            @Override
                            @DebugLog
                            public void onDismiss(String password) {
                                setState(idx, stateIds[which], password);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    } else
                        setState(idx, stateIds[which], null);
                }
            })
            .show();
        ;
    }

    @Override
    @DebugLog
    public void onSelectorDimmerClick(final int idx, final String[] levelNames) {
        new MaterialDialog.Builder(mContext)
            .title(R.string.choose_status)
            .items(levelNames)
            .itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                @DebugLog
                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    for (int i = 0; i < levelNames.length; i++) {
                        if (levelNames[i].equals(text)) {
                            onDimmerChange(idx, i * 10, true);
                        }
                    }
                }
            })
            .show();
    }

    @Override
    public void onSelectorChange(int idx, int level) {
        onDimmerChange(idx, level, true);
    }

    @Override
    @DebugLog
    public void onItemClicked(View v, int position) {
        LinearLayout extra_panel = (LinearLayout) v.findViewById(R.id.extra_panel);
        if (extra_panel != null) {
            if (extra_panel.getVisibility() == View.VISIBLE) {
                extra_panel.startAnimation(animHide);
                extra_panel.setVisibility(View.GONE);
            } else {
                extra_panel.setVisibility(View.VISIBLE);
                extra_panel.startAnimation(animShow);
            }

            if (extra_panel != lExtraPanel) {
                if (lExtraPanel != null) {
                    if (lExtraPanel.getVisibility() == View.VISIBLE) {
                        lExtraPanel.startAnimation(animHide);
                        lExtraPanel.setVisibility(View.GONE);
                    }
                }
            }

            lExtraPanel = extra_panel;
        }
    }

    @Override
    @DebugLog
    public boolean onItemLongClicked(int position) {
        showInfoDialog(adapter.filteredData.get(position));
        return true;
    }

    private void setState(final int idx, int state, final String password) {
        mDomoticz.setModalAction(idx,
            state,
            1,
            password,
            new setCommandReceiver() {
                @Override
                @DebugLog
                public void onReceiveResult(String result) {
                    UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.state_set) + ": " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.state_set);
                    getSwitchesData();
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else
                        errorHandling(error);
                }
            });
    }

    private DevicesInfo getSwitch(int idx) {
        DevicesInfo clickedSwitch = null;
        for (DevicesInfo mDevicesInfo : extendedStatusSwitches) {
            if (mDevicesInfo.getIdx() == idx) {
                clickedSwitch = mDevicesInfo;
            }
        }
        return clickedSwitch;
    }

    @Override
    @DebugLog
    public void onSwitchClick(int idx, final boolean checked) {
        if (busy)
            return;

        addDebugText("onSwitchClick");
        addDebugText("Set idx " + idx + " to " + checked);
        final DevicesInfo clickedSwitch = getSwitch(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                @DebugLog
                public void onDismiss(String password) {
                    toggleSwitch(clickedSwitch, checked, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            toggleSwitch(clickedSwitch, checked, null);
        }
    }

    private void toggleSwitch(DevicesInfo clickedSwitch, boolean checked, final String password) {
        if (checked) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_on);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_off);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        }

        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getIdx() > 0) {
            int jsonAction;
            int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
            if (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                else jsonAction = DomoticzValues.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
                else jsonAction = DomoticzValues.Device.Switch.Action.OFF;
            }

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                @Override
                @DebugLog
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    getSwitchesData();
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else
                        errorHandling(error);
                }
            });
        }
    }

    @Override
    @DebugLog
    public void onButtonClick(int idx, final boolean checked) {
        if (busy)
            return;

        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to " + (checked ? "ON" : "OFF"));
        final DevicesInfo clickedSwitch = getSwitch(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                @DebugLog
                public void onDismiss(String password) {
                    toggleButton(clickedSwitch, checked, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else
            toggleButton(clickedSwitch, checked, null);
    }

    private void toggleButton(DevicesInfo clickedSwitch, boolean checked, final String password) {
        if (checked) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_on);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_off);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        }

        int idx = clickedSwitch.getIdx();
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                getSwitchesData();
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password)) {
                    UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else
                    errorHandling(error);
            }
        });
    }

    @Override
    @DebugLog
    public void onBlindClick(final int idx, final int jsonAction) {
        if (busy)
            return;

        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));
        final DevicesInfo clickedSwitch = getSwitch(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                @DebugLog
                public void onDismiss(String password) {
                    setBlindState(clickedSwitch, jsonAction, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            setBlindState(clickedSwitch, jsonAction, null);
        }
    }

    private void setBlindState(final DevicesInfo clickedSwitch, final int jsonAction, final String password) {
        if ((jsonAction == DomoticzValues.Device.Blind.Action.UP || jsonAction == DomoticzValues.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() != DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_up);
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.OPEN);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.DOWN || jsonAction == DomoticzValues.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() != DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.CLOSED);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_down);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.UP || jsonAction == DomoticzValues.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.CLOSED);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_down);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.DOWN || jsonAction == DomoticzValues.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.OPEN);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_up);
        } else {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.STOPPED);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_stop) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_stop);
        }

        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
        mDomoticz.setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                getSwitchesData();
            }


            @Override
            @DebugLog
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password)) {
                    UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } //else
                //  errorHandling(error);
            }
        });
    }

    @Override
    @DebugLog
    public void onDimmerChange(int idx, final int value, final boolean selector) {
        if (busy)
            return;

        addDebugText("onDimmerChange for " + idx + " to " + value);
        final DevicesInfo clickedSwitch = getSwitch(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                @DebugLog
                public void onDismiss(String password) {
                    setDimmerState(clickedSwitch, value, selector, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            setDimmerState(clickedSwitch, value, selector, null);
        }
    }

    private void setDimmerState(DevicesInfo clickedSwitch, int value, final boolean selector, final String password) {
        if (clickedSwitch != null) {
            String text = String.format(mContext.getString(R.string.set_level_switch),
                clickedSwitch.getName(),
                !selector ? (value - 1) : ((value) / 10) + 1);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, text, Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(text);

            int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
            int jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;

            mDomoticz.setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, !selector ? (value) : (value) + 10, password, new setCommandReceiver() {
                @Override
                @DebugLog
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    if (selector)
                        getSwitchesData();
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else
                        errorHandling(error);
                }
            });
        }
    }

    @Override
    @DebugLog
    public void onPause() {
        super.onPause();
    }

    @Override
    @DebugLog
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);

                super.errorHandling(error);
            }
        }
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<DevicesInfo> cacheSwitches = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheSwitches = (ArrayList<DevicesInfo>) SerializableManager.readSerializedObject(mContext, "Switches");
                    extendedStatusSwitches = cacheSwitches;
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheSwitches != null)
                createListView(cacheSwitches);

            mDomoticz.getDevices(new DevicesReceiver() {
                @Override
                @DebugLog
                public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                    extendedStatusSwitches = switches;
                    SerializableManager.saveSerializable(mContext, switches, "Switches");
                    successHandling(switches.toString(), false);
                    createListView(switches);
                }

                @Override
                @DebugLog
                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    errorHandling(error);
                }
            }, 0, "light");
        }
    }
}
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

package nl.hnogames.domoticz.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.ui.Backdrop.BackdropContainer;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.ViewUtils;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.SunRiseInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticzapi.Utils.ServerUtil;
import rm.com.clocks.ClockImageView;

public class DomoticzDashboardFragment extends Fragment {
    public RecyclerView gridView;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public SharedPrefUtil mSharedPrefs;
    public PhoneConnectionUtil mPhoneConnectionUtil;
    public View frameLayout;
    public LinearLayout lySortDevices;
    public BackdropContainer backdropContainer;
    public MaterialCardView bottomLayoutWrapper;
    public RecyclerView planList;
    public MaterialButton sortAll, sortOn, sortOff, sortStatic, btnCheckSettings;
    public boolean isTablet = false;
    public boolean isPortrait = false;
    public GridLayoutManager mLayoutManager;
    public LinearLayout headerLayout;
    private DomoticzFragmentListener listener;
    private String fragmentName;
    private TextView debugText;
    private boolean debug;
    private ViewGroup root;
    private String sort = "";
    private boolean backdropShown = false;

    public DomoticzDashboardFragment() {
    }

    public void setTheme() {
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(getActivity());
    }

    public String getSort() {
        return sort;
    }

    public ServerUtil getServerUtil() {
        return StaticHelper.getServerUtil(getContext());
    }

    public ConfigInfo getServerConfigInfo(Context context) {
        try {
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                return ((MainActivity) getActivity()).getConfig();
            } else if (activity instanceof PlanActivity) {
                return ((PlanActivity) getActivity()).getConfig();
            } else return null;
        } catch (Exception ex) {
        }
        return null;
    }

    public UserInfo getCurrentUser(Context context, Domoticz domoticz) {
        try {
            ConfigInfo config = getServerConfigInfo(context);
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

    public void sortFragment(String sort) {
        this.sort = sort;
        refreshFragment();
    }

    public void initViews(View root) {
        gridView = root.findViewById(R.id.my_recycler_view);
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(getContext());

        setGridViewLayout();
        setPlanListLayout();
        mSwipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);

        bottomLayoutWrapper = root.findViewById(R.id.bottomLayoutWrapper);
        lySortDevices = root.findViewById(R.id.lySortDevices);
        if (getActivity() instanceof MainActivity)
            frameLayout = ((MainActivity) getActivity()).frameLayout;

        sortStatic = root.findViewById(R.id.btnSortStatic);
        if (sortStatic != null) {
            sortStatic.setOnClickListener(v -> {
                sortFragment(String.valueOf(sortStatic.getText()));
                toggleBackDrop();
            });
        }

        btnCheckSettings = root.findViewById(R.id.btnCheckSettings);
        if (btnCheckSettings != null) {
            btnCheckSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).OpenSettings();
                }
            });
        }

        sortOn = root.findViewById(R.id.btnSortOn);
        if (sortOn != null) {
            sortOn.setOnClickListener(v -> {
                sortFragment(String.valueOf(sortOn.getText()));
                toggleBackDrop();
            });
        }

        sortOff = root.findViewById(R.id.btnSortOff);
        if (sortOff != null) {
            sortOff.setOnClickListener(v -> {
                sortFragment(String.valueOf(sortOff.getText()));
                toggleBackDrop();
            });
        }

        sortAll = root.findViewById(R.id.btnSortAll);
        if (sortAll != null) {
            sortAll.setOnClickListener(v -> {
                sortFragment(String.valueOf(sortAll.getText()));
                toggleBackDrop();
            });
        }

        backdropContainer = root.findViewById(R.id.backdropcontainer);
        backdropContainer
                .dropInterpolator(new LinearInterpolator())
                .dropHeight(this.getResources().getDimensionPixelSize(R.dimen.sneek_height))
                .build();
    }

    public void setGridViewLayout() {
        try {
            isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if (getActivity() instanceof MainActivity) {
                isTablet = ViewUtils.isTablet(getContext());
            }
            Log.d("orientationchanged", "Event: setGridViewLayout Portrait:" + isPortrait + " Tablet:" + isTablet);

            gridView.setHasFixedSize(true);
            if (isTablet) {
                mLayoutManager = new GridLayoutManager(getContext(), 3);
                Log.d("orientationchanged", "Event: GridLayoutManager span 3");
            } else {
                mLayoutManager = new GridLayoutManager(getContext(), 2);
                Log.d("orientationchanged", "Event: GridLayoutManager span 2");
            }
            gridView.setLayoutManager(mLayoutManager);
        } catch (Exception ignored) {
        }
    }

    public void setPlanListLayout() {
        try {
            planList = root.findViewById(R.id.planList);
            headerLayout = root.findViewById(R.id.headerLayout);
            planList.setVisibility(View.GONE);
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            planList.setLayoutManager(layoutManager);
        } catch (Exception ignored) {
        }
    }

    public void setClockLayout(SunRiseInfo sunriseInfo) {
        try {
            ClockImageView clock, sunrise, sunset;
            LinearLayout clockLayout, sunriseLayout, sunsetLayout, clockLayoutWrapper;
            TextView clockText, sunriseText, sunsetText;

            clock = root.findViewById(R.id.clock);
            sunrise = root.findViewById(R.id.sunrise);
            sunset = root.findViewById(R.id.sunset);
            clockLayout = root.findViewById(R.id.clockLayout);
            sunriseLayout = root.findViewById(R.id.sunriseLayout);
            sunsetLayout = root.findViewById(R.id.sunsetLayout);
            clockLayoutWrapper = root.findViewById(R.id.clockLayoutWrapper);
            clockText = root.findViewById(R.id.clockText);
            sunriseText = root.findViewById(R.id.sunriseText);
            sunsetText = root.findViewById(R.id.sunsetText);

            if (mSharedPrefs.addClockToDashboard()) {
                clockLayoutWrapper.setVisibility(View.VISIBLE);
                clockLayout.setVisibility(View.VISIBLE);
                sunriseLayout.setVisibility(View.VISIBLE);
                sunsetLayout.setVisibility(View.VISIBLE);
                if (sunriseInfo != null) {
                    String s = sunriseInfo.getSunrise();
                    if (!UsefulBits.isEmpty(s) && s.indexOf(":") > 0) {
                        sunrise.setHours(Integer.valueOf(s.substring(0, s.indexOf(":"))));
                        sunrise.setMinutes(Integer.valueOf(s.substring(s.indexOf(":") + 1)));
                        sunriseText.setText(s);
                    }

                    String s2 = sunriseInfo.getSunset();
                    if (!UsefulBits.isEmpty(s2) && s2.indexOf(":") > 0) {
                        sunset.setHours(Integer.valueOf(s2.substring(0, s2.indexOf(":"))));
                        sunset.setMinutes(Integer.valueOf(s2.substring(s2.indexOf(":") + 1)));
                        sunsetText.setText(s2);
                    }

                    String c = sunriseInfo.getServerTime();
                    if (!UsefulBits.isEmpty(c) && c.indexOf(":") > 0) {
                        c = c.substring((c.indexOf(":") - 2), (c.indexOf(":") + 3));
                        clock.setHours(Integer.valueOf(c.substring(0, c.indexOf(":"))));
                        clock.setMinutes(Integer.valueOf(c.substring(c.indexOf(":") + 1)));
                        clockText.setText(c);
                    }
                }
            } else {
                clockLayoutWrapper.setVisibility(View.GONE);
                clockLayout.setVisibility(View.GONE);
                sunriseLayout.setVisibility(View.GONE);
                sunsetLayout.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
            Log.e("WEIRD", ignored.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_cameras, null);
        initViews(root);
        setTheme();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSharedPrefs = new SharedPrefUtil(getActivity());
        debug = mSharedPrefs.isDebugEnabled();

        if (debug)
            showDebugLayout();

        checkConnection();
    }

    /**
     * Connects to the attached fragment to cast the DomoticzFragmentListener to.
     * Throws ClassCastException if the fragment does not implement the DomoticzFragmentListener
     *
     * @param fragment fragment to cast the DomoticzFragmentListener to
     */
    public void onAttachFragment(Fragment fragment) {
        fragmentName = fragment.toString();

        try {
            listener = (DomoticzFragmentListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    fragment + " must implement DomoticzFragmentListener");
        }
    }

    public void showSpinner(boolean show) {
        if (show) {
            if (gridView != null)
                gridView.setVisibility(View.GONE);
        } else {
            if (gridView != null)
                gridView.setVisibility(View.VISIBLE);
        }
    }

    public void toggleBackDrop() {
        if (!backdropShown) {
            if (backdropContainer != null) {
                backdropContainer.showBackview();
                backdropShown = true;
            }
        } else {
            if (backdropContainer != null) {
                backdropContainer.closeBackview();
                backdropShown = false;
            }
        }
    }

    /**
     * Checks for a active connection
     */
    public void checkConnection() {
        if (listener == null) {
            List<Fragment> fragments = getFragmentManager().getFragments();
            onAttachFragment(fragments.get(0) != null ? fragments.get(0) : fragments.get(1));
        }
        mPhoneConnectionUtil = new PhoneConnectionUtil(getContext());
        if (mPhoneConnectionUtil.isNetworkAvailable()) {
            addDebugText("Connection OK");
            listener.onConnectionOk();
        } else {
            listener.onConnectionFailed();
            setErrorMessage(getString(R.string.error_notConnected));
        }
    }

    /**
     * Handles the success messages
     *
     * @param result Result text to handle
     */
    public void successHandling(String result, boolean displayToast) {
        if (result.equalsIgnoreCase(DomoticzValues.Result.ERROR))
            Toast.makeText(getActivity(), R.string.action_failed, Toast.LENGTH_SHORT).show();
        else if (result.equalsIgnoreCase(DomoticzValues.Result.OK)) {
            if (displayToast)
                Toast.makeText(getActivity(), R.string.action_success, Toast.LENGTH_SHORT).show();
        } else {
            if (displayToast)
                Toast.makeText(getActivity(), R.string.action_unknown, Toast.LENGTH_SHORT).show();
        }
        if (debug) addDebugText("- Result: " + result);
    }

    /**
     * Handles the error messages
     *
     * @param error Exception
     */
    public void errorHandling(Exception error) {
        showSpinner(false);
        error.printStackTrace();
        String errorMessage = StaticHelper.getDomoticz(getActivity()).getErrorMessage(error);

        if (mPhoneConnectionUtil == null)
            mPhoneConnectionUtil = new PhoneConnectionUtil(getContext());
        if (mPhoneConnectionUtil.isNetworkAvailable()) {
            if (errorMessage.contains("No value for result")) {
                setMessage(getString(R.string.no_data_on_domoticz));
            } else {
                setErrorMessage(errorMessage);
            }
        } else {
            if (frameLayout != null) {
                UsefulBits.showSnackbar(getContext(), frameLayout, R.string.error_notConnected, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_notConnected);
            }
        }
    }

    public void setActionbar(String title) {
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).setActionbar(title);
    }

    public void setSortFab(boolean visible) {
        if (getActivity() instanceof MainActivity) {
            if (((MainActivity) getActivity()).fabSort != null)
                ((MainActivity) getActivity()).fabSort.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setErrorMessage(String message) {
        if (debug) addDebugText(message);
        else {
            Logger(fragmentName, message);
            setErrorLayoutMessage(message);
        }
    }

    public void addDebugText(String text) {
        if (text != null && text.length() > 0) {
            Logger(fragmentName, text);
            if (debug) {
                if (debugText != null) {
                    String temp = debugText.getText().toString();
                    if (temp.isEmpty())
                        debugText.setText(text);
                    else {
                        temp = temp + "\n";
                        temp = temp + text;
                        debugText.setText(temp);
                    }
                } else throw new RuntimeException(
                        "Layout should have a TextView defined with the ID \"debugText\"");
            }
        }
    }

    private void setErrorLayoutMessage(String message) {
        hideListView();

        RelativeLayout errorLayout = root.findViewById(R.id.errorLayout);
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);
            TextView errorTextMessage = root.findViewById(R.id.errorTextMessage);
            errorTextMessage.setText(message);
        } else throw new RuntimeException(
                "Layout should have a RelativeLayout defined with the ID of errorLayout");
    }

    public void setMessage(String message) {
        RelativeLayout errorLayout = root.findViewById(R.id.errorLayout);
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);

            MaterialButton settingsButton = root.findViewById(R.id.btnCheckSettings);
            settingsButton.setVisibility(View.GONE);

            ImageView errorImage = root.findViewById(R.id.errorImage);
            errorImage.setVisibility(View.VISIBLE);

            TextView errorTextWrong = root.findViewById(R.id.errorTextWrong);
            errorTextWrong.setVisibility(View.GONE);

            TextView errorTextMessage = root.findViewById(R.id.errorTextMessage);
            errorTextMessage.setText(message);
        } else throw new RuntimeException(
                "Layout should have a RelativeLayout defined with the ID of errorLayout");
    }

    private void hideListView() {
        if (gridView != null) {
            gridView.setVisibility(View.GONE);
        } else throw new RuntimeException(
                "Layout should have a ListView defined with the ID of listView");
    }

    private void showDebugLayout() {
        try {
            if (root != null) {
                LinearLayout debugLayout = root.findViewById(R.id.debugLayout);
                if (debugLayout != null) {
                    debugLayout.setVisibility(View.VISIBLE);

                    debugText = root.findViewById(R.id.debugText);
                    if (debugText != null) {
                        debugText.setMovementMethod(new ScrollingMovementMethod());
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void Logger(String tag, String text) {
        if (!UsefulBits.isEmpty(tag) && !UsefulBits.isEmpty(text))
            Log.d(tag, text);
    }

    public void Filter(String text) {
    }

    public void refreshFragment() {
    }
}
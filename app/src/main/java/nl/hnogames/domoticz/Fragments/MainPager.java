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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.app.RefreshFragment;

public class MainPager extends RefreshFragment implements DomoticzFragmentListener {
    private static final String TAG = MainPager.class.getSimpleName();
    private Context context;
    private FragmentStatePagerAdapter adapterViewPager;
    private BottomNavigationView bottomNavigation;
    private ViewPager vpPager;
    private SharedPrefUtil mSharedPrefs;
    private RelativeLayout root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void RefreshFragment() {
        Fragment f = (Fragment) vpPager
                .getAdapter()
                .instantiateItem(vpPager, vpPager.getCurrentItem());
        if (f instanceof DomoticzRecyclerFragment) {
            ((DomoticzRecyclerFragment) f).refreshFragment();
        } else if (f instanceof DomoticzCardFragment)
            ((DomoticzCardFragment) f).refreshFragment();
        else if (f instanceof DomoticzDashboardFragment)
            ((DomoticzDashboardFragment) f).refreshFragment();
        else if (f instanceof RefreshFragment)
            ((RefreshFragment) f).RefreshFragment();
    }

    @Override
    public void Filter(String newText) {
        Fragment n = (Fragment) vpPager
                .getAdapter()
                .instantiateItem(vpPager, vpPager.getCurrentItem());
        if (n instanceof DomoticzDashboardFragment) {
            ((DomoticzDashboardFragment) n).Filter(newText);
        } else if (n instanceof DomoticzRecyclerFragment) {
            ((DomoticzRecyclerFragment) n).Filter(newText);
        } else if (n instanceof RefreshFragment) {
            ((RefreshFragment) n).Filter(newText);
        }
    }

    @Override
    public void sortFragment(String selectedSort) {
        Fragment f = (Fragment) vpPager
                .getAdapter()
                .instantiateItem(vpPager, vpPager.getCurrentItem());
        if (f instanceof DomoticzRecyclerFragment) {
            ((DomoticzRecyclerFragment) f).sortFragment(selectedSort);
        } else if (f instanceof DomoticzDashboardFragment) {
            ((DomoticzDashboardFragment) f).sortFragment(selectedSort);
        } else if (f instanceof RefreshFragment) {
            ((RefreshFragment) f).sortFragment(selectedSort);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(getActivity());
        root = (RelativeLayout) inflater.inflate(R.layout.mainpager, null);
        initViews();
        return root;
    }

    @NonNull
    private void initViews() {
        vpPager = root.findViewById(R.id.vpPager);
        bottomNavigation = root.findViewById(R.id.BottomNavigation);
        if (mSharedPrefs.darkThemeEnabled()) {
            bottomNavigation.setBackgroundColor(getResources().getColor(R.color.background_dark));
        }
        adapterViewPager = new MainPagerAdapter(((AppCompatActivity) context), 5);
        vpPager.setAdapter(adapterViewPager);
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.getMenu().getItem(position).setChecked(true);
                SetTitle(GetTitle(position));
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).clearSearch();
                RefreshFragment();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bbn_dashboard:
                        vpPager.setCurrentItem(0);
                        SetTitle(GetTitle(0));
                        break;
                    case R.id.bbn_switches:
                        vpPager.setCurrentItem(1);
                        SetTitle(GetTitle(1));
                        break;
                    case R.id.bbn_scenes:
                        vpPager.setCurrentItem(2);
                        SetTitle(GetTitle(2));
                        break;
                    case R.id.bbn_temperature:
                        vpPager.setCurrentItem(3);
                        SetTitle(GetTitle(3));
                        break;
                    case R.id.bbn_weather:
                        vpPager.setCurrentItem(4);
                        SetTitle(GetTitle(4));
                        break;
                }
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).clearSearch();
                RefreshFragment();
                return false;
            }
        });
    }

    public int GetTitle(int position) {
        switch (position) {
            case 0:
                return R.string.title_dashboard;
            case 1:
                return R.string.title_switches;
            case 2:
                return R.string.title_scenes;
            case 3:
                return R.string.title_temperature;
            case 4:
                return R.string.title_weather;
            default:
                return R.string.title_dashboard;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        SetTitle(R.string.title_dashboard);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    private void SetTitle(int title) {
        if (getActionBar() != null)
            getActionBar().setTitle(title);
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) context).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        Log.i(TAG, "Connection OK MainPager");
    }

    @Override
    public void onConnectionFailed() {
        Log.i(TAG, "Connection Failed MainPager");
    }

    public static class MainPagerAdapter extends FragmentStatePagerAdapter {
        private final int mCount;

        public MainPagerAdapter(final AppCompatActivity activity, int count) {
            super(activity.getSupportFragmentManager());
            this.mCount = count;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return mCount;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Dashboard();
                case 1:
                    return new Switches();
                case 2:
                    return new Scenes();
                case 3:
                    return new Temperature();
                case 4:
                    return new Weather();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }
}
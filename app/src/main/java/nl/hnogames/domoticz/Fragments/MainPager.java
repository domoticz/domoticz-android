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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class MainPager extends Fragment implements DomoticzFragmentListener {
    private static final String TAG = MainPager.class.getSimpleName();
    private Context context;
    private FragmentStatePagerAdapter adapterViewPager;
    private BottomNavigation bottomNavigation;
    private ViewPager vpPager;
    private SharedPrefUtil mSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(getActivity());
        RelativeLayout group = (RelativeLayout) inflater.inflate(R.layout.mainpager, null);

        vpPager = group.findViewById(R.id.vpPager);
        bottomNavigation = group.findViewById(R.id.BottomNavigation);
        if (mSharedPrefs.darkThemeEnabled()) {
            bottomNavigation.setBackgroundColor(getResources().getColor(R.color.background_dark));
        }
        adapterViewPager = new MainPagerAdapter(((AppCompatActivity) context), 5);
        vpPager.setAdapter(adapterViewPager);
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                // bottomNavigation.setSelectedIndex(i, false);
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setSelectedIndex(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        bottomNavigation.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int itemId, int position, boolean fromUser) {
                if (fromUser) vpPager.setCurrentItem(position);
            }
            @Override
            public void onMenuItemReselect(int itemId, int position, boolean fromUser) {
                if (fromUser) vpPager.setCurrentItem(position);
            }
        });

        return group;
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
        Log.i(TAG,"Connection OK MainPager");
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
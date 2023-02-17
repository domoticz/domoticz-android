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

package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ftinc.scoop.Flavor;
import com.ftinc.scoop.Scoop;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.FlavorRecyclerAdapter;

public class ScoopSettingsActivity extends AppCompatActivity implements FlavorRecyclerAdapter.OnItemClickListener {

    private static final String EXTRA_TITLE = "com.ftinc.scoop.intent.EXTRA_TITLE";
    private Toolbar mAppBar;
    private RecyclerView mRecyclerView;
    private FlavorRecyclerAdapter mAdapter;
    private String mTitle;

    public static Intent createIntent(Context ctx) {
        return createIntent(ctx, null);
    }

    public static Intent createIntent(Context ctx, @StringRes int titleResId) {
        return createIntent(ctx, ctx.getString(titleResId));
    }

    public static Intent createIntent(Context ctx, @Nullable String title) {
        Intent intent = new Intent(ctx, ScoopSettingsActivity.class);
        if (!TextUtils.isEmpty(title)) intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the current flavor of ice cream
        Scoop.getInstance().apply(this);

        // Set the activity content
        setContentView(R.layout.activity_theme_settings);

        // Setup UI
        parseExtras(savedInstanceState);
        setupActionBar();
        setupRecyclerView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_TITLE, mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void parseExtras(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mTitle = getIntent().getStringExtra(EXTRA_TITLE);
        }
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(EXTRA_TITLE);
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() == null) {
            mAppBar = findViewById(R.id.toolbar);
            setSupportActionBar(mAppBar);
            mAppBar.setVisibility(View.VISIBLE);
            mAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (TextUtils.isEmpty(mTitle)) {
            getSupportActionBar().setTitle(R.string.activity_settings);
        } else {
            getSupportActionBar().setTitle(mTitle);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler);

        mAdapter = new FlavorRecyclerAdapter(this);
        mAdapter.setItemClickListener(this);
        mAdapter.addAll(Scoop.getInstance().getFlavors());
        mAdapter.setCurrentFlavor(Scoop.getInstance().getCurrentFlavor());

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClicked(View view, Flavor item, int position) {
        // Update Scoops
        Scoop.getInstance().choose(item);

        // Update adapter
        mAdapter.setCurrentFlavor(item);

        // Restart this activity
        Intent restart = new Intent(this, ScoopSettingsActivity.class);
        setResult(RESULT_OK);
        finish();
        startActivity(restart);
        overridePendingTransition(0, 0);
    }
}

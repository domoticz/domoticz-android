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

package nl.hnogames.domoticz;

import android.os.Bundle;
import android.view.MenuItem;

import com.ftinc.scoop.Scoop;

import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.fragments.Camera;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class CameraActivity extends AppCompatAssistActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);

        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            this.finish();

        if (bundle != null) {
            //noinspection SpellCheckingInspection
            String imageUrl = bundle.getString("IMAGEURL");

            //noinspection SpellCheckingInspection
            String title = bundle.getString("IMAGETITLE");
            this.setTitle(title);

            Camera camera = new Camera();
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                    camera).commit();
            camera.setImage(imageUrl);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
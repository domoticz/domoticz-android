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

package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.GraphPointInfo;
import nl.hnogames.domoticzapi.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

@SuppressWarnings("unused")
public class GraphDataParser implements JSONParserInterface {

    private static final String TAG = GraphDataParser.class.getSimpleName();
    private GraphDataReceiver varsReceiver;

    public GraphDataParser(GraphDataReceiver varsReceiver) {
        this.varsReceiver = varsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<GraphPointInfo> mVars = new ArrayList<>();
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mVars.add(new GraphPointInfo(row));
                }
            }

            if (mVars == null || mVars.size() <= 0)
                onError(new NullPointerException(
                        "No Data found in Domoticz."));
            else
                varsReceiver.onReceive(mVars);

        } catch (JSONException e) {
            Log.e(TAG, "GraphDataParser JSON exception");
            e.printStackTrace();
            varsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "GraphDataParser of JSONParserInterface exception");
        varsReceiver.onError(error);
    }
}
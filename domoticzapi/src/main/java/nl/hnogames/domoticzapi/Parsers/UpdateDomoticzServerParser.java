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

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.UpdateDomoticzServerReceiver;
import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class UpdateDomoticzServerParser implements JSONParserInterface {

    private static final String TAG = UpdateDomoticzServerParser.class.getSimpleName();
    private UpdateDomoticzServerReceiver receiver;

    public UpdateDomoticzServerParser(@Nullable UpdateDomoticzServerReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        Log.d(TAG, result);
        try {
            JSONObject response = new JSONObject(result);
            Log.d(TAG, response.toString());
            boolean updateSuccess = false;
            String resultText;

            if (response.has("status")) {
                resultText = response.getString("status");
                if (!UsefulBits.isEmpty(resultText))
                    updateSuccess = true;
            } else updateSuccess = false;

            if (receiver != null) receiver.onUpdateFinish(updateSuccess);
        } catch (JSONException e) {
            if (receiver != null) receiver.onError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "VersionParser of JSONParserInterface exception");
        if (receiver != null) receiver.onError(error);
    }
}
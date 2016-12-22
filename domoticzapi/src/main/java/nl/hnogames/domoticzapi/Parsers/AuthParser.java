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

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.AuthInfo;
import nl.hnogames.domoticzapi.Interfaces.AuthReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

public class AuthParser implements JSONParserInterface {

    private static final String TAG = AuthParser.class.getSimpleName();
    private AuthReceiver receiver;

    public AuthParser(AuthReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            AuthInfo mAuthInfo = new AuthInfo(new JSONObject(result));
            receiver.onReceiveAuthentication(mAuthInfo);
        } catch (JSONException e) {
            Log.e(TAG, "AuthParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "AuthParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}
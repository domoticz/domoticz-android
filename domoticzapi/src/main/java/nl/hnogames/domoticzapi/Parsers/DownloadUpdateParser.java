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

import nl.hnogames.domoticzapi.Interfaces.DownloadUpdateServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class DownloadUpdateParser implements JSONParserInterface {

    private static final String TAG = DownloadUpdateParser.class.getSimpleName();
    private DownloadUpdateServerReceiver receiver;

    public DownloadUpdateParser(DownloadUpdateServerReceiver receiver) {
        this.receiver = receiver;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void parseResult(String result) {
        try {
            if (!UsefulBits.isEmpty(result) && result.contains("ERR"))
                receiver.onDownloadStarted(false);
            else
                receiver.onDownloadStarted(true);
        } catch (Exception error) {
            receiver.onError(error);
            error.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "DownloadUpdateParser of Exception");
        receiver.onError(error);
    }
}
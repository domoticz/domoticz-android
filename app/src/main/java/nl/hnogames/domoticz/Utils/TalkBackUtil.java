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

package nl.hnogames.domoticz.Utils;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class TalkBackUtil {
    private TextToSpeech oTalkBack;
    private Context mContext;
    private InitListener initListener;

    public void Init(Context context, InitListener i) {
        initListener = i;
        this.mContext = context;
        oTalkBack = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    initListener.onInit(status);
                }
            }
        });
    }

    public void Init(Context context, final Locale l, InitListener i) {
        initListener = i;
        this.mContext = context;
        oTalkBack = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    oTalkBack.setLanguage(l);
                    initListener.onInit(status);
                }
            }
        });
    }

    public void Talk(String text) {
        if (UsefulBits.isEmpty(text) || mContext == null)
            return;

        if (oTalkBack == null)
            return; //should first call init

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId = mContext.hashCode() + "";
            oTalkBack.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            oTalkBack.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    public void Stop() {
        if (oTalkBack != null) {
            oTalkBack.stop();
            oTalkBack.shutdown();
        }
    }

    public interface InitListener {
        void onInit(int status);
    }

}

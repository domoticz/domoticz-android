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

package nl.hnogames.domoticzapi.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class SessionUtil {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "SID";
    private static final String COOKIE_EXPIRE_KEY = "CookieExpire";
    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionUtil(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    public final void clearSessionCookie() {
        editor.putString(SESSION_COOKIE, "");
        editor.commit();
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            String expires = "";

            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                editor.putString(SESSION_COOKIE, cookie);
                editor.commit();

                for (String s : splitCookie) {
                    if (s.indexOf("Expires") > 0) {
                        String[] splitExpires = s.split("=");
                        expires = splitExpires[1];
                        editor.putString(COOKIE_EXPIRE_KEY, expires);
                        editor.commit();
                    }
                }
            }
        }
    }

    public Calendar parseStringToDate(String expires) {

        if (UsefulBits.isEmpty(expires)) return null;

        expires = expires.trim();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);

        try {
            cal.setTime(sdf.parse(expires));
            return cal;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds session cookie to headers if exists.
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = prefs.getString(SESSION_COOKIE, "");
        String expires = prefs.getString(COOKIE_EXPIRE_KEY, "");

        Calendar calExpired = parseStringToDate(expires);
        if (calExpired != null) {
            Calendar calNow = Calendar.getInstance();
            if (calExpired.after(calNow)) {
                //session id still valid!
                if (sessionId.length() > 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(SESSION_COOKIE);
                    builder.append("=");
                    builder.append(sessionId);
                    if (headers.containsKey(COOKIE_KEY)) {
                        builder.append("; ");
                        builder.append(headers.get(COOKIE_KEY));
                    }
                    headers.put(COOKIE_KEY, builder.toString());
                }
            }
        }
    }
}

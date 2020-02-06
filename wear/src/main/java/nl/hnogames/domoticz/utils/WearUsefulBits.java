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

package nl.hnogames.domoticz.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class WearUsefulBits {

    public static boolean isEmpty(String string) {
        //noinspection SimplifiableIfStatement
        if (string != null)
            return string.equalsIgnoreCase("")
                    || string.isEmpty()
                    || string.length() <= 0;
        else return true;
    }

    public static boolean isEmpty(CharSequence charSequence) {
        //noinspection SimplifiableIfStatement
        if (charSequence != null)
            return charSequence.length() <= 0;
        else return true;
    }

    public static String newLine() {
        return System.getProperty("line.separator");
    }

    /**
     * @param text to be validated
     * @return if the text is base 64 encoded or not
     */
    public static boolean isBase64Encoded(String text) {
        return text.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
    }

    /**
     * @param text input value
     * @return decode base 64 text
     */
    public static String decodeBase64(String text) {
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * @param text input value
     * @return encode base 64 text
     */
    public static String encodeBase64(String text) {
        try {
            return Base64.encodeToString(text.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
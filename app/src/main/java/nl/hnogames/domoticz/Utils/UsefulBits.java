/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UsefulBits {

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

    public static double[] rgb2hsv(int red, int green, int blue) {
        double computedH = 0, computedS = 0, computedV = 0;
        double r = 0;
        double g = 0;
        double b = 0;

        if (red < 0 || green < 0 || blue < 0 || red > 255 || green > 255 || blue > 255) {
            return null;
        }

        r = (double) red / 255;
        g = (double) green / 255;
        b = (double) blue / 255;

        double minRGB = Math.min(r, Math.min(g, b));
        double maxRGB = Math.max(r, Math.max(g, b));

        // Black-gray-white
        if (minRGB == maxRGB) {
            computedV = minRGB;
            return new double[]{0, 0, computedV};
        }

        // Colors other than black-gray-white:
        double d = (r == minRGB) ? g - b : ((b == minRGB) ? r - g : b - r);
        double h = (r == minRGB) ? 3 : ((b == minRGB) ? 1 : 5);
        computedH = 60 * (h - d / (maxRGB - minRGB));
        computedS = (maxRGB - minRGB) / maxRGB;
        computedV = maxRGB;

        return new double[]{computedH, computedS, computedV};
    }


    public static String getMd5String(String password) {
        StringBuffer hexString = new StringBuffer();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());

            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hexString.toString();
    }

    public static void copyToClipboard(Context mContext, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }
}
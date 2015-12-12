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

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.hnogames.domoticz.R;

public class ColorsUtil {

    @SuppressWarnings("unused")
    private static final String TAG = ColorsUtil.class.getSimpleName();

    public static int getRandomMaterialColor(Context mContext) {
        try {
            List<Integer> allColors = getAllMaterialColors(mContext);
            return allColors.get(new Random().nextInt(allColors.size()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Integer> getAllMaterialColors(Context mContext) throws IOException, XmlPullParserException {
        XmlResourceParser xrp = mContext.getResources().getXml(R.xml.android_material_design_colours);
        List<Integer> allColors = new ArrayList<>();
        int nextEvent;
        while ((nextEvent = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
            String s = xrp.getName();
            if ("color".equals(s)) {
                String color = xrp.nextText();
                allColors.add(Color.parseColor(color));
            }
        }
        return allColors;
    }
}
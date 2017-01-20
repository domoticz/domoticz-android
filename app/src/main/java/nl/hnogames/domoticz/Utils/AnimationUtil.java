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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import nl.hnogames.domoticz.R;

public class AnimationUtil {

    private static Animation enterFromAbove(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.enter_from_above);
    }

    private static Animation exitToAbove(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.exit_to_above);
    }

    private static Animation enterFromRight(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
    }

    private static Animation exitToRight(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    /**
     * Returns the default opening log row animation
     *
     * @param context Context
     * @return the animation
     */
    public static Animation getLogRowAnimationOpen(Context context) {
        return enterFromRight(context);
    }

    /**
     * Returns the default closing log row animation
     *
     * @param context Context
     * @return the animation
     */
    public static Animation getLogRowAnimationClose(Context context) {
        return exitToRight(context);
    }
}
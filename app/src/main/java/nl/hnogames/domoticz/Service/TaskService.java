/**
 * Copyright (C) 2015 Domoticz
 * <p/>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package nl.hnogames.domoticz.Service;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class TaskService extends GcmTaskService {

    @Override
    public void onInitializeTasks() {
        // When Google Play Services or the client app is updated,
        // all scheduled tasks are removed.
        //
        // You can override this method to reschedule them in the case of
        // an updated package. This is not called when your application is first installed.
        //
        // This is called on your application's main thread.

        // Tasks are wiped by system, clear our own flag
        new SharedPrefUtil(this).setTaskIsScheduled(false);
        UsefulBits.setScheduledTasks(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();

        if (tag.equals(UsefulBits.TASK_TAG_PERIODIC) || tag.equals("TEST")) {
            final boolean forceUpdate = true;                                     // Force update
            //noinspection ConstantConditions
            UsefulBits.saveServerConfigToActiveServer(this, forceUpdate, false);
            //noinspection ConstantConditions
            UsefulBits.checkDownloadedLanguage(this, null, forceUpdate, true);
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
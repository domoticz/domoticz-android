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

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import nl.hnogames.domoticz.Containers.AuthInfo;
import nl.hnogames.domoticz.Containers.ConfigInfo;
import nl.hnogames.domoticz.Containers.UserInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.AuthReceiver;
import nl.hnogames.domoticz.Interfaces.ConfigReceiver;
import nl.hnogames.domoticz.Interfaces.UsersReceiver;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Service.TaskService;

public class UsefulBits {

    public static final String TASK_TAG_PERIODIC = "taskPeriodic";

    private static final int RC_PLAY_SERVICES = 123;

    @SuppressWarnings("FieldCanBeLocal")
    private static final int DAYS_TO_CHECK_FOR_SERVER_CONFIG = 5;
    private static final String TAG = UsefulBits.class.getSimpleName();

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

    public static char getDegreeSymbol() {
        return '\u00B0';
    }

    public static String createUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static double[] rgb2hsv(int red, int green, int blue) {
        double computedH, computedS, computedV;
        double r, g, b;

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
        StringBuilder hexString = new StringBuilder();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());

            byte[] hash = md.digest();
            for (byte aHash : hash) {
                if ((0xff & aHash) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & aHash)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & aHash));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hexString.toString();
    }

    /**
     * Convert Byte array into Hex
     *
     * @param in_array byte array to convert
     */
    public static String ByteArrayToHexString(byte[] in_array) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < in_array.length; ++j) {
            in = (int) in_array[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * Set's the display language of the app
     *
     * @param context Context
     * @param lang    Language to display
     */
    public static void setDisplayLanguage(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Get's the display language of the phone
     *
     * @return Returns the phone display language
     */
    public static String getPhoneDisplayLocale() {
        if (!isEmpty(Locale.getDefault().getLanguage()))
            return Locale.getDefault().getLanguage();
        else {
            Log.d(TAG, "No valid language detected, using en");
            return "en";
        }
    }

    /**
     * Get the active language. Either the user chosen or the phone display language
     *
     * @param context Context
     * @return Returns the active language
     */
    public static String getActiveLanguage(Context context) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);

        String userDisplayLanguage = mSharedPrefs.getDisplayLanguage();
        String phoneDisplayLanguage = getPhoneDisplayLocale();

        if (!isEmpty(userDisplayLanguage)) {
            Log.d(TAG, "User specified language to use: " + userDisplayLanguage);
            return userDisplayLanguage;
        } else {
            Log.d(TAG, "User didn't specify language to use: using display language");
            return phoneDisplayLanguage;
        }
    }

    /**
     * Checks the already downloaded languages if they are the correct ones
     * If not downloaded it will download them
     *
     * @param context       Context
     * @param forceDownload Force downloading the language anyway
     */
    public static void checkDownloadedLanguage(Context context, ServerUtil serverUtil, boolean forceDownload, boolean fromService) {

        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);
        String downloadedLanguage = mSharedPrefs.getDownloadedLanguage();
        String activeLanguage = UsefulBits.getActiveLanguage(context);

        if (serverUtil == null)
            serverUtil = new ServerUtil(context);

        if (mSharedPrefs.getSavedLanguage() == null || forceDownload) {
            // Language files aren't there or should be downloaded anyway, let's download them
            Log.d(TAG, "Downloading language files. Forced: " + forceDownload);
            mSharedPrefs.getLanguageStringsFromServer(activeLanguage.toLowerCase(), serverUtil);
            if (mSharedPrefs.isDebugEnabled()) {
                if (forceDownload && !fromService) {
                    showSimpleToast(context, "Language files downloaded because it was forced", Toast.LENGTH_SHORT);
                } else if (!fromService)
                    showSimpleToast(context, "Language files downloaded because there were none", Toast.LENGTH_SHORT);
            }
        } else {
            long dateMillis = mSharedPrefs.getSavedLanguageDate();
            String dateStr = UsefulBits.getFormattedDate(context, dateMillis);
            Log.d(TAG, "Language files are dated: " + dateStr);

            if (mSharedPrefs.isDebugEnabled() && !fromService)
                showSimpleToast(context, "Language files are dated: " + dateStr, Toast.LENGTH_SHORT);

            // check if downloaded files are the correct ones
            if (!downloadedLanguage.equalsIgnoreCase(activeLanguage)) {

                if (mSharedPrefs.isDebugEnabled() && !fromService)
                    showSimpleToast(context, "Downloaded language files did not match the preferred language", Toast.LENGTH_SHORT);

                Log.d(TAG, "Downloaded language files did not match the preferred language:" + newLine()
                        + "Current downloaded language: " + downloadedLanguage + newLine()
                        + "Active language: " + activeLanguage + newLine()
                        + "Downloading the correct language");
                mSharedPrefs.getLanguageStringsFromServer(activeLanguage.toLowerCase(), serverUtil);
            }
        }
    }

    /**
     * Restarts the application
     *
     * @param activity to restart
     */
    public static void restartApplication(Activity activity) {
        Intent refresh = new Intent(activity, MainActivity.class);
        activity.finish();
        activity.startActivity(refresh);
    }

    /**
     * Copy text to the users clipboard
     *
     * @param mContext Context
     * @param label    Label of the to copy text
     * @param text     Text to copy
     */
    public static void copyToClipboard(Context mContext, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Get's the relative formatted date
     *
     * @param mContext     Context
     * @param timeInMillis Time in milliseconds to format
     * @return Formatted date string
     */
    public static String getFormattedDate(Context mContext, long timeInMillis) {
        return DateUtils.getRelativeTimeSpanString(mContext, timeInMillis, false).toString();
    }

    /**
     * Calculates the difference in days between day1 and day 2
     *
     * @param day1 Day 1
     * @param day2 Day 2
     * @return The difference
     */
    public static int differenceInDays(long day1, long day2) {
        return (int) (day1 - day2) / (24 * 60 * 60 * 1000);
    }

    /**
     * Sets the scheduled task for maintenance use in the {@link TaskService} class
     *
     * @param context Context to use
     */
    public static void setScheduledTasks(Context context) {
        final SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(context);

        if (!mSharedPrefUtil.getTaskIsScheduled()) {
            // Only when not already scheduled

            if (mSharedPrefUtil.isDebugEnabled())
                showSimpleToast(context, "Scheduling new task", Toast.LENGTH_SHORT);

            GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);

            @SuppressWarnings("PointlessArithmeticExpression")
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(TaskService.class)                      // Service to start
                    .setPersisted(true)                                 // Will survive reboots
                    .setTag(TASK_TAG_PERIODIC)                          // Schedule periodic
                    .setPeriod(60 * 60 * 24 * 1)                        // Every day
                    .setFlex(60 * 60 * 8)                               // Flex of 8 hours
                    .setRequiredNetwork(Task.NETWORK_STATE_UNMETERED)   // Only un metered networks
                    .setRequiresCharging(true)                          // Only when charging
                    .build();

            mGcmNetworkManager.schedule(task);
            mSharedPrefUtil.setTaskIsScheduled(true);
        } else if (mSharedPrefUtil.isDebugEnabled())
            showSimpleToast(context, "Tasks already scheduled", Toast.LENGTH_SHORT);
    }

    /**
     * Get's the config from the server data but only if it's older then 5 days
     *
     * @param context Context
     * @param forced  Force update the config
     */
    public static void getServerConfigForActiveServer(final Context context, boolean forced, final ConfigReceiver receiver, final ConfigInfo currentConfig) {
        final ServerUtil mServerUtil = new ServerUtil(context);
        final Domoticz domoticz = new Domoticz(context, mServerUtil);
        final long currentTime = Calendar.getInstance().getTimeInMillis();

        if (currentConfig != null && !forced) {
            final long dateOfConfig = currentConfig.getDateOfConfig();
            int age = UsefulBits.differenceInDays(dateOfConfig, currentTime);
            if (age < DAYS_TO_CHECK_FOR_SERVER_CONFIG) {
                Log.i(TAG, "Skipping ConfigInfo fetch which is " + String.valueOf(age) + " days old");
                if (domoticz.isDebugEnabled())
                    showSimpleToast(context,
                            "Skipping ConfigInfo fetch which is only "
                                    + String.valueOf(age)
                                    + " days old (max is: "
                                    + String.valueOf(DAYS_TO_CHECK_FOR_SERVER_CONFIG)
                                    + " days old)", Toast.LENGTH_SHORT);
                receiver.onReceiveConfig(currentConfig);
                return;
            }
        }

        // Get Domoticz server configuration
        domoticz.getConfig(new ConfigReceiver() {
            @Override
            public void onReceiveConfig(final ConfigInfo configInfo) {
                if (configInfo != null) {
                    configInfo.setDateOfConfig(currentTime);
                    domoticz.getUsers(new UsersReceiver() {
                        @Override
                        public void onReceiveUsers(final ArrayList<UserInfo> mUserInfo) {
                            if (mUserInfo != null) {
                                domoticz.getUserAuthenticationRights(new AuthReceiver() {
                                    @Override
                                    public void onReceiveAuthentication(AuthInfo auth) {
                                        ArrayList<UserInfo> mDetailUserInfo = mUserInfo;
                                        //also add current user
                                        UserInfo currentUser = new UserInfo(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME),
                                                UsefulBits.getMd5String(domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD)),
                                                auth.getRights());

                                        mDetailUserInfo.add(currentUser);
                                        configInfo.setUsers(mDetailUserInfo);
                                        mServerUtil.getActiveServer().setConfigInfo(context, configInfo);
                                        mServerUtil.saveDomoticzServers(true);

                                        if (receiver != null)
                                            receiver.onReceiveConfig(configInfo);
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                    }
                                });
                            } else {
                                mServerUtil.getActiveServer().setConfigInfo(context, configInfo);
                                mServerUtil.saveDomoticzServers(true);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            if (currentConfig != null) {
                                configInfo.setUsers(currentConfig.getUsers());
                            }

                            if (receiver != null)
                                receiver.onReceiveConfig(configInfo);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception error) {
                showSimpleToast(context, String.format(
                        context.getString(R.string.error_couldNotCheckForConfig),
                        domoticz.getErrorMessage(error)), Toast.LENGTH_SHORT);
            }
        });
    }

    public static boolean checkPlayServicesAvailable(final Activity activity) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(activity);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error.
                availability.getErrorDialog(activity, resultCode, RC_PLAY_SERVICES).show();
            } else {
                // Unresolvable error
                Log.e(TAG, "Google Play services is unavailable.");
                showSimpleToast(activity,
                        activity.getString(R.string.google_play_services_unavailable), Toast.LENGTH_SHORT);
                return false;
            }
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Google Play services is available.");
        }
        return true;
    }

    public static void showSimpleToast(Context context, String message, int length) {
        try {
            Toast.makeText(context, message, length).show();
        } catch (Exception ex) {
        }
    }

    public static void showSimpleSnackbar(Context context, CoordinatorLayout coordinatorLayout, int message_resource_id, int length) {
        try {
            if (context != null && coordinatorLayout != null)
                showSimpleSnackbar(context, coordinatorLayout, context.getString(message_resource_id), length);
        } catch (Exception ex) {
        }
    }

    public static void showSimpleSnackbar(Context context, CoordinatorLayout coordinatorLayout, String message, int length) {
        try {
            if (context != null && coordinatorLayout != null && !UsefulBits.isEmpty(message))
                Snackbar.make(coordinatorLayout, message, length).show();
        } catch (Exception ex) {
        }
    }

    public static void showSnackbar(Context context, CoordinatorLayout coordinatorLayout, String message, int length,
                                    Snackbar.Callback callback,
                                    View.OnClickListener onclickListener, String actiontext) {
        try {
            if (context != null &&
                    coordinatorLayout != null &&
                    !UsefulBits.isEmpty(message)) {
                if (onclickListener == null || UsefulBits.isEmpty(actiontext)) {
                    if (callback != null) {
                        Snackbar.make(coordinatorLayout, message, length)
                                .setCallback(callback)
                                .show();
                    } else {
                        Snackbar.make(coordinatorLayout, message, length)
                                .show();
                    }
                } else {
                    if (callback != null) {
                        Snackbar.make(coordinatorLayout, message, length)
                                .setAction(actiontext, onclickListener)
                                .setCallback(callback)
                                .show();
                    } else {
                        Snackbar.make(coordinatorLayout, message, length)
                                .setAction(actiontext, onclickListener)
                                .show();
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
}
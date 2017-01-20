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
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Domoticz;

public class ServerUtil {

    private final String SERVER_PREFS = "remote_servers";
    private final String SERVER_PREFS_ACTIVE = "active_server";

    private Context mContext;

    private ServerInfo mActiveServer;
    private ArrayList<ServerInfo> mServerList;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Gson gson;

    /**
     * Constructor ServerUtils
     *
     * @param mContext Application Context
     */
    public ServerUtil(Context mContext) {
        this.mContext = mContext;

        GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        gson = gsonBuilder.create();

        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        loadDomoticzServers();

        if (mServerList == null || mServerList.size() <= 0) {
            //try to recover from previous settings framework:
            Log.i(this.getClass().getSimpleName(), "Recovering old settings to new Object model");
            loadPreviousSettingsToServerInfo();
        }
    }

    /**
     * Load settings from sharedprefs
     */
    private void loadDomoticzServers() {
        String serverSettings = prefs.getString(SERVER_PREFS, "");

        if (!UsefulBits.isEmpty(serverSettings)) {
            mServerList = new ArrayList<>();
            String JSON_VALUE_PAIRS = "nameValuePairs";
            String JSON_CONFIG_INFO = "configInfo";
            String JSON_OBJECT = "jsonObject";
            String serverUpdateInfoJSON_SERVER_UPDATE_INFO = "serverUpdateInfo";
            try {
                JSONArray jsonSettingsArray = new JSONArray(serverSettings);
                for (int i = 0; i < jsonSettingsArray.length(); i++) {
                    JSONObject jsonServer = jsonSettingsArray.getJSONObject(i);
                    ServerInfo oPrefServer = new ServerInfo();
                    oPrefServer.setServerName(jsonServer.getString("SERVER_NAME"));
                    if (jsonServer.has("SERVER_UNIQUE_ID"))
                        oPrefServer.setServerUniqueId(jsonServer.getString("SERVER_UNIQUE_ID"));
                    oPrefServer.setRemoteServerUsername(jsonServer.getString("REMOTE_SERVER_USERNAME"));
                    oPrefServer.setRemoteServerPassword(jsonServer.getString("REMOTE_SERVER_PASSWORD"));
                    oPrefServer.setRemoteServerUrl(jsonServer.getString("REMOTE_SERVER_URL"));
                    oPrefServer.setRemoteServerDirectory(jsonServer.getString("REMOTE_SERVER_DIRECTORY"));
                    oPrefServer.setRemoteServerPort(jsonServer.getString("REMOTE_SERVER_PORT"));
                    oPrefServer.setRemoteServerSecure(jsonServer.getBoolean("REMOTE_SERVER_SECURE"));
                    oPrefServer.setRemoteServerAuthentication(jsonServer.getBoolean("REMOTE_SERVER_AUTHENTICATION_METHOD"));
                    oPrefServer.setIsLocalServerAddressDifferent(jsonServer.getBoolean("IS_LOCAL_SERVER_ADDRESS_DIFFERENT"));
                    oPrefServer.setUseOnlyLocal(jsonServer.getBoolean("USE_ONLY_LOCAL"));
                    oPrefServer.setLocalServerUsername(jsonServer.getString("LOCAL_SERVER_USERNAME"));
                    oPrefServer.setLocalServerPassword(jsonServer.getString("LOCAL_SERVER_PASSWORD"));
                    oPrefServer.setLocalServerUrl(jsonServer.getString("LOCAL_SERVER_URL"));
                    oPrefServer.setLocalServerPort(jsonServer.getString("LOCAL_SERVER_PORT"));
                    oPrefServer.setLocalServerDirectory(jsonServer.getString("LOCAL_SERVER_DIRECTORY"));
                    oPrefServer.setLocalServerSecure(jsonServer.getBoolean("LOCAL_SERVER_SECURE"));
                    oPrefServer.setLocalServerAuthentication(jsonServer.getBoolean("LOCAL_SERVER_AUTHENTICATION_METHOD"));
                    if (jsonServer.has("LOCAL_SERVER_SSID")) {
                        JSONArray jsonSSIDList = jsonServer.getJSONArray("LOCAL_SERVER_SSID");
                        List<String> ssidList = new ArrayList<>();
                        for (int j = 0; j < jsonSSIDList.length(); j++)
                            ssidList.add(jsonSSIDList.getString(j));
                        oPrefServer.setLocalServerSsid(ssidList);
                    }
                    oPrefServer.setEnabled(jsonServer.getBoolean("ENABLED"));

                    if (jsonServer.has(serverUpdateInfoJSON_SERVER_UPDATE_INFO)) {
                        JSONObject serverUpdateInfoJson = jsonServer.getJSONObject(serverUpdateInfoJSON_SERVER_UPDATE_INFO);
                        JSONObject jsonUpdate = serverUpdateInfoJson.getJSONObject(JSON_OBJECT).getJSONObject(JSON_VALUE_PAIRS);
                        oPrefServer.setServerUpdateInfo(mContext, new ServerUpdateInfo(jsonUpdate));
                    }

                    if (jsonServer.has(JSON_CONFIG_INFO)) {
                        JSONObject configInfoJson = jsonServer.getJSONObject(JSON_CONFIG_INFO);
                        long dateOfConfig = configInfoJson.getLong("dateOfConfig");
                        JSONObject jsonUpdate = configInfoJson.getJSONObject(JSON_OBJECT).getJSONObject(JSON_VALUE_PAIRS);
                        ConfigInfo loadedConfigInfo = new ConfigInfo(jsonUpdate);
                        loadedConfigInfo.setDateOfConfig(dateOfConfig);
                        oPrefServer.setConfigInfo(mContext, loadedConfigInfo);
                    }

                    boolean alreadyContains = false;
                    for (ServerInfo s : mServerList) {
                        if (s.getServerName().equals(oPrefServer.getServerName()))
                            alreadyContains = true;
                    }

                    if (!alreadyContains)
                        mServerList.add(oPrefServer);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mServerList.size() > 0) {
                mActiveServer = mServerList.get(0);         //set default
                String activeServerSettings = prefs.getString(SERVER_PREFS_ACTIVE, "");
                if (!UsefulBits.isEmpty(activeServerSettings)) {
                    JSONObject jsonServer;
                    try {
                        jsonServer = new JSONObject(activeServerSettings);
                        ServerInfo oPrefServer = new ServerInfo();
                        oPrefServer.setServerName(jsonServer.getString("SERVER_NAME"));
                        if (jsonServer.has("SERVER_UNIQUE_ID"))
                            oPrefServer.setServerUniqueId(jsonServer.getString("SERVER_UNIQUE_ID"));
                        oPrefServer.setRemoteServerUsername(jsonServer.getString("REMOTE_SERVER_USERNAME"));
                        oPrefServer.setRemoteServerPassword(jsonServer.getString("REMOTE_SERVER_PASSWORD"));
                        oPrefServer.setRemoteServerUrl(jsonServer.getString("REMOTE_SERVER_URL"));
                        oPrefServer.setRemoteServerDirectory(jsonServer.getString("REMOTE_SERVER_DIRECTORY"));
                        oPrefServer.setRemoteServerPort(jsonServer.getString("REMOTE_SERVER_PORT"));
                        oPrefServer.setRemoteServerSecure(jsonServer.getBoolean("REMOTE_SERVER_SECURE"));
                        oPrefServer.setRemoteServerAuthentication(jsonServer.getBoolean("REMOTE_SERVER_AUTHENTICATION_METHOD"));
                        oPrefServer.setIsLocalServerAddressDifferent(jsonServer.getBoolean("IS_LOCAL_SERVER_ADDRESS_DIFFERENT"));
                        oPrefServer.setUseOnlyLocal(jsonServer.getBoolean("USE_ONLY_LOCAL"));
                        oPrefServer.setLocalServerUsername(jsonServer.getString("LOCAL_SERVER_USERNAME"));
                        oPrefServer.setLocalServerPassword(jsonServer.getString("LOCAL_SERVER_PASSWORD"));
                        oPrefServer.setLocalServerUrl(jsonServer.getString("LOCAL_SERVER_URL"));
                        oPrefServer.setLocalServerPort(jsonServer.getString("LOCAL_SERVER_PORT"));
                        oPrefServer.setLocalServerDirectory(jsonServer.getString("LOCAL_SERVER_DIRECTORY"));
                        oPrefServer.setLocalServerSecure(jsonServer.getBoolean("LOCAL_SERVER_SECURE"));
                        oPrefServer.setEnabled(jsonServer.getBoolean("ENABLED"));

                        if (jsonServer.has("LOCAL_SERVER_SSID")) {
                            JSONArray jsonSSIDList = jsonServer.getJSONArray("LOCAL_SERVER_SSID");
                            List<String> ssidList = new ArrayList<>();
                            for (int j = 0; j < jsonSSIDList.length(); j++)
                                ssidList.add(jsonSSIDList.getString(j));
                            oPrefServer.setLocalServerSsid(ssidList);
                        }

                        if (jsonServer.has(serverUpdateInfoJSON_SERVER_UPDATE_INFO)) {
                            JSONObject serverUpdateInfoJson = jsonServer.getJSONObject(serverUpdateInfoJSON_SERVER_UPDATE_INFO);
                            JSONObject jsonUpdate = serverUpdateInfoJson.getJSONObject(JSON_OBJECT).getJSONObject(JSON_VALUE_PAIRS);
                            oPrefServer.setServerUpdateInfo(mContext, new ServerUpdateInfo(jsonUpdate));
                        }

                        if (jsonServer.has(JSON_CONFIG_INFO)) {
                            JSONObject configInfoJson = jsonServer.getJSONObject(JSON_CONFIG_INFO);
                            long dateOfConfig = configInfoJson.getLong("dateOfConfig");
                            JSONObject jsonUpdate = configInfoJson.getJSONObject(JSON_OBJECT).getJSONObject(JSON_VALUE_PAIRS);
                            ConfigInfo loadedConfigInfo = new ConfigInfo(jsonUpdate);
                            loadedConfigInfo.setDateOfConfig(dateOfConfig);
                            oPrefServer.setConfigInfo(mContext, loadedConfigInfo);
                        }

                        oPrefServer.setLocalServerAuthentication(jsonServer.getBoolean("LOCAL_SERVER_AUTHENTICATION_METHOD"));
                        mActiveServer = oPrefServer;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Saves all Domoticz servers to shared preferences
     *
     * @param saveActiveServer true if active server should be updated
     */
    public void saveDomoticzServers(boolean saveActiveServer) {
        if (saveActiveServer)
            putServerInList(mActiveServer);

        String activeServer = gson.toJson(mActiveServer);
        String serversSettings = gson.toJson(mServerList);

        if (editor == null)
            editor = prefs.edit();

        editor.putString(SERVER_PREFS_ACTIVE, activeServer).apply();
        editor.putString(SERVER_PREFS, serversSettings).apply();
    }

    /**
     * Add a ServerInfo to the list, or update if Server with name already exists
     *
     * @param server ServerInfo object to be saved
     */
    public void putServerInList(ServerInfo server) {
        if (server != null) {
            if (mServerList == null) {
                mServerList = new ArrayList<>();
                mServerList.add(server);
            } else {
                boolean found = false;
                int i = 0;
                for (ServerInfo s : mServerList) {
                    if (s.getServerName().equals(server.getServerName())) {
                        found = true;
                        break;
                    }
                    i++;
                }
                if (found) {
                    mServerList.remove(i);
                }
                mServerList.add(server);
            }
        }
    }

    /**
     * Get current active server settings
     */
    public ServerInfo getActiveServer() {
        if (mActiveServer == null)
            mActiveServer = new ServerInfo();
        return mActiveServer;
    }

    /**
     * Set server as active server
     */
    public void setActiveServer(ServerInfo mActiveServer) {
        this.mActiveServer = mActiveServer;
        saveDomoticzServers(false);
    }

    /**
     * Remove a ServerInfo from the list of servers
     *
     * @param server ServerInfo object to be removed
     */
    public void removeServer(ServerInfo server) {
        if (server == null || mServerList == null || mServerList.size() <= 0)
            return;

        if (!server.getServerName().equals(Domoticz.DOMOTICZ_DEFAULT_SERVER)) {
            boolean found = false;
            int i = 0;
            for (ServerInfo s : mServerList) {
                if (s.getServerName().equals(server.getServerName())) {
                    found = true;
                    break;
                }
                i++;
            }
            if (found) {
                mServerList.remove(i);
            }
        }
        saveDomoticzServers(false);
    }

    /**
     * Get all servers (enabled and disabled)
     */
    public ArrayList<ServerInfo> getServerList() {
        return mServerList;
    }

    /**
     * Get all servers that are Enabled
     */
    public ArrayList<ServerInfo> getEnabledServerList() {
        ArrayList<ServerInfo> activeServers = new ArrayList<>();
        for (ServerInfo s : mServerList) {
            if (s.isEnabled())
                activeServers.add(s);
        }
        return activeServers;
    }

    /**
     * add new Domoticz server to the list
     *
     * @param server ServerInfo add new server to the list
     */
    public boolean addDomoticzServer(ServerInfo server) {
        if (mServerList == null)
            mServerList = new ArrayList<>();

        boolean alreadyContains = false;
        for (ServerInfo s : mServerList) {
            if (s.getServerName().equals(server.getServerName()))
                alreadyContains = true;
        }

        if (!alreadyContains) {
            mServerList.add(server);
            saveDomoticzServers(false);
            return true;
        } else
            return false;
    }

    /**
     * Check if server name is unique
     *
     * @param server ServerInfo to check the name from
     */
    public boolean checkUniqueServerName(ServerInfo server) {
        boolean alreadyContains = false;
        for (ServerInfo s : mServerList) {
            if (s.getServerName().equals(server.getServerName()))
                alreadyContains = true;
        }
        return !alreadyContains;
    }

    /**
     * Update / Save specific ServerInfo object (so not the active!!)
     *
     * @param server ServerInfo to be saved
     */
    public void updateServerInfo(ServerInfo server) {
        putServerInList(server);
        saveDomoticzServers(false);
    }

    /**
     * this method is to parse the previous settings from shared preferences into the new structure
     */
    public void loadPreviousSettingsToServerInfo() {
        String REMOTE_SERVER_USERNAME = "remote_server_username";
        String REMOTE_SERVER_PASSWORD = "remote_server_password";
        String REMOTE_SERVER_URL = "remote_server_url";
        String REMOTE_SERVER_PORT = "remote_server_port";
        String REMOTE_SERVER_DIRECTORY = "remote_server_directory";
        String REMOTE_SERVER_SECURE = "remote_server_secure";
        String REMOTE_SERVER_AUTHENTICATION_METHOD =
                "remote_server_authentication_method";
        String IS_LOCAL_SERVER_ADDRESS_DIFFERENT = "local_server_different_address";
        String USE_ONLY_LOCAL = "use_ony_local";
        String LOCAL_SERVER_USERNAME = "local_server_username";
        String LOCAL_SERVER_PASSWORD = "local_server_password";
        String LOCAL_SERVER_URL = "local_server_url";
        String LOCAL_SERVER_PORT = "local_server_port";
        String LOCAL_SERVER_DIRECTORY = "local_server_directory";
        String LOCAL_SERVER_SECURE = "local_server_secure";
        String LOCAL_SERVER_AUTHENTICATION_METHOD =
                "local_server_authentication_method";
        String LOCAL_SERVER_SSID = "local_server_ssid";

        ServerInfo oPrefServer = new ServerInfo();
        oPrefServer.setRemoteServerUsername(prefs.getString(REMOTE_SERVER_USERNAME, ""));
        oPrefServer.setRemoteServerPassword(prefs.getString(REMOTE_SERVER_PASSWORD, ""));
        oPrefServer.setRemoteServerUrl(prefs.getString(REMOTE_SERVER_URL, ""));
        oPrefServer.setRemoteServerDirectory(prefs.getString(REMOTE_SERVER_DIRECTORY, ""));
        oPrefServer.setRemoteServerPort(prefs.getString(REMOTE_SERVER_PORT, ""));
        oPrefServer.setRemoteServerSecure(prefs.getBoolean(REMOTE_SERVER_SECURE, true));
        oPrefServer.setRemoteServerAuthentication(prefs.getBoolean(REMOTE_SERVER_AUTHENTICATION_METHOD, true));

        if (!prefs.getBoolean(IS_LOCAL_SERVER_ADDRESS_DIFFERENT, false))
            oPrefServer.setLocalSameAddressAsRemote();

        oPrefServer.setUseOnlyLocal(prefs.getBoolean(USE_ONLY_LOCAL, false));
        oPrefServer.setIsLocalServerAddressDifferent(prefs.getBoolean(IS_LOCAL_SERVER_ADDRESS_DIFFERENT, false));
        oPrefServer.setLocalServerUsername(prefs.getString(LOCAL_SERVER_USERNAME, ""));
        oPrefServer.setLocalServerPassword(prefs.getString(LOCAL_SERVER_PASSWORD, ""));
        oPrefServer.setLocalServerUrl(prefs.getString(LOCAL_SERVER_URL, ""));
        oPrefServer.setLocalServerPort(prefs.getString(LOCAL_SERVER_PORT, ""));
        oPrefServer.setLocalServerDirectory(prefs.getString(LOCAL_SERVER_DIRECTORY, ""));
        oPrefServer.setLocalServerSecure(prefs.getBoolean(LOCAL_SERVER_SECURE, false));
        oPrefServer.setLocalServerAuthentication(prefs.getBoolean(LOCAL_SERVER_AUTHENTICATION_METHOD, false));
        oPrefServer.setLocalServerSsid(prefs.getStringSet(LOCAL_SERVER_SSID, null));
        oPrefServer.setEnabled(true);

        setActiveServer(oPrefServer);
        if (mServerList == null || mServerList.size() <= 0)
            mServerList = new ArrayList<>();
        mServerList.add(oPrefServer);

        saveDomoticzServers(false);
    }

    /**
     * Get specific ServerInfo
     */
    public ServerInfo getServerInfo(String updateServerName) {
        if (UsefulBits.isEmpty(updateServerName) || this.mServerList == null || this.mServerList.size() <= 0)
            return null;

        for (ServerInfo s : mServerList) {
            if (s.getServerName().equals(updateServerName))
                return s;
        }

        return null;//not found
    }
}
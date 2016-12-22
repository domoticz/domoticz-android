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

package nl.hnogames.domoticzapi.Containers;

import android.content.Context;

import com.google.gson.annotations.Expose;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Utils.SerializableManager;
import nl.hnogames.domoticzapi.Utils.UsefulBits;


public class ServerInfo {
    @SuppressWarnings("FieldCanBeLocal")
    private final String SERIALIZE_CONFIG_FILE_EXTENSION = ".ser";
    @SuppressWarnings("FieldCanBeLocal")
    private final String SERIALIZE_CONFIG_FILE_PREFIX = "configInfo_";
    @SuppressWarnings("FieldCanBeLocal")
    private final String SERIALIZE_UPDATE_FILE_PREFIX = "serverUpdateInfo_";

    @Expose
    private String SERVER_UNIQUE_ID;
    @Expose
    private String SERVER_NAME = Domoticz.DOMOTICZ_DEFAULT_SERVER;
    @Expose
    private String REMOTE_SERVER_USERNAME = "";
    @Expose
    private String REMOTE_SERVER_PASSWORD = "";
    @Expose
    private String REMOTE_SERVER_URL = "";
    @Expose
    private String REMOTE_SERVER_PORT = "";
    @Expose
    private String REMOTE_SERVER_DIRECTORY = "";
    @Expose
    private boolean REMOTE_SERVER_SECURE = true;
    @Expose
    private boolean REMOTE_SERVER_AUTHENTICATION_METHOD = false;
    @Expose
    private boolean IS_LOCAL_SERVER_ADDRESS_DIFFERENT = false;
    @Expose
    private boolean USE_ONLY_LOCAL = false;
    @Expose
    private String LOCAL_SERVER_USERNAME = "";
    @Expose
    private String LOCAL_SERVER_PASSWORD = "";
    @Expose
    private String LOCAL_SERVER_URL = "";
    @Expose
    private String LOCAL_SERVER_PORT = "";
    @Expose
    private String LOCAL_SERVER_DIRECTORY = "";
    @Expose
    private boolean LOCAL_SERVER_SECURE = false;
    @Expose
    private boolean ENABLED = true;
    @Expose
    private boolean LOCAL_SERVER_AUTHENTICATION_METHOD = false;
    @Expose
    private Set<String> LOCAL_SERVER_SSID;

    /* Saved in a separate file because GSON can't serialize this */
    private ServerUpdateInfo serverUpdateInfo;
    private ConfigInfo configInfo;

    /**
     * Getter for this server unique ID
     *
     * @return Server unique ID
     */
    public String getServerUniqueId() {
        if (SERVER_UNIQUE_ID != null)
            return SERVER_UNIQUE_ID;
        else {
            setServerUniqueId(createUniqueServerId());
            return SERVER_UNIQUE_ID;
        }
    }

    /**
     * Setter for this servers unique ID
     *
     * @param SERVER_UNIQUE_ID to set
     */
    public void setServerUniqueId(String SERVER_UNIQUE_ID) {
        this.SERVER_UNIQUE_ID = SERVER_UNIQUE_ID;
    }

    /**
     * Creates a MD5 of local and remote server data
     *
     * @return MD5 checksum
     */
    public String createUniqueServerId() {
        return UsefulBits.getMd5String(
                LOCAL_SERVER_URL + LOCAL_SERVER_PORT +
                        REMOTE_SERVER_URL + REMOTE_SERVER_PORT);
    }

    public String getRemoteServerUsername() {
        return REMOTE_SERVER_USERNAME;
    }

    public void setRemoteServerUsername(String remoteServerUsername) {
        REMOTE_SERVER_USERNAME = remoteServerUsername;
    }

    public String getRemoteServerPassword() {
        return REMOTE_SERVER_PASSWORD;
    }

    public void setRemoteServerPassword(String remoteServerPassword) {
        REMOTE_SERVER_PASSWORD = remoteServerPassword;
    }

    public String getRemoteServerUrl() {
        return REMOTE_SERVER_URL;
    }

    public void setRemoteServerUrl(String remoteServerUrl) {
        REMOTE_SERVER_URL = remoteServerUrl;
    }

    public String getRemoteServerPort() {
        return REMOTE_SERVER_PORT;
    }

    public void setRemoteServerPort(String remoteServerPort) {
        REMOTE_SERVER_PORT = remoteServerPort;
    }

    public String getRemoteServerDirectory() {
        return REMOTE_SERVER_DIRECTORY;
    }

    public void setRemoteServerDirectory(String remoteServerDirectory) {
        REMOTE_SERVER_DIRECTORY = remoteServerDirectory;
    }

    public boolean getRemoteServerSecure() {
        return REMOTE_SERVER_SECURE;
    }

    public void setRemoteServerSecure(boolean remoteServerSecure) {
        REMOTE_SERVER_SECURE = remoteServerSecure;
    }

    public String getRemoteServerAuthenticationMethod() {
        String method;
        if (REMOTE_SERVER_AUTHENTICATION_METHOD)
            method = Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM;
        else method = Domoticz.Authentication.Method.AUTH_METHOD_BASIC_AUTHENTICATION;
        return method;
    }

    @SuppressWarnings("unused")
    public void setRemoteServerAuthenticationMethod(String remoteServerAuthenticationMethod) {
        //noinspection RedundantIfStatement
        if (remoteServerAuthenticationMethod.equals(Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM))
            REMOTE_SERVER_AUTHENTICATION_METHOD = true;
        else
            REMOTE_SERVER_AUTHENTICATION_METHOD = false;
    }

    @SuppressWarnings("unused")
    public boolean getRemoteServerAuthentication() {
        return REMOTE_SERVER_AUTHENTICATION_METHOD;
    }

    public void setRemoteServerAuthentication(boolean remoteServerAuthenticationMethod) {
        REMOTE_SERVER_AUTHENTICATION_METHOD = remoteServerAuthenticationMethod;
    }

    public boolean getIsLocalServerAddressDifferent() {
        return IS_LOCAL_SERVER_ADDRESS_DIFFERENT;
    }

    public void setIsLocalServerAddressDifferent(boolean isLocalServerAddressDifferent) {
        IS_LOCAL_SERVER_ADDRESS_DIFFERENT = isLocalServerAddressDifferent;
    }

    public boolean getUseOnlyLocal() {
        return USE_ONLY_LOCAL;
    }

    public void setUseOnlyLocal(boolean onlyLocal) {
        USE_ONLY_LOCAL = onlyLocal;
    }

    public String getLocalServerUsername() {
        return LOCAL_SERVER_USERNAME;
    }

    public void setLocalServerUsername(String localServerUsername) {
        LOCAL_SERVER_USERNAME = localServerUsername;
    }

    public String getLocalServerPassword() {
        return LOCAL_SERVER_PASSWORD;
    }

    public void setLocalServerPassword(String localServerPassword) {
        LOCAL_SERVER_PASSWORD = localServerPassword;
    }

    public String getLocalServerUrl() {
        return LOCAL_SERVER_URL;
    }

    public void setLocalServerUrl(String localServerUrl) {
        LOCAL_SERVER_URL = localServerUrl;
    }

    public String getLocalServerPort() {
        return LOCAL_SERVER_PORT;
    }

    public void setLocalServerPort(String localServerPort) {
        LOCAL_SERVER_PORT = localServerPort;
    }

    public String getLocalServerDirectory() {
        return LOCAL_SERVER_DIRECTORY;
    }

    public void setLocalServerDirectory(String localServerDirectory) {
        LOCAL_SERVER_DIRECTORY = localServerDirectory;
    }

    public boolean getLocalServerSecure() {
        return LOCAL_SERVER_SECURE;
    }

    public void setLocalServerSecure(boolean localServerSecure) {
        LOCAL_SERVER_SECURE = localServerSecure;
    }

    @SuppressWarnings("unused")
    public String getLocalServerAuthenticationMethod() {
        String method;

        if (LOCAL_SERVER_AUTHENTICATION_METHOD)
            method = Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM;
        else method = Domoticz.Authentication.Method.AUTH_METHOD_BASIC_AUTHENTICATION;

        return method;
    }

    public void setLocalServerAuthenticationMethod(String localServerAuthenticationMethod) {
        LOCAL_SERVER_AUTHENTICATION_METHOD = localServerAuthenticationMethod.equalsIgnoreCase(Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM);
    }

    @SuppressWarnings("unused")
    public boolean getLocalServerAuthentication() {
        return LOCAL_SERVER_AUTHENTICATION_METHOD;
    }

    public void setLocalServerAuthentication(boolean localServerAuthenticationMethod) {
        LOCAL_SERVER_AUTHENTICATION_METHOD = localServerAuthenticationMethod;
    }

    public Set<String> getLocalServerSsid() {
        return LOCAL_SERVER_SSID;
    }

    public void setLocalServerSsid(List<String> ssids) {
        if (ssids != null) {
            Set<String> set = new HashSet<>();
            for (String ssid : ssids) {
                set.add(ssid);
            }
            LOCAL_SERVER_SSID = set;
        }
    }

    public void setLocalServerSsid(Set<String> set) {
        LOCAL_SERVER_SSID = set;
    }

    public String getServerName() {
        return SERVER_NAME;
    }

    public void setServerName(String serverName) {
        SERVER_NAME = serverName;
    }

    /**
     * Reads this server update info from a file
     *
     * @param context Context to use
     * @return Server update info of this server from a file
     */
    private ServerUpdateInfo readServerUpdateInfoFromFile(Context context) {
        String uniqueServerId = getServerUniqueId();
        Object serializedServerInfoObject = SerializableManager.readSerializedObject(
                context,
                SERIALIZE_UPDATE_FILE_PREFIX + uniqueServerId + SERIALIZE_CONFIG_FILE_EXTENSION);
        if (serializedServerInfoObject != null && serializedServerInfoObject instanceof ServerUpdateInfo) {
            return (ServerUpdateInfo) serializedServerInfoObject;
        } else return null;
    }

    /**
     * Get's this server update info.
     * If it's not in this class it will read it from a file
     *
     * @param context Context to use
     * @return Server update info of this server
     */
    public ServerUpdateInfo getServerUpdateInfo(Context context) {
        if (serverUpdateInfo == null) serverUpdateInfo = readServerUpdateInfoFromFile(context);
        return serverUpdateInfo;
    }

    /**
     * Set's this server update information and writes it to a file
     *
     * @param context          Context to use
     * @param serverUpdateInfo Server update info to write
     */
    public void setServerUpdateInfo(Context context, ServerUpdateInfo serverUpdateInfo) {
        this.serverUpdateInfo = serverUpdateInfo;
        String uniqueServerId = getServerUniqueId();
        SerializableManager.saveSerializable(
                context,
                serverUpdateInfo,
                SERIALIZE_UPDATE_FILE_PREFIX + uniqueServerId + SERIALIZE_CONFIG_FILE_EXTENSION);

    }

    /**
     * Reads this server configuration info from a file
     *
     * @param context Context to use
     * @return Configuration info of this server from a file
     */
    private ConfigInfo readConfigInfoFromFile(Context context) {
        try {
            String uniqueServerId = getServerUniqueId();
            Object serializedServerInfoObject = SerializableManager.readSerializedObject(
                    context,
                    SERIALIZE_CONFIG_FILE_PREFIX + uniqueServerId + SERIALIZE_CONFIG_FILE_EXTENSION);
            if (serializedServerInfoObject != null && serializedServerInfoObject instanceof ConfigInfo) {
                return (ConfigInfo) serializedServerInfoObject;
            } else return null;
        }catch(Exception ex){return null;}
    }

    /**
     * Get's this server configuration info.
     * If it's not in this class it will read it from a file
     *
     * @param context Context to use
     * @return Configuration info of this server
     */
    public ConfigInfo getConfigInfo(Context context) {
        if (configInfo == null)
            configInfo = readConfigInfoFromFile(context);
        return configInfo;
    }

    /**
     * Set's this server configuration information and writes it to a file
     *
     * @param context    Context to use
     * @param configInfo Configuration info to write
     */
    public void setConfigInfo(Context context, ConfigInfo configInfo) {
        this.configInfo = configInfo;
        String uniqueServerId = getServerUniqueId();
        SerializableManager.saveSerializable(
                context,
                configInfo,
                SERIALIZE_CONFIG_FILE_PREFIX + uniqueServerId + SERIALIZE_CONFIG_FILE_EXTENSION);
    }

    /**
     * Method for setting local server addresses the same as the remote server addresses
     */
    public void setLocalSameAddressAsRemote() {
        setLocalServerUsername(getRemoteServerUsername());
        setLocalServerPassword(getRemoteServerPassword());
        setLocalServerUrl(getRemoteServerUrl());
        setLocalServerPort(getRemoteServerPort());
        setLocalServerDirectory(getRemoteServerDirectory());
        setLocalServerSecure(getRemoteServerSecure());
        setLocalServerAuthenticationMethod(getRemoteServerAuthenticationMethod());
    }

    public boolean isEnabled() {
        return ENABLED;
    }

    public void setEnabled(boolean enable) {
        this.ENABLED = enable;
    }
}
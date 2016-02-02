package nl.hnogames.domoticz.Containers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.hnogames.domoticz.Domoticz.Domoticz;

public class ServerInfo {
    private String SERVER_NAME = Domoticz.DOMOTICZ_DEFAULT_SERVER;
    private String REMOTE_SERVER_USERNAME = "";
    private String REMOTE_SERVER_PASSWORD = "";
    private String REMOTE_SERVER_URL = "";
    private String REMOTE_SERVER_PORT = "";
    private String REMOTE_SERVER_DIRECTORY = "";
    private boolean REMOTE_SERVER_SECURE = true;
    private boolean REMOTE_SERVER_AUTHENTICATION_METHOD = false;
    private boolean IS_LOCAL_SERVER_ADDRESS_DIFFERENT = false;
    private String LOCAL_SERVER_USERNAME = "";
    private String LOCAL_SERVER_PASSWORD = "";
    private String LOCAL_SERVER_URL = "";
    private String LOCAL_SERVER_PORT = "";
    private String LOCAL_SERVER_DIRECTORY = "";
    private boolean LOCAL_SERVER_SECURE = false;
    private boolean ENABLED = true;
    private boolean LOCAL_SERVER_AUTHENTICATION_METHOD = false;
    private Set<String> LOCAL_SERVER_SSID;
    private ServerUpdateInfo serverUpdateInfo;

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

    public void setRemoteServerAuthenticationMethod(String remoteServerAuthenticationMethod) {
        if (remoteServerAuthenticationMethod.equals(Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM))
            REMOTE_SERVER_AUTHENTICATION_METHOD = true;
        else
            REMOTE_SERVER_AUTHENTICATION_METHOD = false;
    }

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

    public boolean getLocalServerAuthentication() {
        return LOCAL_SERVER_AUTHENTICATION_METHOD;
    }

    public void setLocalServerAuthentication(boolean localServerAuthenticationMethod) {
        LOCAL_SERVER_AUTHENTICATION_METHOD = localServerAuthenticationMethod;
    }

    public Set<String> getLocalServerSsid() {
        return LOCAL_SERVER_SSID;
    }

    public void setLocalServerSsid(Set<String> set) {
        LOCAL_SERVER_SSID = set;
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

    public String getServerName() {
        return SERVER_NAME;
    }

    public void setServerName(String serverName) {
        SERVER_NAME = serverName;
    }

    public ServerUpdateInfo getServerUpdateInfo() {
        return serverUpdateInfo;
    }

    public void setServerUpdateInfo(ServerUpdateInfo serverUpdateInfo) {
        this.serverUpdateInfo = serverUpdateInfo;
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

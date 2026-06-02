
package nl.hnogames.domoticz.interfaces;


import nl.hnogames.domoticzapi.Containers.ServerInfo;

public interface ServerClickListener {

    boolean onEnableClick(ServerInfo server, boolean checked);

    void onRemoveClick(ServerInfo server);
}
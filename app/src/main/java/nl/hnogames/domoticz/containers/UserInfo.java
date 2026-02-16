
package nl.hnogames.domoticz.containers;

import com.stfalcon.chatkit.commons.models.IUser;

public class UserInfo implements IUser {

    @Override
    public String getId() {
        return "dummy";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getAvatar() {
        return null;
    }
}

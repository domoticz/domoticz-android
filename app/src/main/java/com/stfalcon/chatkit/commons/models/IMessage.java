package com.stfalcon.chatkit.commons.models;

import java.util.Date;

public interface IMessage {
    String getId();
    String getText();
    IUser getUser();
    Date getCreatedAt();
}


package net.noconroy.itproject.application;

import net.noconroy.itproject.application.Chat.ChatHelper;

/**
 * Created by matt on 10/11/17.
 */

public class Notifications {

    public ChatHelper chatHelper;
    public void initialise() {
        NetworkHelper.ChannelListen();

    }


}

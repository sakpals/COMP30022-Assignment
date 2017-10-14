package net.noconroy.itproject.application.models;

import com.google.gson.JsonObject;

import net.noconroy.itproject.application.DataStorage;

import static net.noconroy.itproject.application.Chat.ChatHelper.USER_CHANNEL_PREFIX;

/**
 * Created by matt on 10/11/17.
 */

public class Message {
    public String channel;
    public String user;
    public String type;
    public JsonObject data;
    public String id;
    public String prev;
    public String next;
    public String server_time;

    public boolean isMe() {
        return DataStorage.getInstance().me.username.equals(user);
    }
    public boolean filter(String friend) {
        return DataStorage.getInstance().me.username.equals(user) && (USER_CHANNEL_PREFIX + friend).equals(channel)||
                (USER_CHANNEL_PREFIX + DataStorage.getInstance().me.username).equals(channel) && friend.equals(user);
    }
}

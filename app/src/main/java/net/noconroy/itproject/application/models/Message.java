package net.noconroy.itproject.application.models;

import com.google.gson.JsonObject;

import net.noconroy.itproject.application.DataStorage;

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
}

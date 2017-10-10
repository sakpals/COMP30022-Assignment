package net.noconroy.itproject.application.models;

import com.google.gson.JsonObject;

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
}

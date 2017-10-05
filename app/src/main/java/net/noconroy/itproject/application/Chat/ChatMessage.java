package net.noconroy.itproject.application.Chat;

/**
 * Created by Mattias on 5/10/2017.
 * Simple class to represent chat messages
 */

public class ChatMessage {
    private String id;
    private String message;
    private String messageDate;
    private boolean isMe;

    public ChatMessage() {
        ;
    }

    public ChatMessage(String id, String message, String messageDate, boolean isMe) {
        this.id = id;
        this.message = message;
        this.messageDate = messageDate;
        this.isMe = isMe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }
}

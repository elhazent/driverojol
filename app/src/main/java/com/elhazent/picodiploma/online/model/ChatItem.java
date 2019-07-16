package com.elhazent.picodiploma.online.model;

public class ChatItem {
    String id,sender, receiver, message;
    boolean isseen;

    public ChatItem() {
    }

    public ChatItem(String id,String sender, String receiver, String message, boolean isseen) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

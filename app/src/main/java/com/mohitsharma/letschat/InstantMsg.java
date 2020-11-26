package com.mohitsharma.letschat;

public class InstantMsg {
    private String message;
    private String user;

    public InstantMsg(String message, String user) {
        this.message = message;
        this.user = user;
    }

    public InstantMsg() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}

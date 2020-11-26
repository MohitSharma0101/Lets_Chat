package com.mohitsharma.letschat.Model;

public class Message {
    String chatId = "";
    String senderId = "";
    String messageId = "";
    String message = "";
    String time = "";

    public Message() {
    }

    public Message(String chatId, String senderId, String messageId, String message, String time) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageId = messageId;
        this.message = message;
        this.time = time;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}

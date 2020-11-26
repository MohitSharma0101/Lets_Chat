package com.mohitsharma.letschat.Model;

public class Chat {
    String userNumber;
    String profileUrl;
    String lastMsg;
    String timeStamp;
    String userName;

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public Chat() {
    }

    public Chat(String userNumber, String profileUrl, String lastMsg, String timeStamp , String userName) {
        this.userNumber = userNumber;
        this.profileUrl = profileUrl;
        this.lastMsg = lastMsg;
        this.timeStamp = timeStamp;
        this.userName = userName;
    }
}

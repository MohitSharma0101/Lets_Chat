package com.mohitsharma.letschat.Model;

public class Contact {
    String id;
    String name;
    String profileUrl;
    String status;

    public Contact(String id, String name, String profileUrl, String status) {
        this.id = id;
        this.name = name;
        this.profileUrl = profileUrl;
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Contact() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getStatus() {
        return status;
    }
}

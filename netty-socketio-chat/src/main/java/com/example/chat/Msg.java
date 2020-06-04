package com.example.chat;

import java.io.Serializable;

public class Msg implements Serializable{
    private static final long serialVersionUID = -6519304261259719883L;

    private String userId;

    private String userName;

    private String receiveUserId;

    private String content;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Msg(String userId, String userName, String receiveUserId, String content) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.receiveUserId = receiveUserId;
        this.content = content;
    }

    public Msg() {
        super();
    }


}
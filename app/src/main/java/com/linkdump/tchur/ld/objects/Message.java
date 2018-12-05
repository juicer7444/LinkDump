package com.linkdump.tchur.ld.objects;

public class Message {
    private String message, user, userName;
    private long sentTime;
    private boolean isUser;

    public Message() {
    }

    public Message(String mMessage, String mUser, long mSentTime){
        message = mMessage;
        user = mUser;
        sentTime = mSentTime;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String mUserName){
        userName = mUserName;
    }

    public boolean getIsUser() {
        return isUser;
    }

    public void setIsUser(boolean user) {
        isUser = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }
}

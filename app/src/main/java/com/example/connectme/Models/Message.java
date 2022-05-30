package com.example.connectme.Models;

public class Message {
    private String messageId, message, senderId;
    private long timetamp;
    private int feeling;

    public Message() {
    }

    public Message(String message, String senderId, long timetamp) {
        this.message = message;
        this.senderId = senderId;
        this.timetamp = timetamp;

    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimetamp() {
        return timetamp;
    }

    public void setTimetamp(long timetamp) {
        this.timetamp = timetamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }
}

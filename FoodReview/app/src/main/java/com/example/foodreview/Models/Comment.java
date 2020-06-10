package com.example.foodreview.Models;

import android.content.Context;

import com.google.firebase.database.ServerValue;

import java.util.List;

public class Comment {
    private String uid, content, uimg, uname;
    private Object timestamp;

    Context mContext;
    List<Comment> mData;

    public Comment() {

    }

    public Comment(String uid, String content, String uimg, String uname) {
        this.uid = uid;
        this.content = content;
        this.uimg = uimg;
        this.uname = uname;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Comment(String uid, String content, String uimg, String uname, Object timestamp) {
        this.uid = uid;
        this.content = content;
        this.uimg = uimg;
        this.uname = uname;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUimg() {
        return uimg;
    }

    public void setUimg(String uimg) {
        this.uimg = uimg;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}

package com.corydominguez.tweetastic.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Tweet {
    private Long id;
    private String text;
    private String createdAt;
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (key.equals("created_at")) {
            setCreatedAt(value.toString());
        }
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", user=" + user +
                '}';
    }
}


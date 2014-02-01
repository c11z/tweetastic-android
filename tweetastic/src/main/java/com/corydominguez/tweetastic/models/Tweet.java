package com.corydominguez.tweetastic.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;

@Table(name = "Tweet")
public class Tweet extends Model implements Serializable {
    @Column(name="TweetId", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long tweetId;
    @Column(name = "Text")
    private String text;
    @Column(name="User")
    private User user;

    public Tweet() {
        super();
    }

    public Long getTweetId() {
        return tweetId;
    }
    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (key.equals("id")) {
            setTweetId(Long.parseLong(value.toString()));
        }
    }
    @Override
    public String toString() {
        return "Tweet{" +
                "tweetId=" + tweetId +
                ", text='" + text + '\'' +
                ", user=" + user +
                '}';
    }
}


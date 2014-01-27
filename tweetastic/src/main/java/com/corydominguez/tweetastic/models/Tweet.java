package com.corydominguez.tweetastic.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Table(name = "Tweet")
public class Tweet extends Model {
    @Column(name="TweetId", index = true, unique = true,
            onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long id;
    @Column(name = "Text")
    private String text;
    @Column(name="User")
    private User user;
    @Column(name="CreatedAt")
    private Date createdAt;

    public Tweet() {
        super();
    }

    public Long getTweetId() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (key.equals("created_at")) {
            try {
                createdAt = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
                        .parse(value.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", user=" + user +
                ", createdAt=" + createdAt +
                '}';
    }
}


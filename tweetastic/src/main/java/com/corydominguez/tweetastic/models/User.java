package com.corydominguez.tweetastic.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by coryd on 22/01/2014.
 */

@Table(name = "User")
public class User extends Model implements Serializable {
    @Column(name = "UserId")
    private Long userId;
    @Column(name = "ProfileImageUrl")
    private String profileImageUrl;
    @Column(name = "Name")
    private String name;
    @Column(name = "ScreenName")
    private String screenName;
    @Column(name="Description")
    private String description;
    @Column(name="FollowersCount")
    private Integer followersCount;
    @Column(name="FriendsCount")
    private Integer friendsCount;

    public User() {
        super();
    }

    public List<Tweet> tweets() {
        return getMany(Tweet.class, "User");
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long id) {
        this.userId = id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public Integer getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(Integer friendsCount) {
        this.friendsCount = friendsCount;
    }

    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (key.equals("id")) {
            setUserId(Long.parseLong(value.toString()));
        }
        if (key.equals("profile_image_url")) {
            setProfileImageUrl(value.toString());
        }
        if (key.equals("screen_name")) {
            setScreenName(value.toString());
        }
        if (key.equals("followers_count")) {
            setFollowersCount(Integer.parseInt(value.toString()));
        }
        if (key.equals(("friends_count"))) {
            setFriendsCount(Integer.parseInt(value.toString()));
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "screenName='" + screenName + '\'' +
                ", name='" + name + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", userId=" + userId +
                '}';
    }
}

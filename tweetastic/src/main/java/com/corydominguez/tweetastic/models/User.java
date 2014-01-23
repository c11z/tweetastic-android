package com.corydominguez.tweetastic.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Created by coryd on 22/01/2014.
 */
public class User {
    private Long id;
    private String profileImageUrl;
    private String name;
    private String screenName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (key.equals("profile_image_url")) {
            setProfileImageUrl(value.toString());
        }
        if (key.equals("screen_name")) {
            setScreenName(value.toString());
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "screenName='" + screenName + '\'' +
                ", name='" + name + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", id=" + id +
                '}';
    }
}

package com.corydominguez.tweetastic.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Created by coryd on 22/01/2014.
 */

@Table(name = "User")
public class User extends Model {
    @Column(name = "UserId", index = true, unique = true,
            onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long id;
    @Column(name = "ProfileImageUrl")
    private String profileImageUrl;
    @Column(name = "Name")
    private String name;
    @Column(name = "ScreenName")
    private String screenName;

    public User() {
        super();
    }

    public Long getUserId() {
        return id;
    }

    public void setUserId(Long id) {
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

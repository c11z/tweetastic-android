package com.corydominguez.tweetastic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.fragments.UserTimelineFragment;
import com.corydominguez.tweetastic.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by coryd on 02/02/2014.
 */
public class ProfileActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        assert(getActionBar() != null);
        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        assert (user != null);
        getActionBar().setTitle("@" + user.getScreenName());
        populateProfileHeader(user);
        UserTimelineFragment utl = (UserTimelineFragment) getSupportFragmentManager().findFragmentById(R.id.fgUserTimeline);
        utl.setTargetUserId(user.getUserId().toString());
    }

    private void populateProfileHeader(User user) {
        TextView tvName = (TextView) findViewById(R.id.tvName);
        TextView tvTagline = (TextView) findViewById(R.id.tvTagline);
        TextView tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) findViewById(R.id.tvFollowing);
        ImageView ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvName.setText(user.getName());
        tvTagline.setText(user.getDescription());
        tvFollowing.setText(user.getFollowersCount().toString() + " Followers");
        tvFollowers.setText(user.getFriendsCount().toString() + " Following");
        ImageLoader.getInstance().displayImage(user.getProfileImageUrl(), ivProfileImage);
    }
}

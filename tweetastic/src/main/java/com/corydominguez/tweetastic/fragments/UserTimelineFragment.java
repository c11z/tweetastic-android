package com.corydominguez.tweetastic.fragments;

import android.os.Bundle;

import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.models.Tweet;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coryd on 02/02/2014.
 */
public class UserTimelineFragment extends TweetListFragment {
    public String targetUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void dealWithNewTweets(ArrayList<Tweet> newTweets) {
        for (int i=0; i < newTweets.size(); i++) {
            Tweet tweet = newTweets.get(i);
            // Save to database
            tweet.getUser().save();
            tweet.save();
            // Check if we are putting tweets on top or on the bottom of the list view
            if (appendMode) {
                adapter.add(tweet);
            } else {
                adapter.insert(tweet, i);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected List<Tweet> getAllTweets() {
        return null;
    }

    @Override
    protected void moarTweets(RequestParams params) {
        if (isNetworkAvailable()) {
            if (params == null) {
                params = new RequestParams();
            }
            params.put("user_id", targetUserId);
            TweetasticApp.getRestClient().getUserTimeline(params, handler);
        }
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}

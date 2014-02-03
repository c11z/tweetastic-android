package com.corydominguez.tweetastic.fragments;

import android.os.Bundle;

import com.activeandroid.query.Select;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.models.Tweet;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coryd on 01/02/2014.
 */
public class TimelineFragment extends TweetListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void moarTweets(RequestParams params) {
        // There may be a better method of doing this.
        if (isNetworkAvailable() && !asyncClientRunning) {
            TweetasticApp.getRestClient().getHomeTimeline(params, handler);
        }
    }

    protected List<Tweet> getAllTweets() {
        return new Select().from(Tweet.class).orderBy("TweetId DESC").execute();
    }

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
}

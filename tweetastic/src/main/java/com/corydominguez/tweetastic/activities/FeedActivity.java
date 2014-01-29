package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.adapters.FeedAdapter;
import com.corydominguez.tweetastic.listeners.EndlessScrollListener;
import com.corydominguez.tweetastic.models.Tweet;
import com.corydominguez.tweetastic.models.User;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FeedActivity extends Activity {
    private ListView lvTweetFeed;
    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        // Get List view and set adapter to existing list
        lvTweetFeed = (ListView) findViewById(R.id.lvTweetFeed);
        adapter = new FeedAdapter(getBaseContext(), new ArrayList<Tweet>());
        lvTweetFeed.setAdapter(adapter);
        // Choose source of tweets based in networking and staleness
        if (isDatabaseEmpty() || isNetworkAvailable()) {
            // Under right conditions forget about persisted Tweets
            deleteAllRecords();
            moarTweets(null);
        }
        // Load more tweets when feed list is scrolled to the bottom.
        lvTweetFeed.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Arbitrarily limit the history to 200 tweets
                if (totalItemsCount < 200) {
                    Tweet oldest = getOldest();
                    RequestParams params = new RequestParams();
                    params.put("max_id", oldest.getTweetId().toString());
                    moarTweets(params);
               }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTweets();
    }

    public void onCompose(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ComposeActivity.class);
        startActivity(intent);
    }

    public void onRefresh(MenuItem menuItem) {
        refreshTweets();
        lvTweetFeed.smoothScrollToPosition(0);
    }

    private void refreshTweets() {
        Tweet youngest = getYoungest();
        RequestParams params = new RequestParams();
        params.put("since_id", youngest.getTweetId().toString());
        moarTweets(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }

    private void moarTweets(RequestParams params) {
        AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String s) {
                try {
                    TypeReference<ArrayList<Tweet>> tr = new TypeReference<ArrayList<Tweet>>() {};
                    ArrayList<Tweet> tweets = TweetasticApp.mapper.readValue(s, tr);
                    // Bulk save the tweets and users to the database
                    for (Tweet tweet : tweets) {
                        tweet.getUser().save();
                        tweet.save();
                    }
                    // Horribly inefficient
                    adapter.clear();
                    adapter.addAll(getAllTweets());
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                // So... yeah, sometimes you get rate limited.
                if (s.contains("Rate limit exceeded")) {
                    assert (getApplicationContext() != null);
                    Toast.makeText(getApplicationContext(), "Rate Limit Exceeded",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (params == null) {
            TweetasticApp.getRestClient().getHomeTimeline(handler);
        } else {
            TweetasticApp.getRestClient().getHomeTimeline(params, handler);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private Tweet getYoungest() {
        Tweet youngest = new Select().from(Tweet.class).orderBy("TweetId DESC").executeSingle();
        if (youngest == null) {
            youngest = new Tweet();
            youngest.setTweetId(Long.MIN_VALUE);
        }
        return youngest;
    }

    private Tweet getOldest() {
        Tweet oldest = new Select().from(Tweet.class).orderBy("TweetId ASC").executeSingle();
        if (oldest == null) {
            oldest = new Tweet();
            oldest.setTweetId(Long.MAX_VALUE);
        }
        return oldest;
    }

    private List<Tweet> getTweets(String operator, long tweetId) {
        // Not used but still around in case I want to try and make things more performant.
        String whereClause = "TweetId " + operator + " " + tweetId;
        return new Select().from(Tweet.class).where(whereClause).orderBy("TweetId DESC").execute();
    }

    private List<Tweet> getAllTweets() {
        return new Select().from(Tweet.class).orderBy("TweetId DESC").execute();
    }

    private Boolean isDatabaseEmpty() {
        return (getYoungest() == null);
    }

    private void deleteAllRecords() {
        new Delete().from(Tweet.class).execute();
        new Delete().from(User.class).execute();
    }

    private long period(Date date1, Date date2) {
        // multi purpose period calc currently used only for minutes
        long diff = date1.getTime() - date2.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return minutes;
    }

    private void sortAdapter() {
        // Not used since now I get clear adapter at every change and fill it with ordered list
        // from sql query
        adapter.sort(new Comparator<Tweet>() {
            @Override
            public int compare(Tweet tweet, Tweet tweet2) {
                return tweet2.getTweetId().compareTo(tweet.getTweetId());
            }
        });
    }
}

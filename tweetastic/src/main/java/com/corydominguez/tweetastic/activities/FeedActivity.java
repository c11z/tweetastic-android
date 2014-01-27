package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
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

import static com.activeandroid.ActiveAndroid.beginTransaction;
import static com.activeandroid.ActiveAndroid.endTransaction;
import static com.activeandroid.ActiveAndroid.setTransactionSuccessful;

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
            RequestParams params = new RequestParams();
            params.put("count", "25");
            moarTweets(params);
        }
        // Load Tweets from Database and initialize adapter
        adapter.addAll(getAllTweets());

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
                    adapter.addAll(getTweets("<", oldest.getTweetId()));
                    sortAdapter();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // There is a problem on resume, if tweets where posted during the time taken to compose the
        // the new tweet then those tweets get skipped on the refresh tweets call. If I do not save
        // the tweet during compose then most likely a refresh will be too fast to pick it up and
        // the user will have the bad experience of not seeing their tweet get posted. Solution is
        // to make an api call checking for the in between tweets and adding them.
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
        adapter.addAll(getTweets(">", youngest.getTweetId()));
        sortAdapter();
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
                    beginTransaction();
                    try {
                        for (Tweet tweet : tweets) {
                            tweet.getUser().save();
                            tweet.save();
                        }
                        setTransactionSuccessful();
                    }
                    finally {
                        endTransaction();
                    }
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

            @Override
            protected void handleMessage(Message message) {
                super.handleMessage(message);
            }

            @Override
            protected void handleFailureMessage(Throwable throwable, String s) {
                super.handleFailureMessage(throwable, s);
            }

            @Override
            protected Message obtainMessage(int i, Object o) {
                return super.obtainMessage(i, o);
            }
        };

        TweetasticApp.getRestClient().getHomeTimeline(params, handler);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private Tweet getYoungest() {
        return (Tweet) new Select().from(Tweet.class).orderBy("TweetId DESC").executeSingle();
    }

    private Tweet getOldest() {
        return (Tweet) new Select().from(Tweet.class).orderBy("TweetId ASC").executeSingle();
    }

    private List<Tweet> getTweets(String operator, long tweetId) {
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
        adapter.sort(new Comparator<Tweet>() {
            @Override
            public int compare(Tweet tweet, Tweet tweet2) {
                return tweet2.getTweetId().compareTo(tweet.getTweetId());
            }
        });
    }
}

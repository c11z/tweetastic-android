package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.util.List;

public class FeedActivity extends Activity {
    private ListView lvTweetFeed;
    private FeedAdapter adapter;
    private ArrayList<Tweet> tweets;
    private Boolean appendMode;
    private Boolean asyncClientRunning;
    private ProgressBar pb;
    private AsyncHttpResponseHandler handler;
    private final int COMPOSE_RESULT_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupViews();
        setupHandler();
        setupListeners();
        // Choose source of tweets based in networking and staleness
        if (isNetworkAvailable()) {
            deleteAllRecords();
        }
        else {
            adapter.addAll(getAllTweets());
        }
        appendMode = false;
        moarTweets(null);
    }

    private void setupViews() {
        lvTweetFeed = (ListView) findViewById(R.id.lvTweetFeed);
        pb = (ProgressBar) findViewById(R.id.pbLoading);
        tweets = new ArrayList<Tweet>();
        adapter = new FeedAdapter(getBaseContext(), tweets);
        lvTweetFeed.setAdapter(adapter);
        asyncClientRunning = false;
    }

    private void setupHandler() {
        handler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                pb.setVisibility(ProgressBar.VISIBLE);
                asyncClientRunning = true;
            }

            @Override
            public void onSuccess(String s) {
                try {
                    Log.d("DEBUG", "Making api Call");
                    TypeReference<ArrayList<Tweet>> tr = new TypeReference<ArrayList<Tweet>>() {};
                    ArrayList<Tweet> newTweets = TweetasticApp.mapper.readValue(s, tr);
//                    for (int i=0; i < newTweets.size(); i++) {
//                        Tweet tweet = newTweets.get(i);
//                        // Save to database
//                        tweet.getUser().save();
//                        tweet.save();
//                    }
//                    adapter.clear();
//                    adapter.addAll(getAllTweets());
                    // Bulk save the tweets and users to the database
                    for (int i=0; i < newTweets.size(); i++) {
                        Tweet tweet = newTweets.get(i);
                        // Save to database
                        tweet.getUser().save();
                        tweet.save();
                        if (appendMode) {
                            tweets.add(tweet);
                        } else {
                            tweets.add(i, tweet);
                        }
                    }
                    adapter.notifyDataSetChanged();
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
            public void onFinish() {
                super.onFinish();
                pb.setVisibility(ProgressBar.INVISIBLE);
                asyncClientRunning = false;
            }
        };
    }

    private void setupListeners() {
        // Load more tweets when feed list is scrolled to the bottom.
        lvTweetFeed.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Arbitrarily limit the history to 200 tweets
                if (totalItemsCount < 200) {
                    Tweet oldest = getOldest();
                    RequestParams params = new RequestParams();
                    Long max_id = oldest.getTweetId() - 1;
                    params.put("max_id", max_id.toString());
                    appendMode = true;
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
        startActivityForResult(intent, COMPOSE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == COMPOSE_RESULT_CODE) {
            Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
            tweets.add(0, tweet);
            adapter.notifyDataSetChanged();
        }
    }

    public void onRefresh(MenuItem menuItem) {
        refreshTweets();
        lvTweetFeed.smoothScrollToPosition(0);
    }

    private void refreshTweets() {
        // Wait for initial load to finish
        if (tweets.size() > 0) {
            Tweet youngest = getYoungest();
            RequestParams params = new RequestParams();
            params.put("since_id", youngest.getTweetId().toString());
            appendMode = false;
            moarTweets(params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }

    private void moarTweets(RequestParams params) {
        // There may be a better method of doing this.
        if (isNetworkAvailable() && !asyncClientRunning) {
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
        return new Select().from(Tweet.class).orderBy("TweetId DESC").executeSingle();
    }

    private Tweet getOldest() {
        return new Select().from(Tweet.class).orderBy("TweetId ASC").executeSingle();
    }

    private List<Tweet> getAllTweets() {
        return new Select().from(Tweet.class).orderBy("TweetId DESC").execute();
    }

    private void deleteAllRecords() {
        new Delete().from(Tweet.class).execute();
        new Delete().from(User.class).execute();
    }
}

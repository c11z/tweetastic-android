package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.adapters.FeedAdapter;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.listeners.EndlessScrollListener;
import com.corydominguez.tweetastic.models.Tweet;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class FeedActivity extends Activity {
    private Long oldestId;
    private Long youngestId;
    private ListView lvTweetFeed;
    private FeedAdapter adapter;
    private ArrayList<Tweet> tweets = new ArrayList<Tweet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        // Get List view and set adapter to existing list
        lvTweetFeed = (ListView) findViewById(R.id.lvTweetFeed);
        adapter = new FeedAdapter(getBaseContext(), tweets);
        lvTweetFeed.setAdapter(adapter);
        // Check if first api call has been run, if so call onRefresh, otherwise make first call
        if (youngestId != null) {
           onRefresh(null);
        } else {
            moarTweets(null);
        }
        // Load more tweets when feed list is scrolled to the bottom.
        lvTweetFeed.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount < 200) {
                    RequestParams params = new RequestParams();
                    params.put("max_id", oldestId.toString());
                    moarTweets(params);
                }
            }
        });
    }

    public void onCompose(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ComposeActivity.class);
        startActivity(intent);
    }

    public void onRefresh (MenuItem menuItem) {
        RequestParams params = new RequestParams();
        // youngestId should always be set by the time refresh is called
        assert(youngestId != null);
        params.put("since_id", youngestId.toString());
        moarTweets(params);
        // smooth scroll to top
        lvTweetFeed.smoothScrollToPosition(0);
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
                    tweets = TweetasticApp.mapper.readValue(s, tr);
                    if (tweets.size() > 0) {
                        adapter.addAll(tweets);
                        adapter.sort(new Comparator<Tweet>() {
                            @Override
                            public int compare(Tweet tweet, Tweet tweet2) {
                                return tweet2.getId().compareTo(tweet.getId());
                            }
                        });
                        if (youngestId == null || tweets.get(0).getId() > youngestId) {
                            youngestId = tweets.get(0).getId();
                        }
                        if (oldestId ==null || tweets.get(tweets.size()-1).getId() < oldestId) {
                            oldestId = tweets.get(tweets.size()-1).getId();
                        }
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
        };
        if (params == null) {
            TweetasticApp.getRestClient().getHomeTimeline(handler);
        } else {
            TweetasticApp.getRestClient().getHomeTimeline(params, handler);
        }
    }
}

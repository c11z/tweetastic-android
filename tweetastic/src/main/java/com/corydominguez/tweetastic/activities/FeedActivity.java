package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.adapters.FeedAdapter;
import com.corydominguez.tweetastic.apps.TweetasticApp;
import com.corydominguez.tweetastic.models.Tweet;
import com.corydominguez.tweetastic.models.User;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
        lvTweetFeed = (ListView) findViewById(R.id.lvTweetFeed);
        adapter = new FeedAdapter(getBaseContext(), tweets);
        lvTweetFeed.setAdapter(adapter);
        RequestParams params = new RequestParams();
        if (TweetasticApp.me == null) {
            TweetasticApp.getRestClient().getUserInfo(new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String s) {
                    try {
                        TweetasticApp.me = TweetasticApp.mapper.readValue(s, User.class);
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (youngestId != null) {
            params.put("since_id", youngestId.toString());
            moarTweets(params);
            lvTweetFeed.smoothScrollToPosition(0);
        } else {
            moarTweets(null);
        }
    }

    public void onCompose(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ComposeActivity.class);
        startActivity(intent);
    }

    public void onRefresh (MenuItem menuItem) {
        RequestParams params = new RequestParams();
        if (youngestId != null) {
            params.put("since_id", youngestId.toString());
            moarTweets(params);
        } else {
            moarTweets(null);
        }
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
                        youngestId = tweets.get(0).getId();
                        oldestId = tweets.get(tweets.size()-1).getId();

                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
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

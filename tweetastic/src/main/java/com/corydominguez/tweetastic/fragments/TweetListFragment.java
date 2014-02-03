package com.corydominguez.tweetastic.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Delete;
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

/**
 * Created by coryd on 01/02/2014.
 */
public abstract class TweetListFragment extends Fragment {
    protected FeedAdapter adapter;
    private ListView lvTweets;
    protected ArrayList<Tweet> tweets;
    protected Boolean appendMode;
    protected Boolean asyncClientRunning;
    protected ProgressBar pb;
    protected AsyncHttpResponseHandler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tweetlist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupViews();
        setupHandler();
        setupListeners();
        // Choose source of tweets based in networking and staleness
        if (isNetworkAvailable()) {
            deleteAllRecords();
        }
//        else {
//           adapter.addAll(getAllTweets());
//        }
        appendMode = false;
        moarTweets(null);
    }

    private void setupViews() {
        pb = (ProgressBar) getActivity().findViewById(R.id.pbLoading);
        lvTweets = (ListView) getActivity().findViewById(R.id.lvTweets);
        tweets = new ArrayList<Tweet>();
        adapter = new FeedAdapter(getActivity(), tweets);
        lvTweets.setAdapter(adapter);
        asyncClientRunning = false;
    }

    public FeedAdapter getAdapter() {
        return adapter;
    }

    public ListView getlvTweets() {
        return lvTweets;
    }

    private void setupHandler() {
        handler = new AsyncHttpResponseHandler() {
            ArrayList<Tweet> newTweets;

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
                    newTweets = TweetasticApp.mapper.readValue(s, tr);
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
                    assert (getActivity().getApplicationContext() != null);
                    Toast.makeText(getActivity().getApplicationContext(), "Rate Limit Exceeded",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dealWithNewTweets(newTweets);
                pb.setVisibility(ProgressBar.INVISIBLE);
                asyncClientRunning = false;
            }
        };
    }

    private void setupListeners() {
        // Load more tweets when feed list is scrolled to the bottom.
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Arbitrarily limit the history to 200 tweets
                if (totalItemsCount < 200) {
                    RequestParams params = new RequestParams();
                    Tweet oldest = getOldest();
                    if (oldest == null) {
                        //This means it is the first time the listener is getting used.
                        appendMode = false;
                        moarTweets(null);
                    }else {
                        Long max_id = oldest.getTweetId() - 1;
                        params.put("max_id", max_id.toString());
                        appendMode = true;
                        moarTweets(params);
                    }
                }
            }
        });
    }

    protected abstract void dealWithNewTweets(ArrayList<Tweet> newTweets);

    @Override
    public void onResume() {
        super.onResume();
        refreshTweets();
    }

    public void refreshTweets() {
        // Wait for initial load to finish
        if (adapter.getCount() > 0) {
            Tweet youngest = getYoungest();
            RequestParams params = new RequestParams();
            params.put("since_id", youngest.getTweetId().toString());
            appendMode = false;
            moarTweets(params);
        }
    }
    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void deleteAllRecords() {
        new Delete().from(Tweet.class).execute();
        new Delete().from(User.class).execute();
    }

    protected abstract List<Tweet> getAllTweets();

    protected abstract void moarTweets(RequestParams params);

    protected Tweet getYoungest() {
//        return new Select().from(Tweet.class).orderBy("TweetId DESC").executeSingle();
        return tweets.get(0);
    }

    protected Tweet getOldest() {
//        return new Select().from(Tweet.class).orderBy("TweetId ASC").executeSingle();
        return tweets.get(tweets.size()-1);
    }



}

package com.corydominguez.tweetastic;

import android.app.Application;
import android.content.Context;
import com.corydominguez.tweetastic.clients.TwitterClient;
import com.corydominguez.tweetastic.models.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 * 
 *     TwitterClient client = TweetasticApp.getRestClient();
 *     // use client to send requests to API
 *     
 */
public class TweetasticApp extends Application {
	private static Context context;
    public static User me;
    public static final ObjectMapper mapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void onCreate() {
        super.onCreate();
        TweetasticApp.context = this;
        
        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
        		cacheInMemory().cacheOnDisc().build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .build();
        ImageLoader.getInstance().init(config);
    }

    public static TwitterClient getRestClient() {
    	return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TweetasticApp.context);
    }
}
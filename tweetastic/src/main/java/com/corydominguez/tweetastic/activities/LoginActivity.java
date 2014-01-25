package com.corydominguez.tweetastic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.codepath.oauth.OAuthLoginActivity;
import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.clients.TwitterClient;
import com.corydominguez.tweetastic.models.User;
import com.fasterxml.jackson.core.JsonParseException;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;

public class LoginActivity extends OAuthLoginActivity<TwitterClient> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
    @Override
    public void onLoginSuccess() {
        // Make call and get user information
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
    	Intent i = new Intent(this, FeedActivity.class);
    	startActivity(i);
    }
    
    // OAuth authentication flow failed, handle the error
    // i.e Display an error dialog or toast
    @Override
    public void onLoginFailure(Exception e) {
        e.printStackTrace();
    }
    
    // Click handler method for the button used to start OAuth flow
    // Uses the client to initiate OAuth authorization
    // This should be tied to a button used to login
    public void loginToRest(View view) {
        getClient().connect();
    }

}

package com.corydominguez.tweetastic.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.models.Tweet;
import com.corydominguez.tweetastic.models.User;
import com.fasterxml.jackson.core.JsonParseException;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

/**
 * Created by coryd on 24/01/2014.
 */
public class ComposeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        View llTweeBox = findViewById(R.id.llTweetBox);
        ImageView ivProfile = (ImageView) llTweeBox.findViewById(R.id.ivProfile);
        TextView tvName = (TextView) llTweeBox.findViewById(R.id.tvName);
        User me = TweetasticApp.me;
        ImageLoader.getInstance().displayImage(me.getProfileImageUrl(), ivProfile);
        String formattedName = "<b>" + me.getName() + "</b>" +
                " <small><font color='#777777'>@" + me.getScreenName() + "</font></small>";
        tvName.setText(Html.fromHtml(formattedName));
    }

    public void onTweet(View v) {
        RequestParams params = new RequestParams();
        EditText etStatus = (EditText) findViewById(R.id.etStatus);
        assert(etStatus.getText() != null);
        String status = etStatus.getText().toString();
        // If message is empty
        if (status.equals("")) {
            Toast.makeText(this, "Status message is empty :(", Toast.LENGTH_SHORT).show();
        } else {
            params.put("status", status);
            TweetasticApp.getRestClient().postUpdate(params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String s) {
                    try {
                        Tweet tweet = TweetasticApp.mapper.readValue(s, Tweet.class);
                        tweet.save();
                        // When app returns to feed activity refresh is called
                        Log.d("DEBUG", tweet.toString());
                        finish();
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
            });
        }
    }

    public void onCancel(View v) {
        // Do not pass go, do not collect 200 dollars
        finish();
    }
}

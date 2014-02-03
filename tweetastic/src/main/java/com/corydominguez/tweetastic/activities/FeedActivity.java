package com.corydominguez.tweetastic.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.TweetasticApp;
import com.corydominguez.tweetastic.fragments.MentionsFragment;
import com.corydominguez.tweetastic.fragments.TimelineFragment;
import com.corydominguez.tweetastic.models.Tweet;

public class FeedActivity extends FragmentActivity implements ActionBar.TabListener {
    private TimelineFragment tf;
    private MentionsFragment mf;
    private final int COMPOSE_RESULT_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setUpNavigationTabs();
    }

    private void setUpNavigationTabs() {
        ActionBar actionBar = getActionBar();
        assert(actionBar != null);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        ActionBar.Tab tabHome = actionBar.newTab().setText("Home").setTag("TimelineFragment").setTabListener(this);
        ActionBar.Tab tabMentions = actionBar.newTab().setText("Mentions").setTag("MentionsFragment").setTabListener(this);
        actionBar.addTab(tabHome);
        actionBar.addTab(tabMentions);
        actionBar.selectTab(tabHome);
    }

    public void onCompose(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ComposeActivity.class);
        startActivityForResult(intent, COMPOSE_RESULT_CODE);
    }

    public void onProfile(MenuItem menuItem) {
        Log.d("DEBUG", "Profile selected");
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("user", TweetasticApp.me);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == COMPOSE_RESULT_CODE) {
            Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
            tf.getAdapter().insert(tweet, 0);
            tf.getAdapter().notifyDataSetChanged();
        }
    }

    public void onRefresh(MenuItem menuItem) {
        assert (getActionBar() != null);
        assert (getActionBar().getSelectedTab() != null);
        if (getActionBar().getSelectedTab().getTag() == "TimelineFragment") {
            tf.refreshTweets();
            tf.getlvTweets().smoothScrollToPosition(0);
        } else {
            mf.refreshTweets();
            mf.getlvTweets().smoothScrollToPosition(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fts = manager.beginTransaction();
        if (tab.getTag() == "TimelineFragment") {
            if (tf == null) {
                tf = new TimelineFragment();
            }
            fts.replace(R.id.fmContainer, tf);
        } else {
            if (mf == null) {
                mf = new MentionsFragment();
            }
            fts.replace(R.id.fmContainer, mf);
        }
        fts.commit();

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}

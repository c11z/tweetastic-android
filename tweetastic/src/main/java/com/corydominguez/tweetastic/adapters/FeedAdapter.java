package com.corydominguez.tweetastic.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.corydominguez.tweetastic.R;
import com.corydominguez.tweetastic.activities.ProfileActivity;
import com.corydominguez.tweetastic.models.Tweet;
import com.corydominguez.tweetastic.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by coryd on 23/01/2014.
 */
public class FeedAdapter extends ArrayAdapter<Tweet> {

    public FeedAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_feed, null);
        }
        Tweet tweet = getItem(position);
        assert(view != null);
        ImageView ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
        ivProfile.setTag(tweet.getUser());
        ivProfile.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                User user = (User) view.getTag();
                intent.putExtra("user", user);
                getContext().startActivity(intent);
            }
        });
        ImageLoader.getInstance().displayImage(tweet.getUser().getProfileImageUrl(), ivProfile);
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        String formattedName = "<b>" + tweet.getUser().getName() + "</b>" +
                               " <small><font color='#777777'>@" + tweet.getUser().getScreenName() +
                               "</font></small>";
        tvName.setText(Html.fromHtml(formattedName));
        TextView bodyView = (TextView) view.findViewById(R.id.tvBody);
        bodyView.setText(Html.fromHtml(tweet.getText()));
        return view;
    }
}

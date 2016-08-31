package com.gmail.stonedevs.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class DetailActivityFragment extends Fragment {
    private static final String TAG_TITLE = "TITLE";
    private static final String TAG_RELEASE_DATE = "RELEASE_DATE";
    private static final String TAG_USER_RATING = "USER_RATING";
    private static final String TAG_PLOT_SUMMARY = "PLOT_SUMMARY";
    private static final String TAG_POSTER_URL = "POSTER_URL";

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_movie_poster);
        TextView textView_title = (TextView) view.findViewById(R.id.textView_movie_title);
        TextView textView_releaseDate = (TextView) view.findViewById(R.id.textView_movie_release_date);
        TextView textView_userRating = (TextView) view.findViewById(R.id.textView_movie_user_rating);
        TextView textView_plotSummary = (TextView) view.findViewById(R.id.textView_movie_plot_summary);

        Intent intent = getActivity().getIntent();

        assert intent != null;
        String title = intent.getStringExtra(TAG_TITLE);
        String releaseDate = intent.getStringExtra(TAG_RELEASE_DATE);
        double userRating = intent.getDoubleExtra(TAG_USER_RATING, 0);
        String plotSummary = intent.getStringExtra(TAG_PLOT_SUMMARY);
        String posterUrl = intent.getStringExtra(TAG_POSTER_URL);

        textView_title.setText(title);
        textView_releaseDate.setText(releaseDate);
        textView_userRating.setText(String.format(Locale.US, "%.1f", userRating));
        textView_plotSummary.setText(plotSummary);

        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + posterUrl).into(imageView);

        return view;
    }
}

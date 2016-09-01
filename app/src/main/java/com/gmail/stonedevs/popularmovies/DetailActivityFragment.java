package com.gmail.stonedevs.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.Locale;

public class DetailActivityFragment extends Fragment {
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

        Movie movie = Parcels.unwrap(getActivity().getIntent().getParcelableExtra(getString(R.string.intent_key_movie_parcel)));

        textView_title.setText(movie.title);
        textView_releaseDate.setText(movie.releaseDate);
        textView_userRating.setText(String.format(Locale.US, "%.1f", movie.userRating));
        textView_plotSummary.setText(movie.overview);

        Picasso.with(getContext()).load(getString(R.string.tmdb_image_base_url) + movie.posterUrl).into(imageView);

        return view;
    }
}

package com.gmail.stonedevs.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

public class MoviePosterAdapter extends ArrayAdapter<Movie> {
    public MoviePosterAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        }

        ImageView posterImage = (ImageView) convertView.findViewById(R.id.imageView_movie_poster);
        Picasso.with(getContext()).load(getContext().getString(R.string.tmdb_image_base_url) + movie.posterUrl).into(posterImage);

        return convertView;
    }

    /**
     * Overridden to clear adapter before adding collection
     *
     * @param collection List of Movie objects to add to adapter
     */
    @Override
    public void addAll(Collection<? extends Movie> collection) {
        this.clear();

        super.addAll(collection);
    }
}

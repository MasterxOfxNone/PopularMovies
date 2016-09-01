package com.gmail.stonedevs.popularmovies;

import org.parceler.Parcel;

@Parcel
public class Movie {
    String title;
    String releaseDate;
    String overview;
    Double userRating;
    String posterUrl;

    /**
     * Empty constructor as required per the Parceler library
     * https://guides.codepath.com/android/Using-Parceler
     */
    public Movie() {
    }

    /**
     * Full constructor that takes all variable params to build object
     *
     * @param title       Movie title
     * @param releaseDate Release date of Movie
     * @param overview    Plot summary of Movie
     * @param userRating  Ratings of Movie per TheMovieDB.org
     * @param posterUrl   URL of poster image for Movie
     */
    public Movie(String title, String releaseDate, String overview, Double userRating, String posterUrl) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.userRating = userRating;
        this.posterUrl = posterUrl;
    }
}

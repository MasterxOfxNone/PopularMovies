package com.gmail.stonedevs.popularmovies;

public class Movie {
    private String mTitle;
    private String mReleaseDate;
    private String mOverview;
    private Double mUserRating;
    private String mPosterUrl;

    public Movie(String title, String releaseDate, String overview, Double userRating, String posterUrl) {
        this.mTitle = title;
        this.mReleaseDate = releaseDate;
        this.mOverview = overview;
        this.mUserRating = userRating;
        this.mPosterUrl = posterUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getOverview() {
        return mOverview;
    }

    public Double getUserRating() {
        return mUserRating;
    }

    public String getPosterUrl() {
        return mPosterUrl;
    }
}

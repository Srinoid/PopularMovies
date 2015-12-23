package com.srinumallidi.popularmovies.data;

/**
 * Created by Srinu Mallidi on 12/22/2015.
 */

public class MovieData{
    public final String title;
    public final String posterPath;
    public final String overview;
    public final String rating;
    public final String releaseDate;
    public final String runtime;
    public final String voteCount;

    public MovieData(String title,
                     String posterPath,
                     String overview,
                     String rating,
                     String releaseDate,String runtime,String voteCount) {
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.voteCount = voteCount;
    }
}
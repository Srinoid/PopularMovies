package com.srinumallidi.popularmovies.utils;

import com.srinumallidi.popularmovies.BuildConfig;

/**
 * Created by Srinu Mallidi
 */
public class Constants {

    // JSON Constant Keys
    static final public String TITLE = "original_title";
    static final public String POSTER = "poster_path";
    static final public String OVERVIEW = "overview";
    static final public String RATING = "vote_average";
    static final public String RELEASE = "release_date";
    static final public String DURATION = "runtime";
    static final public String VOTECOUNT = "vote_count";

    // TMDB Query URL Components
    static final public String TMDB_DISCOVER_MOVIES_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
    static final public String TMDB_SORT_PARAM = "sort_by";
    static final public String TMDB_API_KEY_PARAM = "api_key";
    static final public String TMDB_API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
}

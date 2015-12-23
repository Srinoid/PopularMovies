package com.srinumallidi.popularmovies.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.srinumallidi.popularmovies.R;
import com.srinumallidi.popularmovies.data.MovieData;
import com.srinumallidi.popularmovies.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Srinu Mallidi.
 */
public class MovieDetailFragment extends Fragment {

    private MovieData movieData;
   ProgressDialog mProgressDialog;

    TextView originalTitle;
    TextView releaseDate;
    TextView filmRating;
    TextView plotSynopsis;
    TextView mRuntime;
    ImageView filmPoster;
    RatingBar ratingBar;
    Button favButton;
    View black_line;
    Boolean isFavorite = true;

    private int filmId;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra("filmId")) {
            filmId = intent.getIntExtra("filmId", 0);
        }

        originalTitle = (TextView) rootView.findViewById(R.id.movie_title);
        releaseDate = (TextView) rootView.findViewById(R.id.movie_year);
        plotSynopsis = (TextView) rootView.findViewById(R.id.movie_overview);
        filmRating = (TextView) rootView.findViewById(R.id.movie_rating);
        ratingBar = (RatingBar)rootView.findViewById(R.id.ratingBar);
        black_line = (View)rootView.findViewById(R.id.black_line);
        favButton = (Button)rootView.findViewById(R.id.bt_fav);
        mRuntime = (TextView) rootView.findViewById(R.id.runtime);

        filmPoster = (ImageView) rootView.findViewById(R.id.movie_poster);

        // Setting the onclick listener based on if the movie is already in the fav list or not.
        // As of now simply maintaining status. Stage 2 i will implement this feature.
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFavorite){
                    favButton.setText("Unmark As Favorite");
                    isFavorite=false;
                }else {
                    favButton.setText("Mark As Favorite");
                    isFavorite=true;
                }
            }
        });

        updateData();

        return rootView;
    }

    private void updateData() {
        FetchFilmData filmsPosterTask = new FetchFilmData();
        filmsPosterTask.execute(Constants.TMDB_API_KEY);
    }

    public class FetchFilmData extends AsyncTask<String, Void, MovieData> {

        private final String LOG_TAG = FetchFilmData.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MovieDetailFragment.this.getContext());
            mProgressDialog.setTitle("Please Wait..");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        private MovieData getFilmDataFromJson(String filmJsonStr)
                throws JSONException {
            JSONObject filmJson = new JSONObject(filmJsonStr);
            MovieData filmData = new MovieData(
                    filmJson.getString(Constants.TITLE),
                    MoviesGridFragment.builtImageUrl(filmJson.getString(Constants.POSTER)),
                    filmJson.getString(Constants.OVERVIEW),
                    filmJson.getString(Constants.RATING),
                    filmJson.getString(Constants.RELEASE),
                    filmJson.getString(Constants.DURATION),
                    filmJson.getString(Constants.VOTECOUNT)
            );

            return filmData;
        }

        @Override
        protected MovieData doInBackground(String... params) {


            // Verify if there is an api key
            if (params.length != 1) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // For JSON response
            String filmJsonStr = null;

            try {
                final String BASE_URL = String.format("http://api.themoviedb.org/3/movie/%s?", Integer.toString(filmId));

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(Constants.TMDB_API_KEY_PARAM, params[0])
                        .build();

                URL url = new URL(builtUri.toString());

                // Set up the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read input
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader((new InputStreamReader(inputStream)));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Add newline for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                filmJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                    }
                }
            }
            try {
                return getFilmDataFromJson(filmJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieData result) {
            if (result != null) {
                originalTitle.setText(result.title);
                plotSynopsis.setText(result.overview);
                filmRating.setText(result.rating + "/10");
                mRuntime.setText(result.runtime);
                int mVoteCount = Integer.parseInt(result.voteCount);
                float rating = mVoteCount / 2;
                ratingBar.setRating(rating);

                if(result.releaseDate.length() > 4) {
                    releaseDate.setText(result.releaseDate.substring(0, 4));
                }
                Picasso.with(getActivity())
                        .load(result.posterPath)
                        .into(filmPoster);
                mProgressDialog.dismiss();
            }
        }
    }

}

package com.srinumallidi.popularmovies.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


import com.srinumallidi.popularmovies.DetailActivity;
import com.srinumallidi.popularmovies.R;
import com.srinumallidi.popularmovies.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Srinu Mallidi.
 */
public class MoviesGridFragment extends Fragment {

    ImageAdapter mPostersAdapter;


     public MoviesGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    final class PosterAndFilmId {
        private final int filmId;
        private final String posterPath;

        public PosterAndFilmId(int filmId, String posterPath) {
            this.filmId = filmId;
            this.posterPath = posterPath;
        }

        public int getFilmId() {
            return filmId;
        }

        public String getPosterPath() {
            return posterPath;
        }
    }

    public static String builtImageUrl(String imageName) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMG_SIZE = "w500";

        return BASE_URL + IMG_SIZE + imageName;
    }


    private int getColumnsNumber() {
        int orientation = getResources().getConfiguration().orientation;
        int columnsNum;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnsNum = 4;
        } else {
            columnsNum = 2;
        }
        return columnsNum;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPostersAdapter = new ImageAdapter(getActivity());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setNumColumns(getColumnsNumber());

        gridView.setAdapter(mPostersAdapter);

        //on item click go to Detail Activity
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int filmId = mPostersAdapter.getItem(position).getFilmId();
               Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("filmId", filmId);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateData() {
        FetchFilmsPosterTask filmsPosterTask = new FetchFilmsPosterTask();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = prefs.getString("sort_order", "popularity.desc");

        filmsPosterTask.execute(sortOrder, Constants.TMDB_API_KEY);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData();
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ArrayList<PosterAndFilmId> imagesLinksAndFilmsId;

        public ImageAdapter(Context c) {
            mContext = c;
            imagesLinksAndFilmsId = new ArrayList<>();
        }

        private int getImageWidth() {
            double size = mContext.getResources().getDisplayMetrics().widthPixels;
            size = size / getColumnsNumber();
            return (int) size;
        }

        private int getImageHeight() {
            double size = getImageWidth() * 1.5;
            return (int) size;
        }

        public int getCount() {
            return imagesLinksAndFilmsId.size();
        }

        public PosterAndFilmId getItem(int position) {
            return imagesLinksAndFilmsId.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);

                imageView.setLayoutParams(
                        new GridView.LayoutParams(getImageWidth(), getImageHeight()));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            Picasso
                    .with(mContext)
                    .load(imagesLinksAndFilmsId.get(position).posterPath)
                    .into(imageView);

            return imageView;
        }

        public void clear() {
            imagesLinksAndFilmsId.clear();
        }

        public void add(PosterAndFilmId newElement) {
            imagesLinksAndFilmsId.add(newElement);
        }

    }

    public class FetchFilmsPosterTask extends AsyncTask<String, Void, ArrayList<PosterAndFilmId>> {

        private final String LOG_TAG = FetchFilmsPosterTask.class.getSimpleName();


        private ArrayList<PosterAndFilmId> getPostersLinksFromJson(List<String> filmsJsonStrings)
                throws JSONException {
            // Names of JSON objects to extract
            final String LIST = "results";
            final String ID = "id";
            final String POSTER = "poster_path";

            ArrayList<PosterAndFilmId> resultPairs = new ArrayList<PosterAndFilmId>();
            for(String filmsJsonStr : filmsJsonStrings) {
                JSONObject filmsJson = new JSONObject(filmsJsonStr);
                JSONArray filmsArray = filmsJson.getJSONArray(LIST);

                int filmsCount = filmsArray.length();


                for(int i = 0; i < filmsCount; i++) {
                    int filmId;
                    String posterPath;

                    JSONObject filmData = filmsArray.getJSONObject(i);

                    filmId = filmData.getInt(ID);
                    posterPath = filmData.getString(POSTER);

                    resultPairs.add(new PosterAndFilmId(filmId, builtImageUrl(posterPath)));
                }
            }
            return resultPairs;
        }

        private String getOnePageData(URL url) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String result;

            // Set up the connection
            try {
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

                result = buffer.toString();
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
            return result;
        }

        @Override
        protected ArrayList<PosterAndFilmId> doInBackground(String... params) {

            // Verify if there are a sorting order and an api key
            if (params.length != 2) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // For JSON responses
            List<String> filmsJsonStrings = new ArrayList<>();

            try {


                final String PAGE_PARAM = "page";
                final int PAGE_AMOUNT = 10;

                for(int page = 1; page <= PAGE_AMOUNT; page++) {
                    Uri builtUri = Uri.parse(Constants.TMDB_DISCOVER_MOVIES_BASE_URL).buildUpon()
                            .appendQueryParameter(Constants.TMDB_SORT_PARAM, params[0])
                            .appendQueryParameter(Constants.TMDB_API_KEY_PARAM, params[1])
                            .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                            .build();

                    URL url = new URL(builtUri.toString());
                    String pageData = getOnePageData(url);

                    // check receive data or not
                    if(pageData != null) {
                        filmsJsonStrings.add(pageData);
                    } else {
                        return null;
                    }

                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            try {
                return getPostersLinksFromJson(filmsJsonStrings);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PosterAndFilmId> result) {
            if (result != null) {
                mPostersAdapter.clear();
                mPostersAdapter.imagesLinksAndFilmsId = result;
                mPostersAdapter.notifyDataSetChanged();

            }
        }

    }


}

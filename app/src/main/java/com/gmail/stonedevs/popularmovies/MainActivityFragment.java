package com.gmail.stonedevs.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivityFragment extends Fragment {

    // Visit https://www.themoviedb.org to create an account and apply for a unique API key.
    private static final String API_KEY = "CHANGE_THIS_TO_API_KEY"; // API Key String Value

    // Adapter for the GridView to use in order to display images
    MoviePosterAdapter mMoviePosterAdapter;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviePosterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<Movie>());

        GridView movieGridView = (GridView) view.findViewById(R.id.gridView_movie_posters);
        movieGridView.setAdapter(mMoviePosterAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMoviePosterAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(getString(R.string.intent_key_movie_parcel), Parcels.wrap(movie));

                startActivity(intent);
            }
        });

        fetchMovieData();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        String prefsSearchMode = getPrefsMovieSearchMode();
        String prefsSearchModePopular = getString(R.string.prefs_search_mode_value_popular);
        String prefsSearchModeTopRated = getString(R.string.prefs_search_mode_value_top_rated);

        // Set Movie Search Mode menu item check state
        if (prefsSearchMode.equals(prefsSearchModePopular)) {
            menu.findItem(R.id.action_search_mode_popular).setChecked(true);
        } else if (prefsSearchMode.equals(prefsSearchModeTopRated)) {
            menu.findItem(R.id.action_search_mode_top_rated).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_mode_popular:
                if (!getPrefsMovieSearchMode().equals(getString(R.string.prefs_search_mode_value_popular))) {
                    item.setChecked(true);
                    setPrefsMovieSearchMode(getString(R.string.prefs_search_mode_value_popular));
                    fetchMovieData();
                    return false;
                }
                break;
            case R.id.action_search_mode_top_rated:
                if (!getPrefsMovieSearchMode().equals(getString(R.string.prefs_search_mode_value_top_rated))) {
                    item.setChecked(true);
                    setPrefsMovieSearchMode(getString(R.string.prefs_search_mode_value_top_rated));
                    fetchMovieData();
                    return false;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Gets settings info for Movie Search Mode
     *
     * @return String value of current/default Movie Search Mode (popular/top_rated)
     */
    private String getPrefsMovieSearchMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return prefs.getString(getString(R.string.prefs_search_mode_key), getString(R.string.prefs_search_mode_default_value));
    }

    /**
     * Sets settings info for Movie Search Mode
     *
     * @param searchMode String value of requested Movie Search Mode (popular/top_rated)
     */
    private void setPrefsMovieSearchMode(String searchMode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prefs.edit()
                .putString(getString(R.string.prefs_search_mode_key), searchMode)
                .apply();
    }

    /**
     * FetchMovieTask takes 2 parameters: Search mode string value, API Key from your account.
     * API Key needs to be edited before results will appear.
     */
    private void fetchMovieData() {
        if (isOnline()) {
            if (hasApiKey()) {
                new FetchMovieTask().execute(getPrefsMovieSearchMode(), API_KEY);
            } else {
                Toast.makeText(getActivity(), getString(R.string.toast_no_api_key_message), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_no_internet_connectivity), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method that checks for Internet Connectivity.
     * http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
     *
     * @return True/False depending upon if Phone has Internet Connectivity.
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Method that checks if developer changes API_KEY with their own API Key from TheMovieDB.org.
     *
     * @return True/False depending upon if API_KEY was edited or not.
     */
    private boolean hasApiKey() {
        return !API_KEY.equals(getString(R.string.api_key_changeme));
    }

    /**
     * Custom AsyncTask class that downloads/returns a JSON list of movie information from TheMovieDB
     */
    private class FetchMovieTask extends AsyncTask<String, Void, String> {
        final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        public FetchMovieTask() {
        }

        /**
         * Background thread method that downloads data from TheMovieDB.org
         *
         * @param params String values: Movie Search Mode, API_KEY
         * @return SparseArray of Movie objects
         */
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            String mode = params[0];    //  Movie Search Mode
            String apiKey = params[1];  //  API Key

            try {
                final String BASE_URL = getString(R.string.tmdb_base_url) + mode;
                final String PARAM_API_KEY = getString(R.string.tmdb_api_key);

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, apiKey)
                        .build();

                String myURL = builtUri.toString();
                URL url = new URL(myURL);

                // Create the request to TheMovieDB.org, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.error_pretext), e);
                // If the code didn't successfully get the data, there's no point in parsing.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, getString(R.string.error_ioexception), e);
                    }
                }
            }

            return movieJsonStr;
        }

        @Override
        protected void onPostExecute(String movieJsonStr) {
            try {
                // Add List of Movie objects to MoviePosterAdapter (Overridden to clear adapter first)
                mMoviePosterAdapter.addAll(parseJsonStringIntoMovieObjects(movieJsonStr));
            } catch (JSONException | ParseException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that parses a JSON string, then returns its result as an ArrayList of Movie objects
     *
     * @param jsonStr JSON string downloaded/returned from FetchMovieTask
     * @return SparseArray of Movie objects
     * @throws JSONException  JSON parsing exception
     * @throws ParseException SimpleDateFormat parsing exception
     */
    private List<Movie> parseJsonStringIntoMovieObjects(String jsonStr)
            throws JSONException, ParseException {

        final String TMDB_RESULTS = getString(R.string.tmdb_json_results);
        final String TMDB_POSTER_PATH = getString(R.string.tmdb_json_poster_path);
        final String TMDB_OVERVIEW = getString(R.string.tmdb_json_overview);
        final String TMDB_RELEASE_DATE = getString(R.string.tmdb_json_release_date);
        final String TMDB_TITLE = getString(R.string.tmdb_json_title);
        final String TMDB_RATING = getString(R.string.tmdb_json_rating);

        JSONObject jsonObjectAsPage = new JSONObject(jsonStr);
        JSONArray jsonArrayWithResults = jsonObjectAsPage.getJSONArray(TMDB_RESULTS);

        List<Movie> movieList = new ArrayList<>();
        for (int i = 0; i < jsonArrayWithResults.length(); i++) {
            JSONObject jsonObjectAsElement = jsonArrayWithResults.getJSONObject(i);

            String title = jsonObjectAsElement.getString(TMDB_TITLE);
            Double rating = jsonObjectAsElement.getDouble(TMDB_RATING);
            String overview = jsonObjectAsElement.getString(TMDB_OVERVIEW);
            String posterPath = jsonObjectAsElement.getString(TMDB_POSTER_PATH);

            // http://stackoverflow.com/questions/11046053/how-to-format-date-string-in-java
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(jsonObjectAsElement.getString(TMDB_RELEASE_DATE));
            String releaseDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date);

            movieList.add(new Movie(title, releaseDate, overview, rating, posterPath));
        }

        return movieList;
    }
}

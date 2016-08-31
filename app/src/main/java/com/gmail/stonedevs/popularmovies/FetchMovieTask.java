package com.gmail.stonedevs.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom AsyncTask class that downloads a JSON list of movie information from TheMovieDB
 */
public class FetchMovieTask extends AsyncTask<String, Void, SparseArray<Movie>> {
    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private Context mContext;

    // This interface ensures that MainActivity will receive what values FetchMovieTask returns.
    // http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
    public interface AsyncResponse {
        void processFinish(SparseArray<Movie> movieListOutput);
    }

    // Delegate helper that holds MainActivity's implementation of AsyncResponse.
    private AsyncResponse mDelegate = null;

    public FetchMovieTask(AsyncResponse d, Context c) {
        this.mDelegate = d;
        this.mContext = c;
    }

    /**
     * Background thread method that downloads data from TheMovieDB.org
     *
     * @param params String values: Movie Search Mode, API_KEY
     * @return SparseArray of Movie objects
     */
    @Override
    protected SparseArray<Movie> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;

        String mode = params[0];    //  Movie Search Mode
        String apiKey = params[1];  //  API Key

        try {
            final String BASE_URL = getContext().getString(R.string.tmdb_base_url) + mode;
            final String PARAM_API_KEY = getContext().getString(R.string.tmdb_api_key);

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
            Log.e(LOG_TAG, getContext().getString(R.string.error_pretext), e);
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
                    Log.e(LOG_TAG, getContext().getString(R.string.error_ioexception), e);
                }
            }
        }

        try {
            return getListOfMoviesFromJson(movieJsonStr);
        } catch (JSONException | ParseException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(SparseArray<Movie> movies) {
        // Push return value back to MainActivity through AsyncResponse (mDelegate).
        mDelegate.processFinish(movies);
    }

    private Context getContext() {
        return mContext;
    }

    private SparseArray<Movie> getListOfMoviesFromJson(String movieJsonStr)
            throws JSONException, ParseException {

        final String TMDB_RESULTS = getContext().getString(R.string.tmdb_json_results);
        final String TMDB_POSTER_PATH = getContext().getString(R.string.tmdb_json_poster_path);
        final String TMDB_OVERVIEW = getContext().getString(R.string.tmdb_json_overview);
        final String TMDB_RELEASE_DATE = getContext().getString(R.string.tmdb_json_release_date);
        final String TMDB_TITLE = getContext().getString(R.string.tmdb_json_title);
        final String TMDB_RATING = getContext().getString(R.string.tmdb_json_rating);

        JSONObject moviePageJSON = new JSONObject(movieJsonStr);
        JSONArray resultsJSONArray = moviePageJSON.getJSONArray(TMDB_RESULTS);

        SparseArray<Movie> listOfMovies = new SparseArray<>();
        for (int i = 0; i < resultsJSONArray.length(); i++) {
            JSONObject movieJSON = resultsJSONArray.getJSONObject(i);

            String title = movieJSON.getString(TMDB_TITLE);
            Double rating = movieJSON.getDouble(TMDB_RATING);
            String overview = movieJSON.getString(TMDB_OVERVIEW);
            String posterPath = movieJSON.getString(TMDB_POSTER_PATH);

            // http://stackoverflow.com/questions/11046053/how-to-format-date-string-in-java
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(movieJSON.getString(TMDB_RELEASE_DATE));
            String releaseDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date);

            listOfMovies.put(listOfMovies.size(), new Movie(title, releaseDate, overview, rating, posterPath));
        }

        return listOfMovies;
    }
}

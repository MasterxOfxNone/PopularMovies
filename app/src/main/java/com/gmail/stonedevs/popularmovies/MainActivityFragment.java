package com.gmail.stonedevs.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivityFragment extends Fragment
        implements FetchMovieTask.AsyncResponse {

    // Visit https://www.themoviedb.org to create an account and apply for a unique API key.
    private static final String API_KEY = "CHANGE_ME_TO_API_KEY"; // API Key String Value

    // Adapter for the GridView to use in order to display images
    ImageAdapter mMoviePosterAdapter;

    // SparseArray that contains a list of Movie objects
    SparseArray<Movie> mMovieList;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviePosterAdapter = new ImageAdapter();

        GridView movieGridView = (GridView) view.findViewById(R.id.gridView_movie_posters);
        movieGridView.setAdapter(mMoviePosterAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieList.valueAt(position);

                // If Movie object isn't null, put data into Intent object; start the activity
                assert movie != null;
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(getString(R.string.intent_key_movie_poster_url), movie.getPosterUrl())
                        .putExtra(getString(R.string.intent_key_movie_title), movie.getTitle())
                        .putExtra(getString(R.string.intent_key_movie_release_date), movie.getReleaseDate())
                        .putExtra(getString(R.string.intent_key_movie_user_rating), movie.getUserRating())
                        .putExtra(getString(R.string.intent_key_movie_plot_summary), movie.getOverview());
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
     * Method that receives results from FetchMovieTask PostExecute() and parses the results into
     * a list of Movie objects to use later and add all found images to the ImageAdapter
     *
     * @param movies SparseArray of Movie objects in direct result of FetchMovieTask
     */
    @Override
    public void processFinish(SparseArray<Movie> movies) {
        mMovieList = movies.clone();

        mMoviePosterAdapter.clear();
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.valueAt(i);

            ImageView imageView = new ImageView(getActivity());
            imageView.setAdjustViewBounds(true);

            Picasso.with(getContext()).load(getString(R.string.tmdb_image_base_url) + movie.getPosterUrl()).into(imageView);

            mMoviePosterAdapter.add(imageView);
        }
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
                new FetchMovieTask(this, getActivity()).execute(getPrefsMovieSearchMode(), API_KEY);
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
}

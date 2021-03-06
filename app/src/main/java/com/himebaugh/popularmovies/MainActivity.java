package com.himebaugh.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.himebaugh.popularmovies.adapter.MovieAdapter;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.utils.MovieUtils;

import java.io.IOException;
import java.util.ArrayList;

import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MovieAdapter.MovieAdapterOnClickHandler {

    private final static String TAG = MainActivity.class.getName();

    private static final String MOVIE_LIST_KEY = "movieList";
    private static final String MOVIE_KEY = "movie";
    private static final String BUNDLE = "bundle";
    private static final String POPULAR = "popular";
    private static final String TOP_RATED = "top_rated";

    private MovieAdapter mAdapter;
    private Context mContext;
    private Boolean moviesHaveBeenLoaded = false;

    private String page = "1";

    private ArrayList<Movie> mMovieList;
    private int mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);

        // Get default sort/filter from SharedPreferences settings...
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadFilterFromPreferences(sharedPreferences);

        // CHANGE GRID SpanCount ON ROTATION
        GridLayoutManager layoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 4);
        }

        mRecyclerView.setLayoutManager(layoutManager);
        // allows for optimizations if all items are of the same size:
        mRecyclerView.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MovieAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {

            // use MovieList from savedInstanceState to avoid making an API Call!
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);

            mAdapter.loadMovies(mMovieList);
            mAdapter.notifyDataSetChanged();

        } else {

            if (!moviesHaveBeenLoaded) {
                loadMovies(mFilter, page);
            }

        }

    }

    // SAVE MovieList on ROTATION so it doesn't make API Call!
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(MOVIE_LIST_KEY, mMovieList);
    }

    @Override
    public void onClick(Movie movie) {

        Context context = this;

        // Launch the DetailActivity using an explicit Intent
        Class destinationActivity = DetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MOVIE_KEY, movie);
        intent.putExtra(BUNDLE, bundle);

        startActivity(intent);
    }

    private void loadMovies(int filter, String page) {

        // Change subtitle as necessary
        switch (filter) {
            case MovieUtils.FILTER_POPULAR:
                getSupportActionBar().setSubtitle(R.string.action_filter_popular);
                break;
            case MovieUtils.FILTER_TOPRATED:
                getSupportActionBar().setSubtitle(R.string.action_filter_top_rated);
                break;
            case MovieUtils.FILTER_FAVORITE:
                getSupportActionBar().setSubtitle(R.string.action_filter_favorite);
                break;
            default:
                getSupportActionBar().setSubtitle(R.string.action_filter_popular);
        }

        new MovieQueryTask().execute(filter);
    }

    private void loadFilterFromPreferences(SharedPreferences sharedPreferences) {

        String filter = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_value_popular));

        switch (filter) {
            case POPULAR:
                mFilter = MovieUtils.FILTER_POPULAR;
                break;
            case TOP_RATED:
                mFilter = MovieUtils.FILTER_TOPRATED;
                break;
            default:
                mFilter = MovieUtils.FILTER_FAVORITE;
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadFilterFromPreferences(sharedPreferences);
    }

    class MovieQueryTask extends AsyncTask<Integer, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: ");
        }

        @Override
        protected ArrayList<Movie> doInBackground(Integer... params) {

            int filter = params[0];

            ArrayList<Movie> movieList = null;

            switch (filter) {
                case MovieUtils.FILTER_POPULAR:
                case MovieUtils.FILTER_TOPRATED:
                    if (isNetworkAvailable(mContext)) {
                        // Network Available make API call
                        try {
                            movieList = MovieUtils.getMovieList(mContext, filter);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Network NOT Available load from database
                        movieList = MovieUtils.getMovieListFromCursor(mContext, filter);
                    }
                    break;
                case MovieUtils.FILTER_FAVORITE:
                    // Load Favorites from database
                    movieList = MovieUtils.getMovieListFromCursor(mContext, filter);
                    break;
                default:
                    // Should not ever get here...
                    movieList = MovieUtils.getMovieListFromCursor(mContext, filter);
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {

            // Save to store on rotation
            mMovieList = movieList;

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (movieList == null) {

                // This should only happen if no internet connection when app is run for the first time
                Toast.makeText(getApplicationContext(), R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

            } else {
                mAdapter.loadMovies(mMovieList);
                mAdapter.notifyDataSetChanged();

                if (mMovieList.size() > 0) {
                    moviesHaveBeenLoaded = true;
                }
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        // UI contains a settings menu to toggle the filter of the movies by: most popular, highest rated.
        // When a user changes the filter criteria (“most popular and highest rated”) the main view gets updated
        // by calling loadMovies below.

        switch (id) {
            case R.id.action_filter_popular:
                mFilter = MovieUtils.FILTER_POPULAR;
                loadMovies(mFilter, page);
                return true;
            case R.id.action_filter_top_rated:
                mFilter = MovieUtils.FILTER_TOPRATED;
                loadMovies(mFilter, page);
                return true;
            case R.id.action_filter_favorite:
                mFilter = MovieUtils.FILTER_FAVORITE;
                loadMovies(mFilter, page);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                // return false;
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Need to reload movies, just in-case they have changed...
        // If I favorite a movie in DetailActivity and then return to MainActivity,
        // re-loading movies here is the only way to see an update.
        // ...Unless I use a CursorLoader, but that won't meet rubric criteria.
        loadMovies(mFilter, page);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showConnectivityStatus(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {

            if (!moviesHaveBeenLoaded) {
                loadMovies(mFilter, page);
            }

        } else if (!moviesHaveBeenLoaded) {

            // Display a Toast that the internet is down...
            // But only if no movies are displayed and there is no internet connection.
            Toast.makeText(this, R.string.msg_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

}



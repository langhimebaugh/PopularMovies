package com.himebaugh.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.himebaugh.popularmovies.adapter.MovieAdapter;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.utils.MovieUtils;
import com.himebaugh.popularmovies.utils.NetworkUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private final static String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private Context mContext;
    //private int mMovieCount = 0;
    private Boolean moviesHaveBeenLoaded = false;

    NetworkBroadcastReceiver mNetworkReceiver;
    IntentFilter mNetworkIntentFilter;

    GridLayoutManager layoutManager;

    ArrayList<Movie> mMovieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mRecyclerView = findViewById(R.id.recyclerView);


        // CHANGE GRID SpanCount ON ROTATION
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 4);
        }

        // LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // May save position and reset upon orientation change???
        // layoutManager.scrollToPosition(0);

        mRecyclerView.setLayoutManager(layoutManager);
        // allows for optimizations if all items are of the same size:
        mRecyclerView.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MovieAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        // RELOADS ON ORIENTATION CHANGE & COMING BACK FROM DETAIL ACTIVITY
        // FIX IN PART-2 OF PROJECT
        // FRAGMENTS MAY HELP

        Log.i(TAG, "onCreate - moviesHaveBeenLoaded: " + moviesHaveBeenLoaded);


        if (savedInstanceState != null) {

            Log.i(TAG, "onCreate: RESTORE FROM savedInstanceState");

            mMovieList =  savedInstanceState.getParcelableArrayList("movieList");

            mAdapter.loadMovies(mMovieList);
            mAdapter.notifyDataSetChanged();

        } else {

            if (!moviesHaveBeenLoaded) {
                loadMovies(MovieUtils.FILTER_POPULAR, "1");
            }

        }



        // Setup and register the broadcast receiver
        // This is only used for a TOAST message...
        // Not related to any other logic
        mNetworkIntentFilter = new IntentFilter();
        mNetworkReceiver = new NetworkBroadcastReceiver();      // CONNECTIVITY_ACTION
        mNetworkIntentFilter.addAction(CONNECTIVITY_ACTION);    // Intent.ACTION_AIRPLANE_MODE_CHANGED

    }

    // SAVE MovieList on ROTATION so it doesn't make API Call!

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("movieList", mMovieList);

    }



    @Override
    public void onClick(Movie movie) {

        Context context = this;

        Log.i(TAG, "onClick: " + movie.getTitle());
        Log.i(TAG, "onClick: " + movie.getId());

        // Launch the DetailActivity using an explicit Intent
        Class destinationActivity = DetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        Bundle bundle = new Bundle();
        bundle.putParcelable("movie", movie);
        intent.putExtra("bundle", bundle);

        // seemed simpler but this won't work...
        // intent.putParcelableArrayListExtra("movie", movie);

        startActivity(intent);

    }

    // private void loadMovies(String movieDatabaseUrl, String page) {
    private void loadMovies(int filter, String page) {

        Log.i(TAG, "loadMovies: ");

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

    public class MovieQueryTask extends AsyncTask<Integer, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: ");
            // mLoadingIndicatorProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(Integer... params) {
            Log.i(TAG, "doInBackground: ");
            // URL url = params[0];
            int filter = params[0];

            ArrayList<Movie> movieList = null;

            if (isNetworkAvailable(mContext)) {
                Log.i(TAG, "Network is Available");
                try {
                    movieList = MovieUtils.getMovieList(mContext, filter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "Network is NOT Available");
                movieList = MovieUtils.getMovieListFromCursor(mContext, filter);
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {


            // Save to store on rotation
            mMovieList = movieList;

            Log.i(TAG, "onPostExecute: ");

//            Log.i(TAG, "movieList.toString(): " + movieList.toString());
//
            Log.i(TAG, "movieList.size()" + movieList.size());
//
//            final ListIterator<Movie> listIterator = movieList.listIterator();
//
//            while (listIterator.hasNext()) {
//                Movie movie = listIterator.next();
//
//                Log.i(TAG, "listIterator: " + movie.getTitle() + " " + movie.getPosterPath());
//            }

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (movieList == null) {

                // This should only happen initially as data should be pulling from MovieProvider
                Toast.makeText(getApplicationContext(), R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

            } else {
                mAdapter.loadMovies(mMovieList);
                mAdapter.notifyDataSetChanged();

                if (mMovieList.size() > 0) {
                    moviesHaveBeenLoaded = true;
                }

                Log.i(TAG, "moviesHaveBeenLoaded: " + moviesHaveBeenLoaded);
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

        Log.i(TAG, "onOptionsItemSelected: ");

        switch (id) {
            case R.id.action_filter_popular:
                loadMovies(MovieUtils.FILTER_POPULAR, "1");
                return true;
            case R.id.action_filter_top_rated:
                loadMovies(MovieUtils.FILTER_TOPRATED, "1");
                return true;
            case R.id.action_filter_favorite:
                loadMovies(MovieUtils.FILTER_FAVORITE, "1");
                return true;
            default:
                // return false;
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);



    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkReceiver, mNetworkIntentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkReceiver);
    }


    private void showConnectivityStatus(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {

            Log.i(TAG, "Internet Connection");

            if (!moviesHaveBeenLoaded) {
                loadMovies(MovieUtils.FILTER_POPULAR, "1");
            }

            // Toast.makeText(this, "Internet Connection", Toast.LENGTH_LONG).show();

        } else if (!moviesHaveBeenLoaded){

            // Display a Toast that the internet is down...
            // But only if no movies are displayed and there is no internet connection.

            Log.i(TAG, "No Internet Connection");

            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();

        }
    }


    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            boolean connectivityChange = (action.equals(CONNECTIVITY_ACTION));

            if (connectivityChange) {

                Log.i(TAG, "onReceive: " + CONNECTIVITY_ACTION);

                showConnectivityStatus(NetworkUtil.isNetworkAvailable(context));
            }

        }
    }

}



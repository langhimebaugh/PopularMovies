package com.himebaugh.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.MenuItemCompat;

import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.utils.MovieUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private final static String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    //private int mMovieCount = 0;
    private Boolean moviesHaveBeenLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

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


        if (!moviesHaveBeenLoaded) {
            loadMovies(MovieUtils.TOPRATED_MOVIES, "1");
        }


    }

    @Override
    public void onClick(Movie movie) {

        Context context = this;

        Log.i(TAG, "onClick: " + movie.getTitle());

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

    private void loadMovies(String movieDatabaseUrl, String page) {

        // In a background thread, app queries the /movie/popular or /movie/top_rated API for the sort criteria specified in the settings menu.

        // Change subtitle as necessary
        if (movieDatabaseUrl == MovieUtils.POPULAR_MOVIES) {
            getSupportActionBar().setSubtitle("Most Popular");
        } else if (movieDatabaseUrl == MovieUtils.TOPRATED_MOVIES) {
            getSupportActionBar().setSubtitle("Highest Rated");
        }

        URL queryUrl = MovieUtils.buildUrl(movieDatabaseUrl, page);

        new MovieQueryTask().execute(queryUrl);
    }

    public class MovieQueryTask extends AsyncTask<URL, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // mLoadingIndicatorProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(URL... params) {
            URL url = params[0];

            List<Movie> movieList = null;

            try {
                movieList = MovieUtils.getMovieList(url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {

//            Log.i(TAG, "movieList.toString(): " + movieList.toString());
//
//            Log.i(TAG, "movieList.size()" + movieList.size());
//
//            final ListIterator<Movie> listIterator = movieList.listIterator();
//
//            while (listIterator.hasNext()) {
//                Movie movie = listIterator.next();
//
//                Log.i(TAG, "listIterator: " + movie.getTitle() + " " + movie.getPosterPath());
//            }

            mAdapter.loadMovies(movieList);
            mAdapter.notifyDataSetChanged();

            if (movieList.size() > 0) {
                moviesHaveBeenLoaded = true;
            }

            Log.i(TAG, "moviesHaveBeenLoaded: " + moviesHaveBeenLoaded);


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

        // UI contains a settings menu to toggle the sort order of the movies by: most popular, highest rated.
        // When a user changes the sort criteria (“most popular and highest rated”) the main view gets updated
        // by calling loadMovies below.

        switch (id) {
            case R.id.action_sort_popular:
                loadMovies(MovieUtils.POPULAR_MOVIES, "1");
                return true;
            case R.id.action_sort_top_rated:
                loadMovies(MovieUtils.TOPRATED_MOVIES, "1");
                return true;
            default:
                // return false;
                return super.onOptionsItemSelected(item);
        }

    }

}

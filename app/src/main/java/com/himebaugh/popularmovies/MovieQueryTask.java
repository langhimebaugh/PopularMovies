package com.himebaugh.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.utils.MovieUtils;

import java.io.IOException;
import java.util.List;

import static android.support.constraint.Constraints.TAG;
import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class MovieQueryTask extends AsyncTask<Integer, Void, List<Movie>> {

    private final String TAG = MovieQueryTask.class.getSimpleName();

    Context mContext;
    Movie movie;
    List<Movie> movieList = null;

    public MovieQueryTask(Context context, Movie movie) {
        mContext = context;
        this.movie = movie;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "onPreExecute: ");
        // mLoadingIndicatorProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<Movie> doInBackground(Integer... params) {
        Log.i(TAG, "doInBackground: ");
        // URL url = params[0];
        int filter = params[0];

        List<Movie> movieList = null;

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
    protected void onPostExecute(List<Movie> movieList) {

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
            Toast.makeText(mContext, R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

        } else {
//            mAdapter.loadMovies(movieList);
//            mAdapter.notifyDataSetChanged();
//
//            if (movieList.size() > 0) {
//                moviesHaveBeenLoaded = true;
//            }
//
//            Log.i(TAG, "moviesHaveBeenLoaded: " + moviesHaveBeenLoaded);
        }

    }

}

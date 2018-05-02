package com.himebaugh.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.utils.MovieUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // URL queryUrl = MovieUtils.buildUrl(MovieUtils.POPULAR_MOVIES, "1");
        URL queryUrl = MovieUtils.buildUrl(MovieUtils.TOPRATED_MOVIES, "2");
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

            Log.i(TAG, "movieList.toString(): " + movieList.toString());

            Log.i(TAG, "movieList.size()" + movieList.size());

            final ListIterator<Movie> listIterator = movieList.listIterator();

            while (listIterator.hasNext()) {
                Movie movie = listIterator.next();

                Log.i(TAG, "listIterator: " + movie.getTitle() + " " + movie.getPosterPath());
            }

        }


    }

}

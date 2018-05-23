package com.himebaugh.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.himebaugh.popularmovies.adapter.UserReviewAdapter;
import com.himebaugh.popularmovies.adapter.VideoTrailerAdapter;
import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.databinding.ActivityDetailBinding;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.UserReview;
import com.himebaugh.popularmovies.model.VideoTrailer;
import com.himebaugh.popularmovies.utils.MovieUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class DetailActivity extends AppCompatActivity implements VideoTrailerAdapter.VideoTrailerAdapterOnClickHandler {

    private static final String TAG = DetailActivity.class.getName();
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE_SMALL = "w185"; //w500
    private static final String SIZE_LARGE = "w500";  //w780

    // COMPLETE: FAVORITES,
    // COMPLETE: Extend the favorites ContentProvider to store the movie poster, synopsis, user rating, and release date, and display them even when offline.
    // COMPLETE: Implement sharing functionality to allow the user to share the first trailer’s YouTube URL from the movie details screen.

    // Then you will need a ‘size’, which will be one of the following: "w92",
    // "w154", "w185", "w342", "w500", "w780", or "original". For most phones we
    // recommend using “w185”.

    // poster_path
    // http://image.tmdb.org/t/p/w500/jjPJ4s3DWZZvI4vw8Xfi4Vqa1Q8.jpg

    // backdrop_path
    // http://image.tmdb.org/t/p/w500/9ywA15OAiwjSTvg3cBs9B7kOCBF.jpg

    private RecyclerView mVideoRecyclerView;
    private VideoTrailerAdapter mVideoAdapter;
    private RecyclerView mReviewRecyclerView;
    private UserReviewAdapter mReviewAdapter;

    private Boolean videoTrailersHaveBeenLoaded = false;
    private Boolean userReviewsHaveBeenLoaded = false;
    private Boolean isFavorite = false;
    private Boolean videoTrailerIsAvailableToShare = false;

    private Movie mMovie;
    private VideoTrailer mVideoTrailer;

    private MenuItem favoriteSelected;
    private MenuItem favoriteNotSelected;

    // Create Data Binding instance called mDetailBinding of type ActivityMainBinding
    private ActivityDetailBinding mDetailBinding;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mContext = this;

        // Implement Up Navigation - displays the back arrow in front of App icon in the Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiate mDetailBinding using DataBindingUtil
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {

            Bundle bundle = intentThatStartedThisActivity.getBundleExtra("bundle");

            mMovie = bundle.getParcelable("movie");

            getSupportActionBar().setTitle("Movie Details");
            getSupportActionBar().setSubtitle(mMovie.getTitle());

            // Bind movie data to the views
            mDetailBinding.titleTv.setText(mMovie.getTitle());
            mDetailBinding.releaseDateTv.setText(mMovie.getReleaseDate());
            mDetailBinding.voteAverageTv.setText(String.valueOf(mMovie.getVoteAverage()));
            mDetailBinding.tvPlotSynopsis.setText(mMovie.getOverview());

            String imageBackdropURL = BASE_URL + SIZE_LARGE + mMovie.getBackdropPath();
            Picasso.get().load(imageBackdropURL).into(mDetailBinding.movieBackdropIv);

            // COMPLETE: placeholder() and error() accept a drawable resource ID. CREATE a default image.
            String imagePosterURL = BASE_URL + SIZE_SMALL + mMovie.getPosterPath();
            Picasso.get().load(imagePosterURL).placeholder(R.mipmap.ic_launcher).error(R.drawable.offline).into(mDetailBinding.moviePosterIv);

            if (!videoTrailersHaveBeenLoaded) {
                loadVideoTrailers();
            }

            if (!userReviewsHaveBeenLoaded) {
                loadUserReviews();
            }

            Log.i(TAG, "onCreate: ID=" + mMovie.getId());


            // =========================================================================
            // Sets the current state of isFavorite according to value in database.

            Uri uri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, mMovie.getId());
            String[] projection = null;
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            // Set selection
            selection = MovieEntry._ID + " = " + mMovie.getId();

            Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

            cursor.moveToNext();

            if (cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_FAVORITE)) == 1) {
                isFavorite = true;
            } else {
                isFavorite = false;
            }

            cursor.close();

        }

    }

    @Override
    public void onClick(VideoTrailer videoTrailer) {

        Log.i(TAG, "onClick: " + videoTrailer.getName() + videoTrailer.getKey());

        // Launch YouTube
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoTrailer.getKey()));

        startActivity(intent);
    }

    private void loadVideoTrailers() {

        mVideoRecyclerView = findViewById(R.id.trailers_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mVideoRecyclerView.setLayoutManager(layoutManager);

        // allows for optimizations if all items are of the same size:
        mVideoRecyclerView.setHasFixedSize(true);

        // Initialize the video adapter and attach it to the RecyclerView
        mVideoAdapter = new VideoTrailerAdapter(this, this);
        mVideoRecyclerView.setAdapter(mVideoAdapter);

        Log.i(TAG, "loadVideoTrailers: movieID=" + mMovie.getId());

        new LoadVideoTrailersTask().execute(mMovie.getId());
    }

    public class LoadVideoTrailersTask extends AsyncTask<Integer, Void, List<VideoTrailer>> {

        @Override
        protected List<VideoTrailer> doInBackground(Integer... params) {
            int movieId = params[0];

            Log.i(TAG, "LoadVideoTrailersTask doInBackground: movieID=" + movieId);

            List<VideoTrailer> videoTrailerList = null;

            if (isNetworkAvailable(mContext)) {
                Log.i(TAG, "Network is Available");
                try {
                    videoTrailerList = MovieUtils.getVideoTrailerList(mContext, movieId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "Network is NOT Available");
                videoTrailerList = MovieUtils.getVideoTrailerListFromCursor(mContext, movieId);
            }

            return videoTrailerList;
        }

        @Override
        protected void onPostExecute(List<VideoTrailer> videoTrailerList) {

            Log.i(TAG, "onPostExecute: ");
            Log.i(TAG, "videoTrailerList.size()" + videoTrailerList.size());

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (videoTrailerList == null) {
                Toast.makeText(getApplicationContext(), R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

            } else {

                Log.i(TAG, "videoTrailerList.toString(): " + videoTrailerList.toString());

                Log.i(TAG, "movieList.size()" + videoTrailerList.size());

                final ListIterator<VideoTrailer> listIterator = videoTrailerList.listIterator();

                videoTrailerIsAvailableToShare = false;

                while (listIterator.hasNext()) {
                    VideoTrailer videoTrailer = listIterator.next();

                    if (!videoTrailerIsAvailableToShare && videoTrailer.getType().contentEquals("Trailer")) {

                        // Store the first VideoTrailer that "type" = "Trailer"
                        // Then flag as shareable to use in shareMovie()
                        mVideoTrailer = videoTrailer;

                        videoTrailerIsAvailableToShare = true;
                    }

                    Log.i(TAG, "listIterator: " + videoTrailer.getId() + " " + videoTrailer.getName() + " " + videoTrailer.getSite() + " " + videoTrailer.getType());
                }

                mVideoAdapter.loadVideoTrailers(videoTrailerList);
                mVideoAdapter.notifyDataSetChanged();

                if (videoTrailerList.size() > 0) {
                    videoTrailersHaveBeenLoaded = true;
                }

                Log.i(TAG, "moviesHaveBeenLoaded: " + videoTrailersHaveBeenLoaded);
            }

        }

    }

    private void loadUserReviews() {

        mReviewRecyclerView = findViewById(R.id.reviews_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mReviewRecyclerView.setLayoutManager(layoutManager);

        // allows for optimizations if all items are of the same size:
        mReviewRecyclerView.setHasFixedSize(true);

        // Initialize the video adapter and attach it to the RecyclerView
        mReviewAdapter = new UserReviewAdapter(this);
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        Log.i(TAG, "loadVideoTrailers: movieID=" + mMovie.getId());

        new LoadUserReviewsTask().execute(mMovie.getId());
    }

    public class LoadUserReviewsTask extends AsyncTask<Integer, Void, List<UserReview>> {

        @Override
        protected List<UserReview> doInBackground(Integer... params) {
            int movieId = params[0];

            List<UserReview> userReviewList = null;

            if (isNetworkAvailable(mContext)) {
                Log.i(TAG, "Network is Available");
                try {
                    userReviewList = MovieUtils.getUserReviewList(mContext, movieId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "Network is NOT Available");
                userReviewList = MovieUtils.getUserReviewListFromCursor(mContext, movieId);
            }

            return userReviewList;
        }

        @Override
        protected void onPostExecute(List<UserReview> userReviewList) {

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (userReviewList == null) {
                Toast.makeText(getApplicationContext(), R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

            } else {

                Log.i(TAG, "userReviewList.toString(): " + userReviewList.toString());

                Log.i(TAG, "userReviewList.size()" + userReviewList.size());

                final ListIterator<UserReview> listIterator = userReviewList.listIterator();

                while (listIterator.hasNext()) {
                    UserReview userReview = listIterator.next();

                    Log.i(TAG, "listIterator: " + userReview.getId() + " " + userReview.getAuthor());
                }

                mReviewAdapter.loadUserReviews(userReviewList);
                mReviewAdapter.notifyDataSetChanged();

                if (userReviewList.size() > 0) {
                    userReviewsHaveBeenLoaded = true;
                }

                Log.i(TAG, "userReviewsHaveBeenLoaded: =" + userReviewsHaveBeenLoaded);
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        favoriteSelected = menu.findItem(R.id.action_favorite_selected);
        favoriteNotSelected = menu.findItem(R.id.action_favorite_not_selected);

        favoriteSelected.setVisible(isFavorite);
        favoriteNotSelected.setVisible(!isFavorite);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        // UI contains a settings menu to SHARE a movie or toggle FAVORITE on/off.

        Log.i(TAG, "onOptionsItemSelected: ");

        switch (id) {
            case R.id.action_favorite_selected:
            case R.id.action_favorite_not_selected:
                toggleFavorite(mMovie.getId());
                return true;
            case R.id.action_share:
                shareMovie();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void shareMovie() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out " + mMovie.getTitle());

        if (videoTrailerIsAvailableToShare) {
            Uri videoUrl = Uri.parse("http://www.youtube.com/watch").buildUpon().appendQueryParameter("v", mVideoTrailer.getKey()).build();
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl.toString() + "\n" + mMovie.getOverview());
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getOverview());
        }

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.action_share)));
    }


    // Toggles isFavorite value associated with the heart shaped favorite icon in the Action Bar.
    // Updates database record with the Movie Id passed in.
    private void toggleFavorite(int movieId) {

        Log.i(TAG, "toggleFavorite: ");

        // toggle condition
        isFavorite = !isFavorite;

        Uri uri = MovieEntry.CONTENT_URI;
        Uri rowURI = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, movieId);
        String selection = null;
        String[] selectionArgs = null;

        // Create the updated row content, assigning values for each row.
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_FAVORITE, isFavorite);

        // Set selection
        selection = MovieEntry._ID + " = " + movieId;

        int rowsUpdated = getContentResolver().update(rowURI, contentValues, selection, selectionArgs);

        // getContentResolver().notify();
        getContentResolver().notifyChange(MovieEntry.CONTENT_URI, null);

        Log.i(TAG, "toggleFavorite: rowsUpdated=" + rowsUpdated);

        if (rowsUpdated > 0) {

            // update the action bar menu item by changing the button visibility for both icons
            favoriteSelected.setVisible(isFavorite);
            favoriteNotSelected.setVisible(!isFavorite);
        }

    }

}

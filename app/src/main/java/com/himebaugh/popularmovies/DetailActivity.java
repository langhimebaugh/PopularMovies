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

import com.himebaugh.popularmovies.adapter.UserReviewAdapter;
import com.himebaugh.popularmovies.adapter.VideoTrailerAdapter;
import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.databinding.ActivityDetailBinding;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.UserReview;
import com.himebaugh.popularmovies.model.VideoTrailer;
import com.himebaugh.popularmovies.utils.MovieUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class DetailActivity extends AppCompatActivity implements VideoTrailerAdapter.VideoTrailerAdapterOnClickHandler {

    private static final String TAG = DetailActivity.class.getName();
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE_SMALL = "w185"; //w500
    private static final String SIZE_LARGE = "w500";  //w780

    private static final String MOVIE_KEY = "movie";
    private static final String VIDEO_TRAILER_LIST_KEY = "videoTrailerList";
    private static final String USER_REVIEW_LIST_KEY = "userReviewList";
    private static final String BUNDLE = "bundle";
    private static final String TRAILER = "Trailer";



    private static final String TYPE_TEXT_PLAIN = "text/plain";
    private static final String EMAIL_SUBJECT = "Check out ";


    private static final String PLAY_YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String SHARE_YOUTUBE_URL = "http://www.youtube.com/watch";
    private static final String PARAMETER_V = "v";
    private static final String NEW_LINE = "\n";

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

    private VideoTrailerAdapter mVideoAdapter;
    private UserReviewAdapter mReviewAdapter;

    private Boolean isFavorite = false;
    private Boolean videoTrailerIsAvailableToShare = false;

    private Movie mMovie;
    private VideoTrailer mVideoTrailer;
    private ArrayList<VideoTrailer> mVideoTrailerList;
    private ArrayList<UserReview> mUserReviewList;

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

        if (savedInstanceState != null) {

            // RESTORE FROM savedInstanceState"
            mMovie = savedInstanceState.getParcelable(MOVIE_KEY);
            displayMovieInfo();

            mVideoTrailerList = savedInstanceState.getParcelableArrayList(VIDEO_TRAILER_LIST_KEY);
            setupVideoTrailers();
            mVideoAdapter.loadVideoTrailers(mVideoTrailerList);
            mVideoAdapter.notifyDataSetChanged();

            mUserReviewList = savedInstanceState.getParcelableArrayList(USER_REVIEW_LIST_KEY);
            setupUserReviews();
            mReviewAdapter.loadUserReviews(mUserReviewList);
            mReviewAdapter.notifyDataSetChanged();

        } else if (intentThatStartedThisActivity != null) {

            Bundle bundle = intentThatStartedThisActivity.getBundleExtra(BUNDLE);

            mMovie = bundle.getParcelable(MOVIE_KEY);

            displayMovieInfo();

            setupVideoTrailers();
            new LoadVideoTrailersTask().execute(mMovie.getId());

            setupUserReviews();
            new LoadUserReviewsTask().execute(mMovie.getId());
        }

    }

    // SAVE VideoTrailerList & UserReviewList on ROTATION so it doesn't make API Call!
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(MOVIE_KEY, mMovie);
        outState.putParcelableArrayList(VIDEO_TRAILER_LIST_KEY, mVideoTrailerList);
        outState.putParcelableArrayList(USER_REVIEW_LIST_KEY, mUserReviewList);
    }

    private void displayMovieInfo() {

        getSupportActionBar().setTitle( getString(R.string.title_activity_detail) );
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

        isFavorite = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_FAVORITE)) == 1;

        cursor.close();
    }


    @Override
    public void onClick(VideoTrailer videoTrailer) {

        // Launch YouTube
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_YOUTUBE_URL + videoTrailer.getKey()));

        startActivity(intent);
    }

    private void setupVideoTrailers() {

        RecyclerView mVideoRecyclerView = findViewById(R.id.trailers_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mVideoRecyclerView.setLayoutManager(layoutManager);

        // allows for optimizations if all items are of the same size:
        mVideoRecyclerView.setHasFixedSize(true);

        // Initialize the video adapter and attach it to the RecyclerView
        mVideoAdapter = new VideoTrailerAdapter(this, this);
        mVideoRecyclerView.setAdapter(mVideoAdapter);
    }

    public class LoadVideoTrailersTask extends AsyncTask<Integer, Void, ArrayList<VideoTrailer>> {

        @Override
        protected ArrayList<VideoTrailer> doInBackground(Integer... params) {
            int movieId = params[0];

            ArrayList<VideoTrailer> videoTrailerList = null;

            if (isNetworkAvailable(mContext)) {
                videoTrailerList = MovieUtils.getVideoTrailerList(mContext, movieId);
            } else {
                videoTrailerList = MovieUtils.getVideoTrailerListFromCursor(mContext, movieId);
            }

            return videoTrailerList;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoTrailer> videoTrailerList) {

            // Save to store on rotation
            mVideoTrailerList = videoTrailerList;

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (videoTrailerList != null) {

                videoTrailerIsAvailableToShare = false;

                for (VideoTrailer videoTrailer : videoTrailerList) {

                    if (!videoTrailerIsAvailableToShare && videoTrailer.getType().contentEquals(TRAILER)) {

                        // Store the first VideoTrailer that "type" = "Trailer"
                        // Then flag as shareable to use in shareMovie()
                        mVideoTrailer = videoTrailer;

                        videoTrailerIsAvailableToShare = true;
                    }

                }

                mVideoAdapter.loadVideoTrailers(videoTrailerList);
                mVideoAdapter.notifyDataSetChanged();

            }

        }

    }

    private void setupUserReviews() {
        RecyclerView mReviewRecyclerView = findViewById(R.id.reviews_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mReviewRecyclerView.setLayoutManager(layoutManager);

        // allows for optimizations if all items are of the same size:
        mReviewRecyclerView.setHasFixedSize(true);

        // Initialize the video adapter and attach it to the RecyclerView
        mReviewAdapter = new UserReviewAdapter(this);
        mReviewRecyclerView.setAdapter(mReviewAdapter);
    }

    public class LoadUserReviewsTask extends AsyncTask<Integer, Void, ArrayList<UserReview>> {

        @Override
        protected ArrayList<UserReview> doInBackground(Integer... params) {
            int movieId = params[0];

            ArrayList<UserReview> userReviewList = null;

            if (isNetworkAvailable(mContext)) {
                // Network is Available
                userReviewList = MovieUtils.getUserReviewList(mContext, movieId);
            } else {
                // Network is NOT Available
                userReviewList = MovieUtils.getUserReviewListFromCursor(mContext, movieId);
            }

            return userReviewList;
        }

        @Override
        protected void onPostExecute(ArrayList<UserReview> userReviewList) {

            // Save to store on rotation
            mUserReviewList = userReviewList;

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (userReviewList != null) {

                mReviewAdapter.loadUserReviews(userReviewList);
                mReviewAdapter.notifyDataSetChanged();
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
        shareIntent.setType(TYPE_TEXT_PLAIN);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT + mMovie.getTitle());

        if (videoTrailerIsAvailableToShare) {
            Uri videoUrl = Uri.parse(SHARE_YOUTUBE_URL).buildUpon().appendQueryParameter(PARAMETER_V, mVideoTrailer.getKey()).build();
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl.toString() + NEW_LINE + mMovie.getOverview());
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getOverview());
        }

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.action_share)));
    }


    // Toggles isFavorite value associated with the heart shaped favorite icon in the Action Bar.
    // Updates database record with the Movie Id passed in.
    private void toggleFavorite(int movieId) {

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

        if (rowsUpdated > 0) {

            // update the action bar menu item by changing the button visibility for both icons
            favoriteSelected.setVisible(isFavorite);
            favoriteNotSelected.setVisible(!isFavorite);
        }

    }

}

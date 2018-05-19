package com.himebaugh.popularmovies;

// Import statements for Data Binding

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.himebaugh.popularmovies.adapter.MovieAdapter;
import com.himebaugh.popularmovies.adapter.UserReviewAdapter;
import com.himebaugh.popularmovies.adapter.VideoTrailerAdapter;
import com.himebaugh.popularmovies.databinding.ActivityDetailBinding;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.UserReview;
import com.himebaugh.popularmovies.model.VideoTrailer;
import com.himebaugh.popularmovies.utils.MovieUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;

public class DetailActivity extends AppCompatActivity implements VideoTrailerAdapter.VideoTrailerAdapterOnClickHandler {

    private static final String TAG = DetailActivity.class.getName();
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE_SMALL = "w185"; //w500
    private static final String SIZE_LARGE = "w500";  //w780

    // TODO: FAVORITES,
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

    private Movie mMovie;

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

            // TODO: placeholder() and error() accept a drawable resource ID. CREATE a default image.
            String imagePosterURL = BASE_URL + SIZE_SMALL + mMovie.getPosterPath();
            Picasso.get().load(imagePosterURL).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_foreground).into(mDetailBinding.moviePosterIv);

            if (!videoTrailersHaveBeenLoaded) {
                loadVideoTrailers();
            }

            if (!userReviewsHaveBeenLoaded) {
                loadUserReviews();
            }

            Log.i(TAG, "onCreate: ID=" + mMovie.getId());
        }

    }

    @Override
    public void onClick(VideoTrailer videoTrailer) {

        Log.i(TAG, "onClick: " + videoTrailer.getName() + videoTrailer.getKey());

        // Launch YouTube
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoTrailer.getKey() ));

        startActivity(intent);

    }

    private void loadVideoTrailers() {

        mVideoRecyclerView = findViewById(R.id.trailers_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // May save position and reset upon orientation change???
        // layoutManager.scrollToPosition(0);

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

                while (listIterator.hasNext()) {
                    VideoTrailer videoTrailer = listIterator.next();

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

        // To fetch reviews you will want to make a request to the reviews endpoint
        // http://api.themoviedb.org/3/movie/{id}/reviews?api_key=<YOUR-API-KEY>

        URL queryUrl = MovieUtils.buildEndpointUrl(this, String.valueOf(mMovie.getId()), MovieUtils.REVIEWS_ENDPOINT);

        // new LoadUserReviewsTask().execute(queryUrl);

    }

    public class LoadUserReviewsTask extends AsyncTask<URL, Void, List<UserReview>> {

        @Override
        protected List<UserReview> doInBackground(URL... params) {
            URL url = params[0];

            List<UserReview> userReviewList = null;

            if (isNetworkAvailable(mContext)) {
                Log.i(TAG, "Network is Available");
                try {
                    userReviewList = MovieUtils.getUserReviewList(mContext, url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "Network is NOT Available");
                // TODO: userReviewList = MovieUtils.getUserReviewListFromCursor(mContext, url);
            }

            return userReviewList;
        }

        @Override
        protected void onPostExecute(List<UserReview> userReviewList) {

            // TO PREVENT ERROR WHEN NO INTERNET...
            if (userReviewList == null) {
                Toast.makeText(getApplicationContext(), R.string.msg_internet_connection, Toast.LENGTH_LONG).show();

            } else {

                Log.i(TAG, "videoTrailerList.toString(): " + userReviewList.toString());

                Log.i(TAG, "movieList.size()" + userReviewList.size());

                final ListIterator<UserReview> listIterator = userReviewList.listIterator();

                while (listIterator.hasNext()) {
                    UserReview userReview = listIterator.next();

                    Log.i(TAG, "listIterator: " + userReview.getId() + " " + userReview.getAuthor() );
                }

                mReviewAdapter.loadUserReviews(userReviewList);
                mReviewAdapter.notifyDataSetChanged();

                if (userReviewList.size() > 0) {
                    userReviewsHaveBeenLoaded = true;
                }

                Log.i(TAG, "moviesHaveBeenLoaded: " + userReviewsHaveBeenLoaded);
            }

        }

    }

}

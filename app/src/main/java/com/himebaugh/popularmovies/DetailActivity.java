package com.himebaugh.popularmovies;

// Import statements for Data Binding

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.himebaugh.popularmovies.databinding.ActivityDetailBinding;
import com.himebaugh.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getName();
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE = "w500";

    private Movie movie;

    // Create Data Binding instance called mDetailBinding of type ActivityMainBinding
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Implement Up Navigation - displays the back arrow in front of App icon in the Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiate mDetailBinding using DataBindingUtil
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {

            Bundle bundle = intentThatStartedThisActivity.getBundleExtra("bundle");

            movie = bundle.getParcelable("movie");

            getSupportActionBar().setTitle("Movie Details");
            getSupportActionBar().setSubtitle(movie.getTitle());


            // Bind movie data to the views
            mDetailBinding.titleTv.setText(movie.getTitle());
            mDetailBinding.releaseDateTv.setText(movie.getReleaseDate());
            mDetailBinding.voteAverageTv.setText(String.valueOf(movie.getVoteAverage()));
            mDetailBinding.tvPlotSynopsis.setText(movie.getOverview());

            String imageURL = BASE_URL + SIZE + movie.getPosterPath();
            Picasso.get().load(imageURL).into(mDetailBinding.moviePosterIv);
        }

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here.
//        // The action bar will automatically handle clicks on the Home/Up button,
//        // so long as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        // This should also handle clicks on the Home/Up button
//        if (id == android.R.id.home) {
//            onBackPressed();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}

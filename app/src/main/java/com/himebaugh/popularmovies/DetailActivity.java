package com.himebaugh.popularmovies;

// Import statements for Data Binding

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.himebaugh.popularmovies.databinding.ActivityDetailBinding;
import com.himebaugh.popularmovies.model.Movie;

public class DetailActivity extends AppCompatActivity {

    private final static String TAG = DetailActivity.class.getName();

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

            // Bind movie data to the views
            mDetailBinding.tvMovieName.setText(movie.getTitle());

        }

    }
}

package com.himebaugh.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            // Implement Up Navigation - displays the back arrow in front of App icon in the Action Bar
            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle(getString(R.string.title_activity_settings));
        }

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }

}
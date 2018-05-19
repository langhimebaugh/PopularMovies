package com.himebaugh.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    // Name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.himebaugh.popularmovies";

    // Base URI to contact the content provider for PopularMovies
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Paths for accessing data in this contract
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_USER_REVIEWS = "user_reviews";
    public static final String PATH_VIDEO_TRAILERS = "video_trailers";

    // Inner class that defines the table contents of the movie table
    public static final class MovieEntry implements BaseColumns {

        // The base CONTENT_URI used to query the Movie table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Table & column names
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        //public static final String COLUMN_MOVIE_ID = "movie_id";          // External not SQLite ID
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_FAVORITE = "favorite";            // Added field to save favorite
        public static final String COLUMN_CATEGORY = "category";            // Added field to save category (popular or top-rated)
    }

    // Inner class that defines the table contents of the user review table
    public static final class UserReviewEntry implements BaseColumns {

        // The base CONTENT_URI used to query the Movie table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER_REVIEWS).build();

        // Table & column names
        public static final String TABLE_NAME = "user_review";

        public static final String COLUMN_MOVIE_ID = "movie_id";                    // Foreign Key
        public static final String COLUMN_USER_REVIEW_ID = "user_review_id";        // External not SQLite ID
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
    }

    // Inner class that defines the table contents of the video trailer table
    public static final class VideoTrailerEntry implements BaseColumns {

        // The base CONTENT_URI used to query the Movie table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO_TRAILERS).build();

        // Table & column names
        public static final String TABLE_NAME = "video_trailer";

        public static final String COLUMN_MOVIE_ID = "movie_id";                    // Foreign Key
        public static final String COLUMN_VIDEO_TRAILER_ID = "video_trailer_id";    // External not SQLite ID
        public static final String COLUMN_ISO_639_1 = "iso_639_1";
        public static final String COLUMN_ISO_3166_1 = "iso_3166_1";
        public static final String COLUMN_KEY = "keyid";                            // key reserved word?
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";
    }

}

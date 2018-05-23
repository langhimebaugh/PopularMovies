package com.himebaugh.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.data.MovieContract.UserReviewEntry;
import com.himebaugh.popularmovies.data.MovieContract.VideoTrailerEntry;

/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String TAG = MovieDbHelper.class.getSimpleName();

    // Database names should end with the .db extension.
    public static final String DATABASE_NAME = "movie.db";

    // If you change the database schema, you must increment the database version
    // or the onUpgrade method will not be called.
    private static final int DATABASE_VERSION = 1;

    // Database Schema

    // I wanted to have all data available offline, but this adds many more API calls & slows everything down a bit...
    // located MovieUtils.saveMovieListToCursor() all will be saved for offline use... but I've commented this out.
    //
    // Design Considerations:
    //      I use 3 tables: Movie, UserReviews, VideoTrailer
    //      Primary Key is MovieEntry._ID + " INTEGER PRIMARY KEY, " +
    //      Primary Key is not using AUTOINCREMENT
    //      It may be possible that POPULAR_MOVIES & TOPRATED_MOVIES include overlapping ID's (If one movie is in both categories)
    //      I will need to ignore one of the records with overlapping ID's when inserting.
    //      Need COLUMN_CATEGORY to track categories POPULAR_MOVIES & TOPRATED_MOVIES
    //      Need COLUMN_FAVORITE which I will toggle on/off
    //      Store the original id from The Movie DB API as _ID without AUTOINCREMENT
    //      Upon every network call, I delete all records except where Favorite
    //      New records are added with ON CONFLICT IGNORE
    //      I attempted to use FOREIGN KEY constraints but the logic of my inserts are not set up where I can insert into all three tables
    //         at the same time within a transaction.  I still use COLUMN_MOVIE_ID as a foreign key, it's just not locked down like it should be.
    //      I setup indexes for speedier reads
    //      I setup some logic to save the favorites upon a database upgrade.


    // This String contains an SQL statement that will create a table to hold movie data
    private static final String CREATE_MOVIE_TABLE = "CREATE TABLE IF NOT EXISTS " + MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT IGNORE, " +
            MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
            MovieEntry.COLUMN_ADULT + " TEXT, " +
            MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
            MovieEntry.COLUMN_GENRE_IDS + " BLOB, " +
            MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
            MovieEntry.COLUMN_TITLE + " TEXT, " +
            MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
            MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
            MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
            MovieEntry.COLUMN_VIDEO + " TEXT, " +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
            MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT '0', " +
            MovieEntry.COLUMN_CATEGORY + " INTEGER NOT NULL DEFAULT '0'  " +
            "); ";

    // ON CONFLICT IGNORE - Insert the movies into the database only if the movie is not currently in the database

    // This String contains an SQL statement that will create a table to hold user review data
    private static final String CREATE_USER_REVIEW_TABLE = "CREATE TABLE IF NOT EXISTS " + UserReviewEntry.TABLE_NAME + " (" +
            UserReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UserReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
            UserReviewEntry.COLUMN_USER_REVIEW_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, " +
            UserReviewEntry.COLUMN_AUTHOR + " TEXT, " +
            UserReviewEntry.COLUMN_CONTENT + " TEXT, " +
            UserReviewEntry.COLUMN_URL + " TEXT " +
            //        "FOREIGN KEY(" + UserReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "(_id)" + " DEFERRABLE INITIALLY DEFERRED" +
            "); ";

    // This String contains an SQL statement that will create a table to hold video trailer data
    private static final String CREATE_VIDEO_TRAILER_TABLE = "CREATE TABLE IF NOT EXISTS " + VideoTrailerEntry.TABLE_NAME + " (" +
            VideoTrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            VideoTrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
            VideoTrailerEntry.COLUMN_VIDEO_TRAILER_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, " +
            VideoTrailerEntry.COLUMN_ISO_639_1 + " TEXT, " +
            VideoTrailerEntry.COLUMN_ISO_3166_1 + " TEXT, " +
            VideoTrailerEntry.COLUMN_KEY + " TEXT, " +
            VideoTrailerEntry.COLUMN_NAME + " TEXT, " +
            VideoTrailerEntry.COLUMN_SITE + " TEXT, " +
            VideoTrailerEntry.COLUMN_SIZE + " INTEGER NOT NULL, " +
            VideoTrailerEntry.COLUMN_TYPE + " TEXT " +
            // UidStore.COLUMN_UID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE," +
            //       "UNIQUE (" + VideoTrailerEntry.COLUMN_VIDEO_TRAILER_ID + ") ON CONFLICT IGNORE " +
            //  "FOREIGN KEY(" + VideoTrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "(_id)" + " DEFERRABLE INITIALLY DEFERRED" +
            "); ";

    private static final String CREATE_INDEX1 = "CREATE INDEX moviefavorite_idx ON " + MovieEntry.TABLE_NAME + "(" + MovieEntry.COLUMN_FAVORITE + ");";
    private static final String CREATE_INDEX2 = "CREATE INDEX moviecategory_idx ON " + MovieEntry.TABLE_NAME + "(" + MovieEntry.COLUMN_CATEGORY + ");";
    private static final String CREATE_INDEX3 = "CREATE INDEX reviewmovieid_idx ON " + UserReviewEntry.TABLE_NAME + "(" + UserReviewEntry.COLUMN_MOVIE_ID + ");";
    private static final String CREATE_INDEX4 = "CREATE INDEX trailermovieid_idx ON " + VideoTrailerEntry.TABLE_NAME + "(" + VideoTrailerEntry.COLUMN_MOVIE_ID + ");";

    private static final String DROP_INDEX1 = "DROP INDEX IF EXISTS moviefavorite_idx ;";
    private static final String DROP_INDEX2 = "DROP INDEX IF EXISTS moviecategory_idx ;";
    private static final String DROP_INDEX3 = "DROP INDEX IF EXISTS reviewmovieid_idx ;";
    private static final String DROP_INDEX4 = "DROP INDEX IF EXISTS trailermovieid_idx ;";

    private static final String SAVE_FAVORITE_1 = "CREATE TABLE 'movie_copy' AS SELECT * FROM 'movie' WHERE favorite = 1 ;";
    private static final String SAVE_FAVORITE_2 = "INSERT INTO 'movie' SELECT * FROM 'movie_copy' ;";


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public MovieDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Execute the SQL with the execSQL method of our SQLite database object
        db.execSQL(CREATE_MOVIE_TABLE);
        db.execSQL(CREATE_USER_REVIEW_TABLE);
        db.execSQL(CREATE_VIDEO_TRAILER_TABLE);

        db.execSQL(DROP_INDEX1);
        db.execSQL(DROP_INDEX2);
        db.execSQL(DROP_INDEX3);
        db.execSQL(DROP_INDEX4);

        db.execSQL(CREATE_INDEX1);
        db.execSQL(CREATE_INDEX2);
        db.execSQL(CREATE_INDEX3);
        db.execSQL(CREATE_INDEX4);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Runs when changing the database structure...

        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", and destroy all data");

        // To AVOID losing favorites...
        if (newVersion > 1) {
            // SAVE Favorites upon upgrade
            // Copies movies to movies_copy
            db.execSQL("BEGIN");
            db.beginTransaction();
            db.execSQL(SAVE_FAVORITE_1);
            db.endTransaction();
            db.execSQL("COMMIT");
        }

        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VideoTrailerEntry.TABLE_NAME);
        onCreate(db);

        // To AVOID losing favorites...
        if (newVersion > 1) {
            // RESTORE Favorites upon upgrade
            // Copies movies_copy to movies
            db.execSQL(SAVE_FAVORITE_2);
        }

    }

// Discussion May 5th on Google+

// Kurt Schneider
// I have a few general design questions regarding extending the ContentProvider to load movie data when offline for Popular Movies, Stage 2.
// Currently whenever I make a network call for popular or top rated movies, I insert the movies into the database only if the movie was not
// currently in the database (using ON_CONFLICT_IGNORE on the movie id). In a short time frame, the database would hold 40 movies (page 1 of
// popular and page 1 of top rated). However, over time, for example, when popular movies change, this would continue to add more movies to
// the database. Would a better design be to delete all the movies before inserting them? Yes, I will be deleting the movie and then inserting
// the same movie, but at least this way the database would always have 40 movies.
//
// Some general concerns: every time I click "popular" or "top rated" in the menu, a network call is made to populate the views and every time
// this happens the movies are inserted into the database (if they currently do not exist). Is this overkill? It's very unlikely that popular
// movies will change that quickly, so inserting to the database this often appears unnecessary. Maybe use a JobDispatcher to do this every so
// often? Any thoughts are appreciated. Thanks!
//
// Langdon Himebaugh
// I've been thinking about this too.  First according to the API rules we are not supposed to cache the data for very long.  Second according
// to rubric I believe we read from content provider only when internet connection is down.  So as inefficient as it is to delete data and
// re-save it for every network call, I'm thinking that is what I will do.  Otherwise you could update the data every 12 hours, but that seems
// like a waste of bandwidth if you only show data from content provider when internet is down.
//
// Langdon Himebaugh
// Other problem with not delete, is what happens if their data changed and image or content changed or was deleted.  Then you would be
// displaying incorrect movie data or link to broken image.  Not very likely but possible.
//
// Kurt Schneider
// Thanks. Good points. I'm going with delete and re-save for every network call.
//
// Kurt Schneider
// Another design question for you. I currently have FAVORITE_COLUMN in my table. This flag keeps track of movies added to favorites.
// The problem with this is that if I delete and re-save, I'll lose track of favorite movies. To solve this, I would need to query the content
// provider every network call for every movie to know if it was a favorite before deleting it and then re-save with the correct flag.
// Do you have an alternative approach?
//
// Langdon Himebaugh
// Maybe don't delete if favorite?  Or how about a separate table of movie ids that are favorite.  And then reset your flag upon
// adding to other table again.... If the ids match.

}

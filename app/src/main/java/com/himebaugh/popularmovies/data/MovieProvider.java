package com.himebaugh.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.data.MovieContract.UserReviewEntry;
import com.himebaugh.popularmovies.data.MovieContract.VideoTrailerEntry;

public class MovieProvider extends ContentProvider {

    // NOTE TO SELF:
    // USE ContentResolver  NOT ContentProvider
    // Don't close the database ... db.close();

    private static final String TAG = MovieProvider.class.getSimpleName();

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int USERREVIEWS = 200;
    public static final int USERREVIEW_WITH_ID = 201;
    public static final int VIDEOTRAILERS = 300;
    public static final int VIDEOTRAILER_WITH_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Initialize a new matcher object without any matches,
     * then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the movie directory and a single movie by ID.
         */
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_USER_REVIEWS, USERREVIEWS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_USER_REVIEWS + "/#", USERREVIEW_WITH_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEO_TRAILERS, VIDEOTRAILERS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEO_TRAILERS + "/#", VIDEOTRAILER_WITH_ID);

        return uriMatcher;
    }

    private MovieDbHelper mMovieDbHelper;


    @Override
    public boolean onCreate() {

        Context context = getContext();
        mMovieDbHelper = new MovieDbHelper(context);
        return true;
    }

    // Implement query to handle requests for data by URI
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;
        String id;

        switch (match) {

            // Query for the movies directory
            case MOVIES:
                returnCursor = db.query(MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for a single movie
            case MOVIE_WITH_ID:
                // Get the movie ID from the URI path
                id = uri.getPathSegments().get(1);

                // Use selections/selectionArgs to filter for this ID
                returnCursor = db.query(MovieEntry.TABLE_NAME,
                        projection,
                        "_id = ?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;

            // Query for the userreviews directory
            case USERREVIEWS:
                returnCursor = db.query(UserReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for a single userreview
            case USERREVIEW_WITH_ID:
                // Get the station ID from the URI path
                id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                returnCursor = db.query(UserReviewEntry.TABLE_NAME,
                        projection,
                        "_id = ?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;

            // Query for the videotrailer directory
            case VIDEOTRAILERS:
                returnCursor = db.query(VideoTrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for a single videotrailer
            case VIDEOTRAILER_WITH_ID:
                // Get the station ID from the URI path
                id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                returnCursor = db.query(VideoTrailerEntry.TABLE_NAME,
                        projection,
                        "_id = ?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;

            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                // directory
                return "vnd.android.cursor.dir" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_MOVIES;
            case MOVIE_WITH_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_MOVIES;
            case USERREVIEWS:
                // directory
                return "vnd.android.cursor.dir" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_USER_REVIEWS;
            case USERREVIEW_WITH_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_USER_REVIEWS;
            case VIDEOTRAILERS:
                // directory
                return "vnd.android.cursor.dir" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_VIDEO_TRAILERS;
            case VIDEOTRAILER_WITH_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_VIDEO_TRAILERS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    // Implement insert to handle requests to insert a single new row of data
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // Get access to the movie database (to write new data to)
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned
        long id;

        switch (match) {

            case MOVIES:
                // Insert new values into the database
                // Inserting values into movie table
                id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case USERREVIEWS:
                // Insert new values into the database
                // Inserting values into user_review table
                id = db.insert(UserReviewEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(UserReviewEntry.CONTENT_URI, id);
                } else {
                    Log.i(TAG, "LANG User values=" + values.toString());
                    returnUri = null;
                    // UNIQUE ON CONFLICT IGNORE will trigger exception below if can't insert...
                    //throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case VIDEOTRAILERS:
                // Insert new values into the database
                // Inserting values into video_trailer table
                id = db.insert(VideoTrailerEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(VideoTrailerEntry.CONTENT_URI, id);
                } else {
                    Log.i(TAG, "LANG Video values=" + values.toString());
                    returnUri = null;
                    // UNIQUE ON CONFLICT IGNORE will trigger exception below if can't insert...
                    //throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    // Implement delete to delete a single row of data
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted records
        int recordsDeleted; // starts as 0
        String id;

        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case MOVIE_WITH_ID:
                // Get the movie ID from the URI path
                id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                recordsDeleted = db.delete(MovieEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            case USERREVIEW_WITH_ID:
                // Get the movie ID from the URI path
                id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                recordsDeleted = db.delete(UserReviewEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            case VIDEOTRAILER_WITH_ID:
                // Get the movie ID from the URI path
                id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                recordsDeleted = db.delete(VideoTrailerEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;

            case MOVIES:
                // USED TO DELETE ALL BUT FAVORITE RECORDS
                // Used in MovieUtils.clearAllButFavorites()
                recordsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case USERREVIEWS:
                // USED TO DELETE ALL BUT FAVORITE RECORDS
                // Used in MovieUtils.clearAllButFavorites() but commented out
                recordsDeleted = db.delete(UserReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case VIDEOTRAILERS:
                // USED TO DELETE ALL BUT FAVORITE RECORDS
                // Used in MovieUtils.clearAllButFavorites() but commented out
                recordsDeleted = db.delete(VideoTrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (recordsDeleted != 0) {
            // A record was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of records deleted
        return recordsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        // match code
        int match = sUriMatcher.match(uri);
        //Keep track of if an update occurs
        int recordsUpdated;
        String id;

        switch (match) {
            case MOVIE_WITH_ID:
                //update a single movie by getting the id
                id = uri.getPathSegments().get(1);
                //using selections
                recordsUpdated = db.update(MovieEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            case USERREVIEW_WITH_ID:
                //update a single movie by getting the id
                id = uri.getPathSegments().get(1);
                //using selections
                recordsUpdated = db.update(UserReviewEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            case VIDEOTRAILER_WITH_ID:
                //update a single movie by getting the id
                id = uri.getPathSegments().get(1);
                //using selections
                recordsUpdated = db.update(VideoTrailerEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (recordsUpdated != 0) {
            //set notifications if a record was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of records updated
        return recordsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        Log.i(TAG, "bulkInsert: ");

        // Get access to the database and write URI matching code to recognize a single item
        SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case MOVIES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        Log.i(TAG, "bulkInsert: FOR");

                        long id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                // This seems to properly insert Video Trailers by default!
                // Maybe Movies doesn't require the above code!
                Log.i(TAG, "bulkInsert: DEFAULT!!!");
                return super.bulkInsert(uri, values);
        }

    }

}

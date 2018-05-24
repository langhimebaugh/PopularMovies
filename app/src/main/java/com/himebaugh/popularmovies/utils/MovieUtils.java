package com.himebaugh.popularmovies.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.himebaugh.popularmovies.R;
import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.data.MovieContract.UserReviewEntry;
import com.himebaugh.popularmovies.data.MovieContract.VideoTrailerEntry;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.UserReview;
import com.himebaugh.popularmovies.model.VideoTrailer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieUtils {

    // NOTE TO SELF: Avoid Null Error...
    // USE ContentResolver  NOT ContentProvider
    // So... context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    // Not.. movieProvider.query(uri, projection, selection, selectionArgs, sortOrder);

    private static final String TAG = MovieUtils.class.getSimpleName();

    public static final int FILTER_POPULAR = 0;
    public static final int FILTER_TOPRATED = 1;
    public static final int FILTER_FAVORITE = 2;

    private final static String POPULAR_MOVIES = "https://api.themoviedb.org/3/movie/popular";
    private final static String TOPRATED_MOVIES = "https://api.themoviedb.org/3/movie/top_rated";
    private final static String VIDEOS_ENDPOINT = "/videos";
    private final static String REVIEWS_ENDPOINT = "/reviews";
    private final static String PARAM_APIKEY = "api_key";
    private final static String PARAM_PAGE = "page";
    private final static String page = "1";
    private final static String PARAM_LANGUAGE = "language";
    private final static String language = "en-US";


    // http://api.themoviedb.org/3/movie/popular?api_key=<YOUR-API-KEY>

    // To fetch trailers you will want to make a request to the videos endpoint
    // http://api.themoviedb.org/3/movie/{id}/videos?api_key=<YOUR-API-KEY>

    // To fetch reviews you will want to make a request to the reviews endpoint
    // http://api.themoviedb.org/3/movie/{id}/reviews?api_key=<YOUR-API-KEY>

    // You should use an Intent to open a youtube link in either the native app or a web browser of choice.

    /**
     * Builds the URL used to query GitHub.
     *
     * @param movieID The movie that the detail will be queried for.
     * @return The URL to use to query the GitHub.
     */
    private static URL buildEndpointUrl(Context context, String movieID, String endPoint) {

        // http://api.themoviedb.org/3/movie/278/videos?api_key=<YOUR-API-KEY>

        String baseUrl = "https://api.themoviedb.org/3/movie/";

        String movieDatabaseUrl = baseUrl + movieID + endPoint;

        // Reading API KEY from text file on gitignore list
        String apiKey = readRawTextFile(context, R.raw.api_key);

        Uri builtUri = Uri.parse(movieDatabaseUrl).buildUpon()
                .appendQueryParameter(PARAM_APIKEY, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to query Videos Endpoint.
     *
     * @param movieDatabaseUrl The keyword that will be queried for.
     * @return The URL to use to query the Videos Endpoint.
     */
    private static URL buildUrl(Context context, String movieDatabaseUrl, String page) {

        // Reading API KEY from text file on gitignore list
        String apiKey = readRawTextFile(context, R.raw.api_key);

        Uri builtUri = Uri.parse(movieDatabaseUrl).buildUpon()
                .appendQueryParameter(PARAM_PAGE, page)
                .appendQueryParameter(PARAM_LANGUAGE, language)
                .appendQueryParameter(PARAM_APIKEY, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private static String readRawTextFile(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);
        } catch (IOException e) {
            return null;
        }
        return stringBuilder.toString();
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     */
    private static String getJsonFromHttpUrl(URL url) {

        String jsonString = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                jsonString = scanner.next();
            }

            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    /**
     * Alternatively this method returns the entire result using OkHttpClient.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     */
    public static String getJsonFromOkHttpClient(URL url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        String jsonString = null;

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                jsonString = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    // ==========MovieList=Start====================================================

    public static ArrayList<Movie> getMovieList(Context context, int filter) throws IOException {

        ArrayList<Movie> returnMovieList;

        URL queryUrl;

        // **********************************************
        // filter is passed in when clicking options menu in MainActivity
        //
        // filter may contain either of:
        //  1) MovieUtils.FILTER_POPULAR
        //
        //      Build a URL using MovieUtils.POPULAR_MOVIES
        //      Attempt to get MovieList through internet via API through getJsonFromHttpUrl(url)
        //      IF SUCCESSFUL, process & saveMovieListToCursor() [REMOVE OLD MOVIES & REFRESH WITH NEW ONES]
        //      IF NOT SUCCESSFUL assume the internet is down and get backup data from the MovieProvider using getMovieListFromCursor()
        //
        //  2) MovieUtils.FILTER_TOPRATED
        //
        //      Build a URL using MovieUtils.TOPRATED_MOVIES
        //      Attempt to get MovieList through internet via API through getJsonFromOkHttpClient(url)
        //      IF SUCCESSFUL, process & saveMovieListToCursor() [REMOVE OLD MOVIES & REFRESH WITH NEW ONES]
        //      IF NOT SUCCESSFUL assume the internet is down and get backup data from the MovieProvider using getMovieListFromCursor()
        //
        //  3) MovieUtils.FILTER_FAVORITE
        //
        //      Get MovieList from database via the MovieProvider using getMovieListFromCursor()
        //
        // **********************************************

        switch (filter) {
            case MovieUtils.FILTER_FAVORITE:

                // ***********************************************************
                // Get MovieList from database via the MovieProvider
                // ***********************************************************
                returnMovieList = getMovieListFromCursor(context, filter);

                break;
            case MovieUtils.FILTER_POPULAR:

                queryUrl = MovieUtils.buildUrl(context, MovieUtils.POPULAR_MOVIES, page);

                // Can use either one... getJsonFromHttpUrl() -OR- getJsonFromOkHttpClient()
                String popularMoviesJsonResults = getJsonFromHttpUrl(queryUrl);
                // String popularMoviesJsonResults = getJsonFromOkHttpClient(queryUrl);  // This Works Fine

                if (popularMoviesJsonResults == null) {

                    // ***********************************************************
                    // INTERNET IS DOWN, so get MovieList from database
                    // ***********************************************************
                    returnMovieList = getMovieListFromCursor(context, filter);

                } else {

                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(popularMoviesJsonResults, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    String movieListJsonResults = gson.toJson(map.get("results"));

                    Type listType = new TypeToken<ArrayList<Movie>>() {
                    }.getType();

                    // Now Gson converts the json to a list of movies object based on the Movie class
                    returnMovieList = gson.fromJson(movieListJsonResults, listType);

                    // ***********************************************************
                    // The latest MovieList results are saved to the database
                    // Also, MovieUtils.FILTER_POPULAR is saved to COLUMN_CATEGORY
                    // ***********************************************************
                    saveMovieListToCursor(context, returnMovieList, MovieUtils.FILTER_POPULAR);
                }

                break;
            case MovieUtils.FILTER_TOPRATED:

                queryUrl = MovieUtils.buildUrl(context, MovieUtils.TOPRATED_MOVIES, page);

                // Can use either one... getJsonFromHttpUrl() -OR- getJsonFromOkHttpClient()
                // String topratedMoviesJsonResults = getJsonFromHttpUrl(queryUrl);
                String topratedMoviesJsonResults = getJsonFromOkHttpClient(queryUrl);  // This Works Fine

                if (topratedMoviesJsonResults == null) {

                    // ***********************************************************
                    // INTERNET IS DOWN, so get MovieList from database
                    // ***********************************************************
                    returnMovieList = getMovieListFromCursor(context, filter);

                } else {

                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(topratedMoviesJsonResults, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    String movieListJsonResults = gson.toJson(map.get("results"));

                    Type listType = new TypeToken<ArrayList<Movie>>() {
                    }.getType();

                    // Now Gson converts the json to a list of movies object based on the Movie class
                    returnMovieList = gson.fromJson(movieListJsonResults, listType);

                    // ***********************************************************
                    // The latest MovieList results are saved to the database
                    // Also, MovieUtils.FILTER_TOPRATED is saved to COLUMN_CATEGORY
                    // ***********************************************************
                    saveMovieListToCursor(context, returnMovieList, MovieUtils.FILTER_TOPRATED);
                }

                break;
            default:
                returnMovieList = null;

        }

        return returnMovieList;

    }

    public static ArrayList<Movie> getMovieListFromCursor(Context context, int filter) {

        ArrayList<Movie> returnMovieList = new ArrayList<>();

        Uri uri = MovieEntry.CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        // Set selection
        switch (filter) {
            case MovieUtils.FILTER_POPULAR:
                selection = MovieEntry.COLUMN_CATEGORY + " = " + MovieUtils.FILTER_POPULAR;
                break;

            case MovieUtils.FILTER_TOPRATED:
                selection = MovieEntry.COLUMN_CATEGORY + " = " + MovieUtils.FILTER_TOPRATED;
                break;

            case MovieUtils.FILTER_FAVORITE:
                selection = MovieEntry.COLUMN_FAVORITE + " = 1";
                break;

            default:
                selection = MovieEntry.COLUMN_CATEGORY + " = " + MovieUtils.FILTER_POPULAR;
        }

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        // iterate over cursor to load into List
        while (cursor.moveToNext()) {

            String posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
            Boolean adult = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) > 0;
            String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
            String releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));

            // String temp = String.valueOf(cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_GENRE_IDS)));
            ArrayList<Integer> genreIds = null;  // Can't figure out how to convert, but don't need.

            int id = cursor.getInt(cursor.getColumnIndex(MovieEntry._ID));
            String originalTitle = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
            String originalLanguage = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE));
            String title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
            String backdropPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH));
            double popularity = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY));
            int voteCount = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT));
            Boolean video = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VIDEO)) > 0;
            double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));

            Movie movie = new Movie(
                    posterPath,
                    adult,
                    overview,
                    releaseDate,
                    genreIds,
                    id,
                    originalTitle,
                    originalLanguage,
                    title,
                    backdropPath,
                    popularity,
                    voteCount,
                    video,
                    voteAverage
            );
            returnMovieList.add(movie);
        }

        cursor.close();

        return returnMovieList;
    }

    private static void saveMovieListToCursor(Context context, ArrayList<Movie> movieList, int filter) {

        // CLEAR DATABASE OF ALL RECORDS from UserReviews & VideoTrailers & ALL BUT FAVORITES from Movies
        // Very inefficient, but this way it is always up to date with the latest data...
        if (movieList.size() > 10) {
            // Only clear the database if I have enough movies to add...
            clearAllButFavorites(context, filter);
        }

        Uri uri = MovieEntry.CONTENT_URI;

        ContentValues[] bulkMovieValues = new ContentValues[movieList.size()];

        for (int i = 0; i < movieList.size(); i++) {

            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movieList.get(i).getPosterPath());
            movieValues.put(MovieEntry.COLUMN_ADULT, movieList.get(i).getAdult());
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, movieList.get(i).getOverview());
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movieList.get(i).getReleaseDate());
            // movieValues.put(MovieEntry.COLUMN_GENRE_IDS, movieList.get(i).getGenreIds());
            movieValues.put(MovieEntry._ID, movieList.get(i).getId());
            movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movieList.get(i).getOriginalTitle());
            movieValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movieList.get(i).getOriginalLanguage());
            movieValues.put(MovieEntry.COLUMN_TITLE, movieList.get(i).getTitle());
            movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, movieList.get(i).getBackdropPath());
            movieValues.put(MovieEntry.COLUMN_POPULARITY, movieList.get(i).getPopularity());
            movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, movieList.get(i).getVoteCount());
            movieValues.put(MovieEntry.COLUMN_VIDEO, movieList.get(i).getVideo());
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movieList.get(i).getVoteAverage());

            movieValues.put(MovieEntry.COLUMN_CATEGORY, filter);

            bulkMovieValues[i] = movieValues;

            // ========== Cache UserReviews & VideoTrailer for OFF-LINE use =========================================
            // This adds many more API calls & slows everything down a bit... but all will be saved for online use...
            // It works, but I'm being nice to the API and commenting this out.
//            try {
//                getVideoTrailerList(context, movieList.get(i).getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                getUserReviewList(context, movieList.get(i).getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }

        int recordsInserted = context.getContentResolver().bulkInsert(uri, bulkMovieValues);
    }

    // ==========VideoTrailerList=Start====================================================

    public static ArrayList<VideoTrailer> getVideoTrailerList(Context context, int movieId) {

        ArrayList<VideoTrailer> returnVideoTrailerList;

        // To fetch trailers you will want to make a request to the videos endpoint
        // http://api.themoviedb.org/3/movie/{id}/videos?api_key=<YOUR-API-KEY>
        URL queryUrl = MovieUtils.buildEndpointUrl(context, String.valueOf(movieId), MovieUtils.VIDEOS_ENDPOINT);

        String videoTrailersJsonResults = getJsonFromHttpUrl(queryUrl);

        if (videoTrailersJsonResults == null) {

            Log.i(TAG, "SOMETHING WENT WRONG....");

            // ***********************************************************
            // INTERNET IS UP, BUT API MAY BE DOWN, so get MovieList from database
            // ***********************************************************
            returnVideoTrailerList = getVideoTrailerListFromCursor(context, movieId);

        } else {

            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(videoTrailersJsonResults, new TypeToken<Map<String, Object>>() {
            }.getType());

            String videoTrailerJsonResults = gson.toJson(map.get("results"));

            Type listType = new TypeToken<ArrayList<VideoTrailer>>() {
            }.getType();

            // Now Gson converts the json to a list of video trailers object based on the VideoTrailer class
            returnVideoTrailerList = gson.fromJson(videoTrailerJsonResults, listType);

            // ***********************************************************
            // The latest VideoTrailerList results are saved to the database
            // This works but only saves VideoTrailers for 1 movie at a time AND only if you view DetailActivity for the movie.

            // In saveMovieListToCursor() I have code that will Cache ALL UserReviews & VideoTrailers for OFF-LINE use
            // It has been commented out as it is inside a loop and make many API calls in a short period of time.
            // ***********************************************************
            saveVideoTrailerListToCursor(context, returnVideoTrailerList, movieId);
        }

        return returnVideoTrailerList;
    }

    public static ArrayList<VideoTrailer> getVideoTrailerListFromCursor(Context context, int movieId) {

        ArrayList<VideoTrailer> returnVideoTrailerList = new ArrayList<>();

        Uri uri = VideoTrailerEntry.CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        // Set selection
        selection = VideoTrailerEntry.COLUMN_MOVIE_ID + " = " + movieId;

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        // iterate over cursor to load into List
        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_VIDEO_TRAILER_ID));
            String iso_639_1 = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_ISO_639_1));
            String iso_3166_1 = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_ISO_3166_1));
            String key = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_KEY));
            String name = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_NAME));
            String site = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_SITE));
            int size = cursor.getInt(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_SIZE));
            String type = cursor.getString(cursor.getColumnIndex(VideoTrailerEntry.COLUMN_TYPE));

            VideoTrailer videoTrailer = new VideoTrailer(
                    id,
                    iso_639_1,
                    iso_3166_1,
                    key,
                    name,
                    site,
                    size,
                    type
            );

            returnVideoTrailerList.add(videoTrailer);
        }

        cursor.close();

        return returnVideoTrailerList;
    }

    private static void saveVideoTrailerListToCursor(Context context, ArrayList<VideoTrailer> videoTrailerList, int movieId) {

        Uri uri = VideoTrailerEntry.CONTENT_URI;

        ContentValues[] bulkVideoTrailerValues = new ContentValues[videoTrailerList.size()];

        for (int i = 0; i < videoTrailerList.size(); i++) {

            ContentValues videoTrailerValues = new ContentValues();

            videoTrailerValues.put(VideoTrailerEntry.COLUMN_MOVIE_ID, movieId);
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_VIDEO_TRAILER_ID, videoTrailerList.get(i).getId());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_ISO_639_1, videoTrailerList.get(i).getIso6391());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_ISO_3166_1, videoTrailerList.get(i).getIso31661());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_KEY, videoTrailerList.get(i).getKey());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_NAME, videoTrailerList.get(i).getName());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_SITE, videoTrailerList.get(i).getSite());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_SIZE, videoTrailerList.get(i).getSize());
            videoTrailerValues.put(VideoTrailerEntry.COLUMN_TYPE, videoTrailerList.get(i).getType());

            bulkVideoTrailerValues[i] = videoTrailerValues;
        }

        int recordsInserted = context.getContentResolver().bulkInsert(uri, bulkVideoTrailerValues);
    }

    // ==========UserReviewList=Start====================================================

    public static ArrayList<UserReview> getUserReviewList(Context context, int movieId) {

        ArrayList<UserReview> returnUserReviewList;

        // To fetch reviews you will want to make a request to the reviews endpoint
        // http://api.themoviedb.org/3/movie/{id}/reviews?api_key=<YOUR-API-KEY>

        URL queryUrl = MovieUtils.buildEndpointUrl(context, String.valueOf(movieId), MovieUtils.REVIEWS_ENDPOINT);

        String userReviewsJsonResults = getJsonFromHttpUrl(queryUrl);

        if (userReviewsJsonResults == null) {

            Log.i(TAG, "SOMETHING WENT WRONG....");

            // ***********************************************************
            // INTERNET IS UP, BUT API MAY BE DOWN, so get MovieList from database
            // ***********************************************************
            returnUserReviewList = getUserReviewListFromCursor(context, movieId);

        } else {

            Gson gson = new Gson();

            Map<String, Object> map = gson.fromJson(userReviewsJsonResults, new TypeToken<Map<String, Object>>() {
            }.getType());
            String userReviewJsonResults = gson.toJson(map.get("results"));

            Type listType = new TypeToken<ArrayList<UserReview>>() {
            }.getType();

            // Now Gson converts the json to a list of user review objects based on the UserReview class
            returnUserReviewList = gson.fromJson(userReviewJsonResults, listType);

            // ***********************************************************
            // The latest UserReviewList results are saved to the database
            // This works but only saves UserReviews for 1 movie at a time AND only if you view DetailActivity for the movie.

            // In saveMovieListToCursor() I have code that will Cache ALL UserReviews & VideoTrailers for OFF-LINE use
            // It has been commented out as it is inside a loop and make many API calls in a short period of time.
            // ***********************************************************
            saveUserReviewListToCursor(context, returnUserReviewList, movieId);
        }

        return returnUserReviewList;
    }

    public static ArrayList<UserReview> getUserReviewListFromCursor(Context context, int movieId) {

        ArrayList<UserReview> returnUserReviewList = new ArrayList<>();

        Uri uri = UserReviewEntry.CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        // Set selection
        selection = UserReviewEntry.COLUMN_MOVIE_ID + " = " + movieId;

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        // iterate over cursor to load into List
        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex(UserReviewEntry.COLUMN_USER_REVIEW_ID));
            String author = cursor.getString(cursor.getColumnIndex(UserReviewEntry.COLUMN_AUTHOR));
            String content = cursor.getString(cursor.getColumnIndex(UserReviewEntry.COLUMN_CONTENT));
            String url = cursor.getString(cursor.getColumnIndex(UserReviewEntry.COLUMN_URL));

            UserReview userReview = new UserReview(
                    id,
                    author,
                    content,
                    url
            );

            returnUserReviewList.add(userReview);
        }

        cursor.close();

        return returnUserReviewList;
    }

    private static void saveUserReviewListToCursor(Context context, ArrayList<UserReview> userReviewList, int movieId) {

        Uri uri = UserReviewEntry.CONTENT_URI;

        ContentValues[] bulkUserReviewValues = new ContentValues[userReviewList.size()];

        for (int i = 0; i < userReviewList.size(); i++) {

            ContentValues userReviewValues = new ContentValues();

            userReviewValues.put(UserReviewEntry.COLUMN_MOVIE_ID, movieId);
            userReviewValues.put(UserReviewEntry.COLUMN_USER_REVIEW_ID, userReviewList.get(i).getId());
            userReviewValues.put(UserReviewEntry.COLUMN_AUTHOR, userReviewList.get(i).getAuthor());
            userReviewValues.put(UserReviewEntry.COLUMN_CONTENT, userReviewList.get(i).getContent());
            userReviewValues.put(UserReviewEntry.COLUMN_URL, userReviewList.get(i).getUrl());

            bulkUserReviewValues[i] = userReviewValues;
        }

        int recordsInserted = context.getContentResolver().bulkInsert(uri, bulkUserReviewValues);
    }

    public static void clearAllButFavorites(Context context, int filter) {

        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        int recordDeleted;

//        uri = UserReviewEntry.CONTENT_URI;
//        recordDeleted = context.getContentResolver().delete(uri,selection,selectionArgs);
//
//        uri = VideoTrailerEntry.CONTENT_URI;
//        recordDeleted = context.getContentResolver().delete(uri,selection,selectionArgs);

        uri = MovieEntry.CONTENT_URI;
        selection = MovieEntry.COLUMN_CATEGORY + " = " + filter + " AND " + MovieEntry.COLUMN_FAVORITE + " != 1"; // Set selection where not = favorites
        recordDeleted = context.getContentResolver().delete(uri, selection, selectionArgs);
    }

}
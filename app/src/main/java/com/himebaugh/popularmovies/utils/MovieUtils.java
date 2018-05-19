package com.himebaugh.popularmovies.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.himebaugh.popularmovies.R;
import com.himebaugh.popularmovies.data.MovieContract;
import com.himebaugh.popularmovies.data.MovieProvider;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.Result;
import com.himebaugh.popularmovies.model.UserReview;
import com.himebaugh.popularmovies.model.VideoTrailer;
import com.himebaugh.popularmovies.data.MovieContract.MovieEntry;
import com.himebaugh.popularmovies.data.MovieContract.UserReviewEntry;
import com.himebaugh.popularmovies.data.MovieContract.VideoTrailerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieUtils {

    // NOTE: Avoid Null Error...
    // USE ContentResolver  NOT ContentProvider
    // So... context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    // Not.. movieProvider.query(uri, projection, selection, selectionArgs, sortOrder);

    private static final String TAG = MovieUtils.class.getSimpleName();

    public static final int FILTER_POPULAR = 0;
    public static final int FILTER_TOPRATED = 1;
    public static final int FILTER_FAVORITE = 2;

    public final static String POPULAR_MOVIES = "https://api.themoviedb.org/3/movie/popular";
    public final static String TOPRATED_MOVIES = "https://api.themoviedb.org/3/movie/top_rated";
    public final static String FAVORITE_MOVIES = "favorite";
    public final static String VIDEOS_ENDPOINT = "/videos";
    public final static String REVIEWS_ENDPOINT = "/reviews";
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
    public static URL buildEndpointUrl(Context context, String movieID, String endPoint) {

        Log.i(TAG, "buildEndpointUrl: ");

        // http://api.themoviedb.org/3/movie/278/videos?api_key=<YOUR-API-KEY>

        String baseUrl = "https://api.themoviedb.org/3/movie/";

        String movieDatabaseUrl = baseUrl + movieID + endPoint;


        // Reading API KEY from text file on gitignore list
        String apiKey = readRawTextFile(context, R.raw.api_key);
        // String apiKey = "Your-Key-Here";

        Log.i(TAG, "buildUrl: apiKey=" + apiKey);

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
    public static URL buildUrl(Context context, String movieDatabaseUrl, String page) {

        Log.i(TAG, "buildUrl: ");

        // Reading API KEY from text file on gitignore list
        String apiKey = readRawTextFile(context, R.raw.api_key);
        // String apiKey = "Your-Key-Here";

        Log.i(TAG, "buildUrl: apiKey=" + apiKey);

        Log.i(TAG, "HELP-1: ");
        Log.i(TAG, "movieDatabaseUrl: " + movieDatabaseUrl.toString());
        Log.i(TAG, "HELP-2: ");

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
     * @throws IOException Related to network and stream reading
     */
    public static String getJsonFromHttpUrl(URL url) {

        Log.i(TAG, "getJsonFromHttpUrl: ");

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
     * @throws IOException Related to network and stream reading
     */
    public static String getJsonFromOkHttpClient(URL url) throws IOException {

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

        Log.i(TAG, "getMovieList: ");

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
        //      Attempt to get MovieList through internet via API through getJsonFromHttpUrl(url)
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

                Log.i(TAG, "case MovieUtils.FILTER_FAVORITE: ");
                // ***********************************************************
                // Get MovieList from database via the MovieProvider
                // ***********************************************************
                returnMovieList = getMovieListFromCursor(context, filter);

                break;
            case MovieUtils.FILTER_POPULAR:

                Log.i(TAG, "case MovieUtils.FILTER_POPULAR: ");

                queryUrl = MovieUtils.buildUrl(context, MovieUtils.POPULAR_MOVIES, page);

                // Can use either one... getJsonFromHttpUrl() -OR- getJsonFromOkHttpClient()
                String popularMoviesJsonResults = getJsonFromHttpUrl(queryUrl);
                // String popularMoviesJsonResults = getJsonFromOkHttpClient(queryUrl);  // This Works Fine

                if (popularMoviesJsonResults == null) {

                    Log.i(TAG, "INTERNET IS DOWN - FILTER_POPULAR");

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

                Log.i(TAG, "case MovieUtils.FILTER_TOPRATED: ");

                queryUrl = MovieUtils.buildUrl(context, MovieUtils.TOPRATED_MOVIES, page);

                // Can use either one... getJsonFromHttpUrl() -OR- getJsonFromOkHttpClient()
                // String topratedMoviesJsonResults = getJsonFromHttpUrl(queryUrl);
                String topratedMoviesJsonResults = getJsonFromOkHttpClient(queryUrl);  // This Works Fine

                if (topratedMoviesJsonResults == null) {

                    Log.i(TAG, "INTERNET IS DOWN - FILTER_TOPRATED");

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
                    // Also, MovieUtils.FILTER_POPULAR is saved to COLUMN_CATEGORY
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

        Log.i(TAG, "getMovieListFromCursor: ");

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

        Log.i(TAG, "getMovieListFromCursor: COUNT=" + cursor.getCount());

        // iterate over cursor to load into List
        while (cursor.moveToNext()) {

            String posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
            Boolean adult = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) > 0;
            String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
            String releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));

//            String temp = String.valueOf(cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
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

        return returnMovieList;
    }

    private static void saveMovieListToCursor(Context context, ArrayList<Movie> movieList, int filter) {

        // TODO: CLEAR DATABASE OF ALL RECORDS from Movies, UserReviews & VideoTrailers.
        // Very inefficient, but this way it is always up to date with the latest data...
        // ONLY clear if movieList.size() > 0

        Log.i(TAG, "saveMovieListToCursor: ");

        Uri uri = MovieEntry.CONTENT_URI;

        ContentValues[] bulkMovieValues = new ContentValues[movieList.size()];

        for (int i = 0; i < movieList.size(); i++) {

            Log.i(TAG, "saveMovieListToCursor: i = " + i);

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

            // ****************************************************************************************************************
            // This slows everything down and adds many more API calls to the MainActivity, but all will be saved for online use...
            // Call this in the loop as it will retrieve and insert into database


            try {
                getVideoTrailerList(context, movieList.get(i).getId());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        int recordsInserted = context.getContentResolver().bulkInsert(uri, bulkMovieValues);

        Log.i(TAG, "saveMovieListToCursor: Records Inserted = " + recordsInserted);
    }

    // ==========VideoTrailerList=Start====================================================

    public static List<VideoTrailer> getVideoTrailerList(Context context, int movieId) throws IOException {

        Log.i(TAG, "getVideoTrailerList: ");

        List<VideoTrailer> returnVideoTrailerList;

        // To fetch trailers you will want to make a request to the videos endpoint
        // http://api.themoviedb.org/3/movie/{id}/videos?api_key=<YOUR-API-KEY>
        URL queryUrl = MovieUtils.buildEndpointUrl(context, String.valueOf(movieId), MovieUtils.VIDEOS_ENDPOINT);

        //URL queryUrl = MovieUtils.buildUrl(context, MovieUtils.POPULAR_MOVIES, page);

        String videoTrailersJsonResults = getJsonFromHttpUrl(queryUrl);

        // ======================

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

            Type listType = new TypeToken<List<VideoTrailer>>() {
            }.getType();

            // Now Gson converts the json to a list of movies object based on the Movie class
            returnVideoTrailerList = gson.fromJson(videoTrailerJsonResults, listType);

            // ***********************************************************
            // The latest VideoTrailerList results are saved to the database
            // This worked but only save 1 movie at a time.  Now doing it above in saveMovieListToCursor()
            // ***********************************************************
            saveVideoTrailerListToCursor(context, returnVideoTrailerList, movieId);
        }

        return returnVideoTrailerList;
    }

    public static List<VideoTrailer> getVideoTrailerListFromCursor(Context context, int movieId) {

        Log.i(TAG, "getVideoTrailerListFromCursor: ");

        List<VideoTrailer> returnVideoTrailerList = new ArrayList<>();

        Uri uri = VideoTrailerEntry.CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        // Set selection
        selection = VideoTrailerEntry.COLUMN_MOVIE_ID + " = " + movieId;

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        Log.i(TAG, "getVideoTrailerListFromCursor: COUNT=" + cursor.getCount());

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

        return returnVideoTrailerList;
    }

    private static void saveVideoTrailerListToCursor(Context context, List<VideoTrailer> videoTrailerList, int movieId) {

        Log.i(TAG, "saveVideoTrailerListToCursor: ");

        Uri uri = VideoTrailerEntry.CONTENT_URI;

        ContentValues[] bulkVideoTrailerValues = new ContentValues[videoTrailerList.size()];

        for (int i = 0; i < videoTrailerList.size(); i++) {

            Log.i(TAG, "saveVideoTrailerListToCursor: i = " + i);

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

        Log.i(TAG, "saveVideoTrailerListToCursor: Records Inserted = " + recordsInserted);
    }

    // ==========UserReviewList=Start====================================================

    public static List<UserReview> getUserReviewList(Context context, URL url) throws IOException {

        Log.i(TAG, "getUserReviewList: ");

        String userReviewsJsonResults = getJsonFromHttpUrl(url);

        Gson gson = new Gson();

        Map<String, Object> map = gson.fromJson(userReviewsJsonResults, new TypeToken<Map<String, Object>>() {
        }.getType());
        String movieListJsonResults = gson.toJson(map.get("results"));

        Type listType = new TypeToken<List<UserReview>>() {
        }.getType();

        // Now Gson converts the json to a list of movies object based on the Movie class
        // Movie class member variable names needed to be named with under_scores not camelCase to work.
        return gson.fromJson(movieListJsonResults, listType);
    }



}
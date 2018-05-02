package com.himebaugh.popularmovies.utils;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieUtils {

    private static final String TAG = MovieUtils.class.getSimpleName();

    public final static String POPULAR_MOVIES = "https://api.themoviedb.org/3/movie/popular";
    public final static String TOPRATED_MOVIES = "https://api.themoviedb.org/3/movie/top_rated";
    final static String PARAM_APIKEY = "api_key";
    final static String apiKey = "ad7c6dd44b65b088cd2adee6760754bd";
    final static String PARAM_PAGE = "page";
    final static String page = "1";
    final static String PARAM_LANGUAGE = "language";
    final static String language = "en-US";

    /**
     * Builds the URL used to query GitHub.
     *
     * @param movieDatabaseUrl The keyword that will be queried for.
     * @return The URL to use to query the GitHub.
     */
    public static URL buildUrl(String movieDatabaseUrl, String page) {
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

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getMoviesJsonFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Alternatively this method returns the entire result using OkHttpClient.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getMoviesJsonFromOkHttpClient(URL url) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        String moviesJsonString = null;

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                moviesJsonString = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return moviesJsonString;
    }

    public static List<Movie> getMovieList(URL url) throws IOException, JSONException {

         String moviesJsonResults = getMoviesJsonFromHttpUrl(url);

        // Uses JSONObject & JSONArray to get strictly the Movie List Result Object
        // could not figure out how to use Gson until removal of
        // "page", "total_results" & "total_pages"
        // Returns everything within "results": [
        Result result = parseResultsJson(moviesJsonResults);

        String movieListJsonResults = result.getResults().toString();

        Type listType = new TypeToken<List<Movie>>() {}.getType();

        // Now Gson converts the json to a list of movies object based on the Movie class
        // Movie class member variable names needed to be named with under_scores not camelCase to work.
        List<Movie> movieList = new Gson().fromJson(movieListJsonResults, listType);

        return movieList;
    }

    public static Result parseResultsJson(String resultJsonStr) throws JSONException {

        final String RESULT_RESULTS = "results";

        JSONObject resultJsonObject = new JSONObject(resultJsonStr);

        List<String> resultMovies = new ArrayList<>();
        JSONArray resultMoviesArray = resultJsonObject.getJSONArray(RESULT_RESULTS);
        for (int i = 0; i < resultMoviesArray.length(); i++) {
            resultMovies.add( resultMoviesArray.getString(i)      );
        }

        Result result = new Result(resultMovies);

        return result;
    }

}
package com.himebaugh.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.himebaugh.popularmovies.model.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ListItemViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();
    private List<Movie> mMovieList;
    private Context mContext;

    /**
     * Constructor for the MovieAdapter that initializes the Context.
     *
     * @param context the current Context
     */
    public MovieAdapter(Context context) {
        mContext = context;
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public List<Movie> loadMovies(List<Movie> movieList) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mMovieList == movieList) {
            return null; // bc nothing has changed
        }
        List<Movie> temp = mMovieList;
        mMovieList = movieList; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (movieList != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

//        Context context = viewGroup.getContext();
//        int layoutIdForListItem = R.layout.list_item;
//        LayoutInflater inflater = LayoutInflater.from(context);
//        boolean shouldAttachToParentImmediately = false;
//
//        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);

        ListItemViewHolder viewHolder = new ListItemViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {

        Movie movie = mMovieList.get(position);
        holder.listItemTest.setText(movie.getTitle());
    }


    @Override
    public int getItemCount() {
        if (mMovieList == null) {
            return 0;
        }
        return mMovieList.size();
    }


    public class ListItemViewHolder extends RecyclerView.ViewHolder {

        TextView listItemTest;

        public ListItemViewHolder(View itemView) {
            super(itemView);

            listItemTest = (TextView) itemView.findViewById(R.id.tv_item_test);

        }

    }

    // https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true

//            Using Picasso To Fetch Images and Load Them Into Views
//            You can use Picasso to easily load album art thumbnails into your views using:
//
//            Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
//
//            Picasso will handle loading the images on a background thread, image decompression and caching the images.

    // http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg

//            A note on resolving poster paths with themoviedb.org API
//            You will notice that the API response provides a relative path to a movie poster image when you request the metadata for a specific movie.
//
//
//                    For example, the poster path return for Interstellar is “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
//
//
//            You will need to append a base path ahead of this relative path to build the complete url you will need to fetch the image using Picasso.
//
//
//            It’s constructed using 3 parts:
//
//
//            The base URL will look like: http://image.tmdb.org/t/p/.
//            Then you will need a ‘size’, which will be one of the following: "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
//            And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
//
//            Combining these three parts gives us a final url of http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
//
//
//
//            This is also explained explicitly in the API documentation for /configuration.


}

package com.himebaugh.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.himebaugh.popularmovies.R;
import com.himebaugh.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ListItemViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    //  The base URL will look like: http://image.tmdb.org/t/p/.
    //  Then you will need a ‘size’, which will be one of the following: "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
    //  And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
    //  Combining these three parts gives us a final url of http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg

    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SIZE = "w500";

    private List<Movie> mMovieList;
    private Context mContext;

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private final MovieAdapterOnClickHandler mClickHandler;

    // The interface that receives onClick messages
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * Constructor for the MovieAdapter that initializes the Context.
     *
     * @param context the current Context
     * @param clickHandler The on-click handler for this adapter. This single handler is called when an item is clicked.
     */
    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }


    /**
     * Using this Method designed for Cursors to pass in List of Movies
     *
     * */
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

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_movie, viewGroup, false);

        return new ListItemViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {

        Movie movie = mMovieList.get(position);

        // holder.listItemTest.setText(movie.getTitle());

        String imageURL = BASE_URL + SIZE + movie.getPosterPath();

        Picasso.get().load(imageURL).into(holder.listItemImageView);

    }


    @Override
    public int getItemCount() {
        if (mMovieList == null) {
            return 0;
        }
        return mMovieList.size();
    }


    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // TextView listItemTest;
        ImageView listItemImageView;

        private ListItemViewHolder(View itemView) {
            super(itemView);

            // listItemTest = (TextView) itemView.findViewById(R.id.tv_item_test);
            listItemImageView = itemView.findViewById(R.id.iv_item_poster);

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            Movie movie = mMovieList.get(adapterPosition);

            mClickHandler.onClick(movie);
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

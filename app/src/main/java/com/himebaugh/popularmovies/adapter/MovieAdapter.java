package com.himebaugh.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
     * @param context      the current Context
     * @param clickHandler The on-click handler for this adapter. This single handler is called when an item is clicked.
     */
    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * Using this Method designed for Cursors to pass in List of Movies
     */
    public void loadMovies(List<Movie> movieList) {
        // check if this list is the same as the previous list (mMovieList)
        if (mMovieList == movieList) {
            return; // bc nothing has changed
        }
        List<Movie> temp = mMovieList;
        mMovieList = movieList; // new list value assigned

        //check if this is a valid list, then update the list
        if (movieList != null) {
            this.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_movie, viewGroup, false);

        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {

        Movie movie = mMovieList.get(position);

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

        ImageView listItemImageView;

        private ListItemViewHolder(View itemView) {
            super(itemView);

            listItemImageView = itemView.findViewById(R.id.iv_item_poster);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            Movie movie = mMovieList.get(adapterPosition);

            Log.i(TAG, "onClick: movieId=" + movie.getId());

            mClickHandler.onClick(movie);
        }
    }

}

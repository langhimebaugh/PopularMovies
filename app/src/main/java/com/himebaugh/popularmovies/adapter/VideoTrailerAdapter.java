package com.himebaugh.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.himebaugh.popularmovies.R;
import com.himebaugh.popularmovies.model.Movie;
import com.himebaugh.popularmovies.model.VideoTrailer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoTrailerAdapter extends RecyclerView.Adapter<VideoTrailerAdapter.ListItemViewHolder> {

    private static final String TAG = VideoTrailerAdapter.class.getSimpleName();

    private List<VideoTrailer> mVideoTrailerList;
    private Context mContext;

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private final VideoTrailerAdapterOnClickHandler mClickHandler;

    // The interface that receives onClick messages
    public interface VideoTrailerAdapterOnClickHandler {
        void onClick(VideoTrailer videoTrailer);
    }

    public VideoTrailerAdapter(Context mContext, VideoTrailerAdapterOnClickHandler mClickHandler) {
        this.mContext = mContext;
        this.mClickHandler = mClickHandler;
    }

    /**
     * Using this Method designed for Cursors to pass in List of Movies
     *
     * */
    public List<VideoTrailer> loadVideoTrailers(List<VideoTrailer> videoTrailerList) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mVideoTrailerList == videoTrailerList) {
            return null; // bc nothing has changed
        }
        List<VideoTrailer> temp = mVideoTrailerList;
        mVideoTrailerList = videoTrailerList; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (videoTrailerList != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    @NonNull
    @Override
    public VideoTrailerAdapter.ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, viewGroup, false);

        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoTrailerAdapter.ListItemViewHolder holder, int position) {

        VideoTrailer videoTrailer = mVideoTrailerList.get(position);

        // holder.listItemTest.setText(movie.getTitle());
        holder.listItemName.setText(videoTrailer.getName());

//        String imageURL = BASE_URL + SIZE + movie.getPosterPath();
//
//        Picasso.get().load(imageURL).into(holder.listItemImageView);

    }

    @Override
    public int getItemCount() {
        if (mVideoTrailerList == null) {
            return 0;
        }
        return mVideoTrailerList.size();
    }


    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView listItemName;

        // Play Icon Image in video_list_item.xml
        // Doesn't change so no need to do anything here.

        private ListItemViewHolder(View itemView) {
            super(itemView);

            listItemName = itemView.findViewById(R.id.trailer_name_tv);

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            VideoTrailer videoTrailer = mVideoTrailerList.get(adapterPosition);

            mClickHandler.onClick(videoTrailer);
        }
    }

}

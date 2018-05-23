package com.himebaugh.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.himebaugh.popularmovies.R;
import com.himebaugh.popularmovies.model.UserReview;

import java.util.List;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.ListItemViewHolder> {

    private static final String TAG = UserReviewAdapter.class.getSimpleName();

    private List<UserReview> mUserReviewList;
    private Context mContext;

    public UserReviewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Using this Method designed for Cursors to pass in List of Movies
     */
    public List<UserReview> loadUserReviews(List<UserReview> userReviewList) {
        // check if this list is the same as the previous list (mUserReviewList)
        if (mUserReviewList == userReviewList) {
            return null; // bc nothing has changed
        }
        List<UserReview> temp = mUserReviewList;
        mUserReviewList = userReviewList; // new list value assigned

        //check if this is a valid list, then update the list
        if (userReviewList != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    @NonNull
    @Override
    public UserReviewAdapter.ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_review, viewGroup, false);

        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewAdapter.ListItemViewHolder holder, int position) {

        UserReview userReview = mUserReviewList.get(position);

        holder.listItemAuthor.setText(userReview.getAuthor());
        holder.listItemContent.setText(userReview.getContent());
    }

    @Override
    public int getItemCount() {
        if (mUserReviewList == null) {
            return 0;
        }
        return mUserReviewList.size();
    }


    public class ListItemViewHolder extends RecyclerView.ViewHolder {

        TextView listItemAuthor;
        TextView listItemContent;

        private ListItemViewHolder(View itemView) {
            super(itemView);

            listItemAuthor = itemView.findViewById(R.id.author_tv);
            listItemContent = itemView.findViewById(R.id.content_tv);
        }

    }

}

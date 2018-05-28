//package com.himebaugh.popularmovies.task;
//
//import android.os.AsyncTask;
//
//import com.himebaugh.popularmovies.model.VideoTrailer;
//import com.himebaugh.popularmovies.utils.MovieUtils;
//
//import java.util.ArrayList;
//
//import static com.himebaugh.popularmovies.utils.NetworkUtil.isNetworkAvailable;
//
//public class LoadVideoTrailersTask extends AsyncTask<Integer, Void, ArrayList<VideoTrailer>> {
//
//    @Override
//    protected ArrayList<VideoTrailer> doInBackground(Integer... params) {
//        int movieId = params[0];
//
//        ArrayList<VideoTrailer> videoTrailerList = null;
//
//        if (isNetworkAvailable(mContext)) {
//            videoTrailerList = MovieUtils.getVideoTrailerList(mContext, movieId);
//        } else {
//            videoTrailerList = MovieUtils.getVideoTrailerListFromCursor(mContext, movieId);
//        }
//
//        return videoTrailerList;
//    }
//
//    @Override
//    protected void onPostExecute(ArrayList<VideoTrailer> videoTrailerList) {
//
//        // Save to store on rotation
//        mVideoTrailerList = videoTrailerList;
//
//        // TO PREVENT ERROR WHEN NO INTERNET...
//        if (videoTrailerList != null) {
//
//            videoTrailerIsAvailableToShare = false;
//
//            for (VideoTrailer videoTrailer : videoTrailerList) {
//
//                if (!videoTrailerIsAvailableToShare && videoTrailer.getType().contentEquals(TRAILER)) {
//
//                    // Store the first VideoTrailer that "type" = "Trailer"
//                    // Then flag as shareable to use in shareMovie()
//                    mVideoTrailer = videoTrailer;
//
//                    videoTrailerIsAvailableToShare = true;
//                }
//
//            }
//
//            mVideoAdapter.loadVideoTrailers(videoTrailerList);
//            mVideoAdapter.notifyDataSetChanged();
//
//        }
//
//    }
//
//}

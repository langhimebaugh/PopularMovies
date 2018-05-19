package com.himebaugh.popularmovies.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class NetworkService extends Service {

    private final static String TAG = NetworkService.class.getName();

    NetworkBroadcastReceiver mNetworkReceiver;
    IntentFilter mNetworkIntentFilter;

    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(CONNECTIVITY_ACTION)) {

                Log.i(TAG, "onReceive: " + CONNECTIVITY_ACTION);

                showConnectivityStatus(NetworkUtil.isNetworkAvailable(context));
            }

        }
    }

    private void showConnectivityStatus(boolean isNetworkActive) {
        if (isNetworkActive) {

            Log.i(TAG, "Internet Connection");

            Toast.makeText(this, "Internet Connection", Toast.LENGTH_LONG).show();

        } else {

            Log.i(TAG, "No Internet Connection");

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNetworkIntentFilter = new IntentFilter();
        mNetworkReceiver = new NetworkBroadcastReceiver();
        mNetworkIntentFilter.addAction(CONNECTIVITY_ACTION);    // Intent.ACTION_AIRPLANE_MODE_CHANGED

        registerReceiver(mNetworkReceiver, mNetworkIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}

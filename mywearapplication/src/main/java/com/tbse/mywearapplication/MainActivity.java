package com.tbse.mywearapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private TextView mTimeTextView;
    private TextView mDayTextView;
    private TextView mHighTextView;
    private TextView mLowTextView;
    private TextView mDescriptionTextView;
    private GoogleApiClient apiClient;
    private Intent serviceIntent;
    public static String TAG = "nano6wear";
    private int a = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimeTextView = (TextView) findViewById(R.id.time);
        mDayTextView = (TextView) findViewById(R.id.day);
        mHighTextView = (TextView) findViewById(R.id.high);
        mLowTextView = (TextView) findViewById(R.id.low);
        mDescriptionTextView = (TextView) findViewById(R.id.description);
        serviceIntent = new Intent(this, WatchUpdateService.class);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();

        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimeTextView = (TextView) stub.findViewById(R.id.time);
                mDayTextView = (TextView) stub.findViewById(R.id.day);
                mHighTextView = (TextView) stub.findViewById(R.id.high);
                mLowTextView = (TextView) stub.findViewById(R.id.low);
                mDescriptionTextView = (TextView) stub.findViewById(R.id.description);
                registerReceiver(mTimeInfoReceiver, INTENT_FILTER);
                mTimeInfoReceiver.onReceive(MainActivity.this, registerReceiver(null, INTENT_FILTER));    //  Here, we're just calling our onReceive() so it can set the current time.
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent);
        apiClient.connect();
    }

    @Override
    protected void onPause() {
        Wearable.DataApi.removeListener(apiClient, this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        apiClient.disconnect();
        stopService(serviceIntent);
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "on conn sus");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "on conn failed");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged");
        DataMap dataMap = DataMapItem.fromDataItem(
                dataEventBuffer.get(0).getDataItem()).getDataMap();

        mDayTextView.setText("day is " + dataMap.getString("Day"));
        mHighTextView.setText("high is " + dataMap.getString("High"));
        mLowTextView.setText("low is " + dataMap.getString("Low"));
        mDescriptionTextView.setText("desciption is " + dataMap.getString("Description"));
    }

    private final static IntentFilter INTENT_FILTER;
    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Intent.ACTION_TIME_TICK);
        INTENT_FILTER.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        INTENT_FILTER.addAction(Intent.ACTION_TIME_CHANGED);
    }

    private final String TIME_FORMAT_DISPLAYED = "kk:mm a";

    private BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mTimeTextView.setText(
                    new SimpleDateFormat(TIME_FORMAT_DISPLAYED)
                            .format(Calendar.getInstance().getTime()));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeInfoReceiver);
    }
}

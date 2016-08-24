package com.tbse.mywearapplication;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import static com.tbse.mywearapplication.MainActivity.TAG;

/**
 * Created by todd on 8/7/16.
 */

public class WatchUpdateService extends WearableListenerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                long time = dataMap.getLong("Time");
                Log.d(TAG, "onDataChanged time is " + time);
                if ("/update".equalsIgnoreCase(dataEvent.getDataItem().getUri().getPath())) {
                    Log.d(TAG, "device updated");
                }
            }
        }
    }
}

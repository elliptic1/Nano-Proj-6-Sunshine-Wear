package com.tbse.mywearapplication;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by todd on 8/7/16.
 */

public class WatchUpdateService extends WearableListenerService {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("wear", "wear onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("wear", "wear onDataChanged");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                dataMap.putLong("Time",System.currentTimeMillis());
                if("/update".equalsIgnoreCase(dataEvent.getDataItem().getUri().getPath())){
                    Log.d("wear", "device updated");
                }
            }
        }
    }
}

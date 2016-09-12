package com.tbse.mywearapplication;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class ExampleWatchFace extends CanvasWatchFaceService {

    private static String TAG = "Nano6-ExampleWatchFace";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = 1000;

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements IWatchFaceConfig,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener {

        WatchFaceDrawer mWatchfaceDrawer;

        boolean mAmbient = false;
        boolean mLowBitAmbient = false;
        boolean mIsRound = false;
        GregorianCalendar mCalendar = new GregorianCalendar();

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;
        private GoogleApiClient apiClient;

        boolean mLightTheme = true;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mWatchfaceDrawer = new WatchFaceDrawer(getApplicationContext());
            setNewWatchFaceStyle();

            apiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addApi(Wearable.API)
                    .build();

            apiClient.connect();


        }

        private void setNewWatchFaceStyle() {
            WatchFaceStyle.Builder watchfaceStyleBuilder = new WatchFaceStyle.Builder(ExampleWatchFace.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false);

            if (mLightTheme) {
                watchfaceStyleBuilder = watchfaceStyleBuilder.setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR | WatchFaceStyle.PROTECT_HOTWORD_INDICATOR);
            }

            setWatchFaceStyle(watchfaceStyleBuilder.build());
        }

        @Override
        public void onDestroy() {
            Wearable.DataApi.removeListener(apiClient, this);
            apiClient.disconnect();

            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                mWatchfaceDrawer.onAmbientModeChanged(getApplicationContext(), this);
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mWatchfaceDrawer.onDraw(getApplicationContext(), this, canvas, bounds);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            final IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            ExampleWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            ExampleWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                final long timeMs = System.currentTimeMillis();
                final long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }


        // IWatchfaceConfig

        @Override
        public Calendar getCalendar() {
            return mCalendar;
        }

        @Override
        public boolean isLightTheme() {
            return mLightTheme;
        }

        @Override
        public boolean isAmbient() {
            return mAmbient;
        }

        @Override
        public boolean isLowBitAmbient() {
            return mLowBitAmbient;
        }

        @Override
        public boolean isRound() {
            return mIsRound;
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
            final DataMap dataMap = DataMapItem.fromDataItem(
                    dataEventBuffer.get(0).getDataItem()).getDataMap();

            mWatchfaceDrawer.setWeather(
                    dataMap.getString("Day"),
                    dataMap.getString("Low"),
                    dataMap.getString("High"),
                    dataMap.getString("Description"));
        }


    }

    private static class EngineHandler extends Handler {
        private final WeakReference<ExampleWatchFace.Engine> mWeakReference;

        EngineHandler(ExampleWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            final ExampleWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}

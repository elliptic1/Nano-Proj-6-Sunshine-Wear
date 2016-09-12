package com.tbse.mywearapplication;

/* Watch face drawing code is from Android Studio examples. License:
 *
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

class WatchFaceDrawer {
    private boolean mIsMobilePreview = false;

    private final float mHourOuterOffset;
    private final float mMinuteOuterOffset;
    private final float mSecondOuterOffset;

    private final float mPreviewSquareRadius;

    private final Paint mBackgroundPaint;
    private Paint mPreviewBorderPaint;
    private final Paint mSecondHandPaint;
    private final Paint mMinuteHandPaint;
    private final Paint mHourHandPaint;

    private Paint textPaint;
    private Paint ambientTextPaint;
    private String day = "default day";
    private String high = "high";
    private String low = "low";
    private String weatherDescription = "desc";
    private Bitmap image;

    // put your resources here (Paint objects, dimensions, colors, …)

    WatchFaceDrawer(Context context) {
        Resources res = context.getResources();

        // initialize your resources

        mHourOuterOffset = res.getDimension(R.dimen.hour_outer_offset);
        mMinuteOuterOffset = res.getDimension(R.dimen.minute_outer_offset);
        mSecondOuterOffset = res.getDimension(R.dimen.second_outer_offset);

        mPreviewSquareRadius = res.getDimension(R.dimen.watchface_preview_square_radius);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(ContextCompat.getColor(context, R.color.watchface_background));

        mSecondHandPaint = new Paint();
        mSecondHandPaint.setColor(ContextCompat.getColor(context, R.color.second_hand));
        mSecondHandPaint.setStrokeWidth(res.getDimension(R.dimen.seconds_hand_stroke));
        mSecondHandPaint.setAntiAlias(true);
        mSecondHandPaint.setStrokeCap(Paint.Cap.ROUND);

        mMinuteHandPaint = new Paint(mSecondHandPaint);
        mMinuteHandPaint.setColor(ContextCompat.getColor(context, R.color.minute_hand));
        mMinuteHandPaint.setStrokeWidth(res.getDimension(R.dimen.minute_hand_stroke));

        mHourHandPaint = new Paint(mSecondHandPaint);
        mHourHandPaint.setColor(ContextCompat.getColor(context, R.color.hour_hand));
        mHourHandPaint.setStrokeWidth(res.getDimension(R.dimen.hour_hand_stroke));

        textPaint = new Paint();
        ambientTextPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(context, R.color.black));
        ambientTextPaint.setColor(ContextCompat.getColor(context, R.color.white));
    }

    public void setMobilePreview(Context context, boolean isMobilePreview) {
        mIsMobilePreview = isMobilePreview;
        mBackgroundPaint.setAntiAlias(isMobilePreview);

        if (mIsMobilePreview && mPreviewBorderPaint == null) {
            mPreviewBorderPaint = new Paint();
            mPreviewBorderPaint.setColor(ContextCompat.getColor(context, R.color.watchface_preview_border));
            mPreviewBorderPaint.setAntiAlias(true);
        }
    }

    void onAmbientModeChanged(Context context, IWatchFaceConfig config) {
        if (config.isLowBitAmbient()) {
            final boolean inAmbientMode = config.isAmbient();

            mSecondHandPaint.setAntiAlias(!inAmbientMode);
            mMinuteHandPaint.setAntiAlias(!inAmbientMode);
            mHourHandPaint.setAntiAlias(!inAmbientMode);

            mSecondHandPaint.setColor(ContextCompat.getColor(context, inAmbientMode ? R.color.low_bit_ambient_hand : R.color.second_hand));
            mMinuteHandPaint.setColor(ContextCompat.getColor(context, inAmbientMode ? R.color.low_bit_ambient_hand : R.color.minute_hand));
            mHourHandPaint.setColor(ContextCompat.getColor(context, inAmbientMode ? R.color.low_bit_ambient_hand : R.color.hour_hand));
        }
    }

    void setImage(Bitmap bitmap) {
        this.image = bitmap;
    }

    void setWeather(String day, String low, String high, String desc) {
        this.day = day;
        this.low = low;
        this.high = high;
        this.low = low;
        this.weatherDescription = desc;
    }

    void onDraw(Context context, IWatchFaceConfig config, Canvas canvas, Rect bounds) {
        final Calendar calendar = config.getCalendar();
        final boolean isAmbient = config.isAmbient();
        final boolean isRound = config.isRound();
        final boolean useLightTheme = !isAmbient && config.isLightTheme();

        mBackgroundPaint.setColor(ContextCompat.getColor(context, useLightTheme
                ? R.color.watchface_background_light : R.color.watchface_background));

        /////////////////////////////////////////////////////////////////////
        // Draw your watch face here, using the provided canvas and bounds //
        /////////////////////////////////////////////////////////////////////

        final int width = bounds.width();
        final int height = bounds.height();

        // Find the center. Ignore the window insets so that, on round
        // watches with a "chin", the watch face is centered on the entire
        // screen, not just the usable portion.
        final float centerX = width / 2f;
        final float centerY = height / 2f;

        // Draw the background.
        if (mIsMobilePreview) {
            if (isRound) {
                canvas.drawCircle(centerX, centerY, centerX, mPreviewBorderPaint);
            } else {
                final float radius = mPreviewSquareRadius;
                final RectF rectF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.drawRoundRect(rectF, radius, radius, mPreviewBorderPaint);
            }

            final float translateXY = width * 0.05f;
            canvas.translate(translateXY, translateXY);
            canvas.scale(0.9f, 0.9f);

            if (isRound) {
                canvas.drawCircle(centerX, centerY, centerX, mBackgroundPaint);
            } else {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            }
        } else {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
        }

        // Draw weather icon
        if (image != null) {
            Matrix matrix = new Matrix();
            matrix.setTranslate(50, 90);
            matrix.preScale(0.2f, 0.2f);
            canvas.drawBitmap(image, matrix, null);
        }

        final float secRot = calendar.get(Calendar.SECOND) / 30f * (float) Math.PI;
        final int minutes = calendar.get(Calendar.MINUTE);
        final float minRot = minutes / 30f * (float) Math.PI;
        final float hrRot = ((calendar.get(Calendar.HOUR) + (minutes / 60f)) / 6f) * (float) Math.PI;

        final float secLength = centerX - mSecondOuterOffset;
        final float minLength = centerX - mMinuteOuterOffset;
        final float hrLength = centerX - mHourOuterOffset;

        if (!isAmbient) {
            final float secX = (float) Math.sin(secRot) * secLength;
            final float secY = (float) -Math.cos(secRot) * secLength;
            canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mSecondHandPaint);
        }

        final float minX = (float) Math.sin(minRot) * minLength;
        final float minY = (float) -Math.cos(minRot) * minLength;
        canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mMinuteHandPaint);

        final float hrX = (float) Math.sin(hrRot) * hrLength;
        final float hrY = (float) -Math.cos(hrRot) * hrLength;
        canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHourHandPaint);

        // Draw weather text
        canvas.drawText(day, 50, 50, isAmbient ? ambientTextPaint : textPaint);
        canvas.drawText(low, 50, 65, isAmbient ? ambientTextPaint : textPaint);
        canvas.drawText(high, 80, 65, isAmbient ? ambientTextPaint : textPaint);
        canvas.drawText(weatherDescription, 50, 80, isAmbient ? ambientTextPaint : textPaint);

    }

    static String TAG = "WatchFaceDrawer";

}


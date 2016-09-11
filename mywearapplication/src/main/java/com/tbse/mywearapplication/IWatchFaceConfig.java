package com.tbse.mywearapplication;

import java.util.Calendar;

public interface IWatchFaceConfig {
    Calendar getCalendar();

    boolean isAmbient();

    boolean isLowBitAmbient();

    boolean isRound();

    // put your watch face options here
    boolean isLightTheme();
}

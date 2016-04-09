package com.bignerdranch.android.criminalintent;

import java.io.Serializable;

/**
 * Created by Rasul on 09.04.2016.
 */
public class Time implements Serializable {
    int hour, minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}

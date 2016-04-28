package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Admin on 06.04.2016.
 */
public class TimePickerFragment extends DialogFragment {

    private static final String ARG_TIME = "time";
    private static final String EXTRA_TIME = "com.bignerdranch.android.criminalintent.time";
    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date time) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Time getTime(Intent data) {
        return (Time) data.getSerializableExtra(EXTRA_TIME);
    }

    public void sendResult(Time time) {
        Intent data = new Intent();
        data.putExtra(EXTRA_TIME, time);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_picker);

        Date time = (Date) getArguments().getSerializable(ARG_TIME);
        if (time != null) {
            Calendar c = GregorianCalendar.getInstance();
            c.setTime(time);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTimePicker.setHour(c.get(Calendar.HOUR));
                mTimePicker.setMinute(c.get(Calendar.MINUTE));
            } else {
                mTimePicker.setCurrentHour(c.get(Calendar.HOUR));
                mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
            }
        }

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour, minute;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    hour = mTimePicker.getHour();
                                    minute = mTimePicker.getMinute();
                                } else {
                                    hour = mTimePicker.getCurrentHour();
                                    minute = mTimePicker.getCurrentMinute();
                                }
                                sendResult(new Time(hour, minute));
                            }
                        }

                )
                .create();
    }

//    public static Date getDate(Intent data) {
//        //Date date = data.getSerializableExtra(EXTRA_)
//
//    }
}

package com.yknx4.magmarooms;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.Event;

import java.util.List;

/**
 * Created by yknx4 on 2/19/16.
 */
public abstract class CalendarAsyncTask extends AsyncTask<String,Void,List<Event>> {
    protected Exception mLastError = null;
    CalendarFetcher mCalendarFetcher;
    GoogleAccountCredential mCredential;
    String calendarId;

    public CalendarAsyncTask(String calendarId, GoogleAccountCredential credential){
        mCalendarFetcher = new CalendarFetcher(credential);
        mCredential = credential;
    }
    public CalendarAsyncTask(CalendarFetcher calendarFetcher){
        mCalendarFetcher = calendarFetcher;
    }


    @Override
    protected List<Event> doInBackground(String... params) {
        try {
            return mCalendarFetcher.getDataFromApi(params[0]);
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }
}

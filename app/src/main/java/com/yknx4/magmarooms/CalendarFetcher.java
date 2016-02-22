package com.yknx4.magmarooms;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yknx4 on 2/19/16.
 */
public class CalendarFetcher {
    private final GoogleAccountCredential mAccountCredential;

    private com.google.api.services.calendar.Calendar mService = null;

    public CalendarFetcher( GoogleAccountCredential mAccountCredential){
        this.mAccountCredential = mAccountCredential;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mAccountCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();
    }

    public List<Event> getDataFromApi(String calendarId) throws IOException {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY,0);
        mCalendar.set(Calendar.MINUTE,0);
        DateTime start = new DateTime(mCalendar.getTimeInMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY,23);
        mCalendar.set(Calendar.MINUTE,59);
        DateTime finish = new DateTime(mCalendar.getTimeInMillis());

        Events events = mService.events().list(calendarId)
                .setMaxResults(10)
                .setTimeMax(finish)
                .setTimeMin(start)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        for (Event ev:items){
            if(ev.getStart()==null || ev.getEnd()==null)
                items.remove(ev);
        }
        return items;
    }

}

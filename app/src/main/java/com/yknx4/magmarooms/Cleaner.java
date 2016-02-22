package com.yknx4.magmarooms;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.sql.Date;
import java.text.DateFormat;

/**
 * Created by yknx4 on 2/19/16.
 */
public class Cleaner {
    private final Event mEvent;

    private Cleaner(Event event){
        this.mEvent = event;
        Log.d("","");
    }

    public static Cleaner with(Event event){
        return new Cleaner(event);
    }

    public String getEventName(){
        if(mEvent==null){
            return "None";
        }
        return mEvent.getSummary();
    }


    public DateTime getStartTime(){
        return mEvent.getStart().getDateTime();
    }
    public DateTime getFinalTime(){
        return mEvent.getEnd().getDateTime();
    }
    public Date getStartTimeDate(){

        return new Date(getStartTime().getValue());
    }
    public Date getEndTimeDate(){
        return new Date(getFinalTime().getValue());
    }

    public String getEndTimeString(){
        if(mEvent==null) return "";
        return df.format(getEndTimeDate());
    }
    DateFormat df =         DateFormat.getTimeInstance();


    public String getTimeString(){
        try{

            if(mEvent==null){
                return "";
            }

            return df.format(getStartTimeDate())+" - "+df.format(getEndTimeDate());
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }

    public String getCurrentEventName() {
        if(getEventName().equals("None"))
            return "Room Available";
        return getEventName();
    }
}

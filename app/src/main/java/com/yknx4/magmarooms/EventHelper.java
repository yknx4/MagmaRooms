package com.yknx4.magmarooms;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.List;

/**
 * Created by yknx4 on 2/19/16.
 */
public class EventHelper {
    private static final String TAG = EventHelper.class.getSimpleName();
    private List<Event> events;

    public EventHelper(List<Event> events){

        this.events = events;
        for(Event event:events){
            try {
                Log.d(TAG,event.getSummary()+" - "+event.getStart().toPrettyString()+" - "+event.getEnd().toPrettyString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Event getCurrentEvent(){
//        DateTime now = new DateTime(System.currentTimeMillis());
        Long now = System.currentTimeMillis();
        long eventTimeStart, eventTimeEnd;
        for(Event event:events){
            eventTimeStart = event.getStart().getDateTime().getValue();
            eventTimeEnd = event.getEnd().getDateTime().getValue();
            if(eventTimeStart<now && eventTimeEnd>now)
                return event;
        }
        return null;
    }
    public Event getPreviousEvent(){
//        DateTime now = new DateTime(System.currentTimeMillis())
        if(getCurrentEvent()!=null){
            int index = events.indexOf(getCurrentEvent());
            index--;
            if(index>=0){
                return events.get(index);
            }
            else return null;
        }
        if(getNextEvent()!=null){
            int index = events.indexOf(getNextEvent());
            index--;
            if(index>=0){
                return events.get(index);
            }
        }
        return events.get(events.size()-1);
    }

    public Event getNextEvent(){
        if(getCurrentEvent()!=null){
            int index = events.indexOf(getCurrentEvent());
            index++;
            if(index<events.size()){
                return events.get(index);
            }
            else return null;
        }
        Long now = System.currentTimeMillis();
        Long differenceFromNow = Long.MAX_VALUE;
        Event temptResult= null;
        long eventTimeStart, eventTimeEnd;
        for(Event event:events){
            eventTimeEnd = event.getEnd().getDateTime().getValue();
            if(eventTimeEnd<now) continue;
            long currDiff = eventTimeEnd - now;
            if(currDiff<differenceFromNow){
                differenceFromNow = currDiff;
                temptResult = event;
            }
        }
        return temptResult;
    }

    public Event getLateEvent(){
        if(getNextEvent()==null){
            return null;
        }
        int index = events.indexOf(getNextEvent());
        index++;
        if(index<events.size()){
            return events.get(index);
        }
        return null;
    }

    public boolean hasCurrent(){
        return getCurrentEvent()!=null;
    }

    public boolean hasNext(){
        return getNextEvent()!=null;
    }
    public boolean hasLate(){
        return getLateEvent()!=null;
    }

}

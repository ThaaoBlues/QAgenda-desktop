package com.qsync.qagenda;

import java.util.Date;

public class Event implements Comparable<Event> {
    private Date startDate;
    private String summary;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public int compareTo(Event other) {
        return this.startDate.compareTo(other.startDate);
    }
}

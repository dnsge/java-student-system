package org.dnsge.studentsystem.sql.model;

import java.sql.Time;

public class Period {
    private final int id;
    private String display;
    private Time startTime;

    public Period(int id, String display, Time startTime) {
        this.id = id;
        this.display = display;
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
}

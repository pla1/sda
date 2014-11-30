package net.pla1.sda;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Schedule implements Comparable<Schedule> {

    private String programID;
    private String md5;
    private int duration;
    private String liveTapeDelay;
    private boolean newShowing = false;
    private String stationID;
    private Program program;
    private Station station;
    private Date airDateTime;

    public String getAirDateTimeDisplay() {
        return Utils.getDateDisplay(airDateTime);
    }

    public boolean isNowPlaying() {
        if (program == null) {
            Log.i(Utils.TAG, "isNowPlaying Program is null");
            return false;
        }
        if (airDateTime == null) {
            Log.i(Utils.TAG, "isNowPlaying airDateTime is null");
            return false;
        }
        Calendar end = GregorianCalendar.getInstance();
        end.setTime(airDateTime);
        end.add(Calendar.SECOND, duration);
        Date now = new Date();
        //   Log.i(Utils.TAG, "Now: " + now + " start: " + airDateTime + " end: " + end.getTime());
        if (now.after(airDateTime) && now.before(end.getTime())) {
            return true;
        }
        return false;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Date getAirDateTime() {
        return airDateTime;
    }

    public void setAirDateTime(Date airDateTime) {
        this.airDateTime = airDateTime;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(programID).append(" ");
        sb.append(md5).append(" ");
        sb.append(duration).append(" ");
        sb.append(liveTapeDelay).append(" ");
        sb.append(newShowing).append(" ");
        return sb.toString();
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getProgramID() {
        return programID;
    }

    public void setProgramID(String programID) {
        this.programID = programID;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLiveTapeDelay() {
        return liveTapeDelay;
    }

    public void setLiveTapeDelay(String liveTapeDelay) {
        this.liveTapeDelay = liveTapeDelay;
    }

    public boolean isNewShowing() {
        return newShowing;
    }

    public void setNewShowing(boolean newShowing) {
        this.newShowing = newShowing;
    }


    public Schedule() {
    }

    public int compareTo(Schedule schedule) {
        if (schedule != null && schedule.getAirDateTime() != null && airDateTime != null) {
            if (schedule.getAirDateTime().equals(airDateTime)) {
                return 0;
            }
            if (schedule.getAirDateTime().before(airDateTime)) {
                return 1;
            } else {
                return -1;
            }
        }
        return -1;
    }
}


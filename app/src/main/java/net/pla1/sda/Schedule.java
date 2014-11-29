package net.pla1.sda;

import java.util.Date;

public class Schedule {

    private String programID;
    private String md5;
    private Date airDateTime;
    private int duration;
    private String liveTapeDelay;
    private boolean newShowing = false;
    private String stationID;


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(programID).append(" ");
        sb.append(md5).append(" ");
        sb.append(airDateTime).append(" ");
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

    public Date getAirDateTime() {
        return airDateTime;
    }

    public void setAirDateTime(Date airDateTime) {
        this.airDateTime = airDateTime;
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


}


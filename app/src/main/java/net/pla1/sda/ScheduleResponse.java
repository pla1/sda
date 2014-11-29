package net.pla1.sda;

import java.util.ArrayList;

public class ScheduleResponse {
    private String stationID;
    private ArrayList<Schedule> programs;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stationID).append("\n");
        sb.append(programs.size() + " schedule rows\n");
        for (Schedule schedule : programs) {
            sb.append(schedule.toString()).append("\n");
        }
        return sb.toString();
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public ArrayList<Schedule> getPrograms() {
        return programs;
    }

    public void setPrograms(ArrayList<Schedule> schedules) {
        this.programs = schedules;
    }
}

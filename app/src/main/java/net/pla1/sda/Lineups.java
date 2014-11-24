package net.pla1.sda;

import java.util.ArrayList;
import java.util.Date;

public class Lineups {
    private String serverID;
    private Date datetime;
    private ArrayList<Lineup> lineups;

    public Lineups() {
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public ArrayList<Lineup> getLineups() {
        return lineups;
    }

    public void setLineups(ArrayList<Lineup> lineups) {
        this.lineups = lineups;
    }
}

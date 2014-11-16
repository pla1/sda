package net.pla1.sda;

import java.util.ArrayList;

public class Headend {
    private String type;
    private String location;
    private String name;
    private ArrayList<Lineup> lineups;


    public Headend() {

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(" ");
        sb.append(location).append(" ");
        sb.append(name).append(" ");
        if (lineups != null) {
            for (Lineup lineup : lineups) {
                sb.append(lineup.toString());
            }
        }
        return sb.toString();
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<Lineup> getLineups() {
        return lineups;
    }

    public void setLineups(ArrayList<Lineup> lineups) {
        this.lineups = lineups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

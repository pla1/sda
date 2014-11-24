package net.pla1.sda;

import java.util.ArrayList;
import java.util.HashMap;

public class StationResponse {
    private ArrayList<Station> stations;
    private ArrayList<ChannelMap> map;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stations.size() + " stations ");
        sb.append(map.size() + " maps");
        return sb.toString();
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public ArrayList<Station> getStationsWithChannelNumber() {
        HashMap<String, String> channelMapHashMap = new HashMap<String, String>();
        for (ChannelMap channelMap : map) {
            channelMapHashMap.put(channelMap.getStationID(), channelMap.getChannel());
        }
        for (Station station : stations) {
            station.setChannel(channelMapHashMap.get(station.getStationID()));
        }
        return stations;
    }

    public void setStations(ArrayList<Station> stations) {
        this.stations = stations;
    }


    public ArrayList<ChannelMap> getMap() {
        return map;
    }

    public void setMap(ArrayList<ChannelMap> map) {
        this.map = map;
    }
}

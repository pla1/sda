package net.pla1.sda;

public class ChannelMap {
    private String stationID;
    private String channel;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stationID).append(" ");
        sb.append(channel);
        return sb.toString();
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}

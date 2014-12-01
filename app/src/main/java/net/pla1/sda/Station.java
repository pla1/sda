package net.pla1.sda;

public class Station {
    private String callsign;
    private String name;
    private String broadcastLanguage;
    private String descriptionLanguage;
    private String stationID;
    private String channel;
    private Logo logo;
    private boolean subscribed = false;
    private String md5;

    public String getChannelDisplay() {
        if (channel == null) {
            return "";
        }
        return channel.replaceFirst("^0+(?!$)", "");
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getLogoUrl() {
        if (logo == null) {
            return null;
        } else {
            return logo.getURL();
        }

    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBroadcastLanguage() {
        return broadcastLanguage;
    }

    public void setBroadcastLanguage(String broadcastLanguage) {
        this.broadcastLanguage = broadcastLanguage;
    }

    public String getDescriptionLanguage() {
        return descriptionLanguage;
    }

    public void setDescriptionLanguage(String descriptionLanguage) {
        this.descriptionLanguage = descriptionLanguage;
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

    public Logo getLogo() {
        return logo;
    }

    public void setLogo(Logo logo) {
        this.logo = logo;
    }


}

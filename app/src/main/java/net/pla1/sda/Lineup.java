package net.pla1.sda;

import java.io.Serializable;
import java.util.Date;

public class Lineup implements Serializable {
    private String ID;
    private Date modified;
    private String uri;
    private String name;
    private String type;
    private String location;
    private boolean subscribed = false;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID).append(" ");
        if (name != null) {
            sb.append(name).append(" ");
        }
        sb.append(modified).append(" ");
        sb.append(uri).append(" ");
        return sb.toString();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean equals(Object object) {
        if (object != null && object instanceof Lineup) {
            Lineup lineup = (Lineup) object;
            if (lineup.getUri() != null && uri != null && lineup.getUri().equals(uri)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}

package net.pla1.sda;

import java.util.Date;

public class Lineup {
    private String ID;
    private Date modified;
    private String uri;
    private String name;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID).append(" ");
        sb.append(name).append(" ");
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
}

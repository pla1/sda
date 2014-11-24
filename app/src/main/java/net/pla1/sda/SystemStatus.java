package net.pla1.sda;

import java.util.Date;

public class SystemStatus {
    private Date date;
    private String status;
    private String details;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System status date: ");
        sb.append(date).append("\nSystem status: ");
        sb.append(status);
        if (details != null) {
            sb.append("\nSystem status details: ").append(details);
        }
        return sb.toString();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

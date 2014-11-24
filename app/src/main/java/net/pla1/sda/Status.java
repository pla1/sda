package net.pla1.sda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Status implements Serializable {
    private Account account;
    private ArrayList<Lineup> lineups;
    private Date lastDataUpdate;
    private String[] notifications;
    private ArrayList<SystemStatus> systemStatus;
    private String serverID;
    private int code;

    public Status() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Last update: ");
        sb.append(lastDataUpdate).append("\nServer ID: ");
        sb.append(serverID).append("\nStatus code: ");
        sb.append(code).append(" ");
        if (systemStatus != null && systemStatus.size() > 0) {
            for (SystemStatus ss : systemStatus) {
                sb.append("\n\n");
                sb.append(ss);
            }
        }
        if (account != null) {
            sb.append("\n");
            sb.append(account.toString()).append(" ");
        }
        if (lineups != null) {
            sb.append("\nLine up quantity: ").append(lineups.size());
            for (Lineup lineup : lineups) {
                sb.append("\n\n");
                sb.append(lineup.toString());
            }
        }
        return sb.toString();
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ArrayList<Lineup> getLineups() {
        return lineups;
    }

    public void setLineups(ArrayList<Lineup> lineups) {
        this.lineups = lineups;
    }

    public Date getLastDataUpdate() {
        return lastDataUpdate;
    }

    public void setLastDataUpdate(Date lastDataUpdate) {
        this.lastDataUpdate = lastDataUpdate;
    }

    public String[] getNotifications() {
        return notifications;
    }

    public void setNotifications(String[] notifications) {
        this.notifications = notifications;
    }

    public ArrayList<SystemStatus> getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(ArrayList<SystemStatus> systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

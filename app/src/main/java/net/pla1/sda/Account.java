package net.pla1.sda;

import java.io.Serializable;
import java.util.Date;

public class Account implements Serializable{
    private Date expires;
    private String[] messages;
    private int maxLineups;
    private Date nextSuggestedConnectTime;

    public Account() {

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expires).append(" ");
        sb.append(maxLineups).append(" ");
        sb.append(nextSuggestedConnectTime).append(" ");
        sb.append(" message quantity: ").append(messages.length);
        for (String message : messages) {
            sb.append("\n").append(message);
        }
        return sb.toString();
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public int getMaxLineups() {
        return maxLineups;
    }

    public void setMaxLineups(int maxLineups) {
        this.maxLineups = maxLineups;
    }

    public Date getNextSuggestedConnectTime() {
        return nextSuggestedConnectTime;
    }

    public void setNextSuggestedConnectTime(Date nextSuggestedConnectTime) {
        this.nextSuggestedConnectTime = nextSuggestedConnectTime;
    }
}

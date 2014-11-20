package net.pla1.sda;

import java.io.Serializable;

public class Token implements Serializable {
    private String token;
    private int code;
    private String serverID;
    private String message;

    public Token() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(code).append(" ");
        sb.append(message).append(" ");
        sb.append(serverID).append(" ");
        sb.append(token);
        return sb.toString();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

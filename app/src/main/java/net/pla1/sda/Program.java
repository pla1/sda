package net.pla1.sda;

import java.util.Date;

public class Program {
    private String programID;
    private Date originalAirDate;
    private String episodeTitle150;
    private String showType;
    private String md5;

    public String getProgramID() {
        return programID;
    }

    public void setProgramID(String programID) {
        this.programID = programID;
    }

    public Date getOriginalAirDate() {
        return originalAirDate;
    }

    public void setOriginalAirDate(Date originalAirDate) {
        this.originalAirDate = originalAirDate;
    }

    public String getEpisodeTitle150() {
        return episodeTitle150;
    }

    public void setEpisodeTitle150(String episodeTitle150) {
        this.episodeTitle150 = episodeTitle150;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}

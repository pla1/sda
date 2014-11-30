package net.pla1.sda;

import java.util.ArrayList;

public class Program {
    private String programID;
    private String episodeTitle150;
    private String title120;
    private String showType;
    private String md5;
    private boolean hasImageArtwork;
    private int duration;
    private boolean found = false;
    private String description;
    private ArrayList<Cast> cast;
    private ArrayList<Crew> crew;
    private String genres;
    private String originalAirDate;

    public String getOriginalAirDate() {
        return originalAirDate;
    }

    public void setOriginalAirDate(String originalAirDate) {
        this.originalAirDate = originalAirDate;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getCastAndCrewDisplay() {
        StringBuilder sb = new StringBuilder();
        if (!cast.isEmpty()) {
            sb.append("\nCAST\n");
            for (Cast c : cast) {
                sb.append(c.getRole()).append(": ");
                sb.append(c.getName()).append("\n");
            }
        }
        if (!crew.isEmpty()) {
            sb.append("\n\nCREW\n");
            for (Crew c : crew) {
                sb.append(c.getRole()).append(": ");
                sb.append(c.getName()).append("\n");
            }
        }
        return sb.toString();
    }

    public ArrayList<Cast> getCast() {
        return cast;
    }

    public void setCast(ArrayList<Cast> cast) {
        this.cast = cast;
    }

    public ArrayList<Crew> getCrew() {
        return crew;
    }

    public void setCrew(ArrayList<Crew> crew) {
        this.crew = crew;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isHasImageArtwork() {
        return hasImageArtwork;
    }

    public void setHasImageArtwork(boolean hasImageArtwork) {
        this.hasImageArtwork = hasImageArtwork;
    }

    public String getProgramID() {
        return programID;
    }

    public String getTitle120() {
        return title120;
    }

    public void setTitle120(String title120) {
        this.title120 = title120;
    }

    public void setProgramID(String programID) {
        this.programID = programID;
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

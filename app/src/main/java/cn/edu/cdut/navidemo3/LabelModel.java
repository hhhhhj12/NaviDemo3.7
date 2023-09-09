package cn.edu.cdut.navidemo3;

public class LabelModel {
    private Integer id;
    private String startTime,endTime,action,position,audio;

    public LabelModel(Integer id, String startTime, String endTime, String action, String position, String audio) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.action = action;
        this.position = position;
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "LabelModel{" +
                "id=" + id +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", action='" + action + '\'' +
                ", position='" + position + '\'' +
                ", audio='" + audio + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}

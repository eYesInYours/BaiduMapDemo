package com.example.demobaidumap.alarmlist;

public class AlarmData {
    private String title;
    private String time;
    private boolean isEnabled;

    public AlarmData(String title, String time, boolean isEnabled) {
        this.title = title;
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}

package com.smartattend.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceHistory {
    @SerializedName("records")
    private List<AttendanceRecord> records;

    @SerializedName("percentage")
    private double percentage;

    @SerializedName("totalSessions")
    private int totalSessions;

    @SerializedName("presentCount")
    private int presentCount;

    @SerializedName("absentCount")
    private int absentCount;

    @SerializedName("lateCount")
    private int lateCount;

    public AttendanceHistory() {}

    public List<AttendanceRecord> getRecords() { return records; }
    public void setRecords(List<AttendanceRecord> records) { this.records = records; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }
}

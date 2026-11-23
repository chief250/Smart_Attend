package com.smartattend.app.model;

import com.google.gson.annotations.SerializedName;

public class AttendanceSession {
    @SerializedName("id")
    private long id;

    @SerializedName("courseId")
    private long courseId;

    @SerializedName("lecturerId")
    private long lecturerId;

    @SerializedName("sessionDate")
    private String sessionDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("status")
    private String status; // ACTIVE, CLOSED, UPCOMING

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("courseCode")
    private String courseCode;

    @SerializedName("qrToken")
    private String qrToken;

    @SerializedName("attended")
    private boolean attended;

    @SerializedName("attendanceStatus")
    private String attendanceStatus;

    public AttendanceSession() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public long getLecturerId() { return lecturerId; }
    public void setLecturerId(long lecturerId) { this.lecturerId = lecturerId; }

    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
}

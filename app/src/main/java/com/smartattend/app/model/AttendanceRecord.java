package com.smartattend.app.model;

import com.google.gson.annotations.SerializedName;

public class AttendanceRecord {
    @SerializedName("id")
    private long id;

    @SerializedName("studentId")
    private long studentId;

    @SerializedName("sessionId")
    private long sessionId;

    @SerializedName("checkInTime")
    private String checkInTime;

    @SerializedName("status")
    private String status; // PRESENT, LATE, ABSENT

    @SerializedName("verificationStatus")
    private String verificationStatus; // VERIFIED, UNVERIFIED

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("courseCode")
    private String courseCode;

    @SerializedName("sessionDate")
    private String sessionDate;

    public AttendanceRecord() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }
}

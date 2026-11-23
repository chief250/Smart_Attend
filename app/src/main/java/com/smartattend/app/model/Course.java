package com.smartattend.app.model;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("courseId")
    private long id;

    @SerializedName("courseCode")
    private String courseCode;

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("lecturerId")
    private long lecturerId;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("attendancePercentage")
    private double attendancePercentage;

    public Course() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public long getLecturerId() { return lecturerId; }
    public void setLecturerId(long lecturerId) { this.lecturerId = lecturerId; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
}

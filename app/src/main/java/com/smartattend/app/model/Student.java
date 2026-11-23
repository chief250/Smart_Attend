package com.smartattend.app.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("studentNumber")
    private String studentNumber;

    @SerializedName("program")
    private String program;

    @SerializedName("classGroup")
    private String classGroup;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    public Student() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getClassGroup() { return classGroup; }
    public void setClassGroup(String classGroup) { this.classGroup = classGroup; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

package com.smartattend.app.model.request;

public class SignUpRequest {
    private String fullName;
    private String studentNumber;
    private String email;
    private String password;
    private String program;
    private String classGroup;
    private String faceTemplate;

    public SignUpRequest() {
    }

    public SignUpRequest(String fullName, String studentNumber, String email, String password,
                         String program, String classGroup, String faceTemplate) {
        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.email = email;
        this.password = password;
        this.program = program;
        this.classGroup = classGroup;
        this.faceTemplate = faceTemplate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(String classGroup) {
        this.classGroup = classGroup;
    }

    public String getFaceTemplate() {
        return faceTemplate;
    }

    public void setFaceTemplate(String faceTemplate) {
        this.faceTemplate = faceTemplate;
    }
}

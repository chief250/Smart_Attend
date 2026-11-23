package com.smartattend.app.model.request;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("qrToken")
    private String qrToken;

    @SerializedName("faceVerified")
    private boolean faceVerified;

    @SerializedName("studentId")
    private long studentId;

    public CheckInRequest(String qrToken, boolean faceVerified, long studentId) {
        this.qrToken = qrToken;
        this.faceVerified = faceVerified;
        this.studentId = studentId;
    }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isFaceVerified() { return faceVerified; }
    public void setFaceVerified(boolean faceVerified) { this.faceVerified = faceVerified; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
}

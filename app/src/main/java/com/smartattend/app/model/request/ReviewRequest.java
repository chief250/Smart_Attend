package com.smartattend.app.model.request;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("attendanceId")
    private long attendanceId;

    @SerializedName("reason")
    private String reason;

    public ReviewRequest(long attendanceId, String reason) {
        this.attendanceId = attendanceId;
        this.reason = reason;
    }

    public long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(long attendanceId) { this.attendanceId = attendanceId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

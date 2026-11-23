package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.AttendanceSession;
import com.smartattend.app.model.request.CheckInRequest;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.DateUtils;
import com.smartattend.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceConfirmationActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView ivStatusIcon;
    private TextView tvStatusMessage, tvCourseName, tvCheckInTime, tvStatus, tvVerification;
    private MaterialButton btnDone;
    private View layoutSuccess, layoutError;
    private TextView tvErrorMessage;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String qrToken;
    private boolean faceVerified;
    private AttendanceSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_confirmation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.attendance_confirmation);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        qrToken = getIntent().getStringExtra(Constants.EXTRA_QR_TOKEN);
        faceVerified = getIntent().getBooleanExtra(Constants.EXTRA_FACE_VERIFIED, false);
        String sessionJson = getIntent().getStringExtra(Constants.EXTRA_SESSION);
        if (sessionJson != null) {
            session = new Gson().fromJson(sessionJson, AttendanceSession.class);
        }

        initViews();
        performCheckIn();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCheckInTime = findViewById(R.id.tvCheckInTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvVerification = findViewById(R.id.tvVerification);
        btnDone = findViewById(R.id.btnDone);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        btnDone.setOnClickListener(v -> navigateToDashboard());
        layoutSuccess.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void performCheckIn() {
        progressBar.setVisibility(View.VISIBLE);

        long studentId = sessionManager.getStudentId();
        CheckInRequest request = new CheckInRequest(qrToken, faceVerified, studentId);

        apiService.checkIn(request).enqueue(new Callback<ApiResponse<AttendanceRecord>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceRecord>> call,
                                   Response<ApiResponse<AttendanceRecord>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AttendanceRecord> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        showSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : getString(R.string.error_check_in_failed));
                    }
                } else {
                    String errorMsg = getString(R.string.error_check_in_failed);
                    try {
                        if (response.errorBody() != null) {
                            String errJson = response.errorBody().string();
                            com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(errJson).getAsJsonObject();
                            if (obj.has("message")) {
                                errorMsg = obj.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceRecord>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Could not reach backend server: " + t.getMessage() + ". Check server IP and Wi-Fi connection.");
            }
        });
    }

    private void showSuccess(AttendanceRecord record) {
        layoutSuccess.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        ivStatusIcon.setImageResource(R.drawable.ic_check_success);

        String courseName = record.getCourseName() != null
                ? record.getCourseName()
                : (session != null ? session.getCourseName() : "");
        tvCourseName.setText(courseName);
        tvCheckInTime.setText(DateUtils.formatDateTime(record.getCheckInTime()));

        String status = record.getStatus();
        tvStatus.setText(status);
        if (Constants.STATUS_PRESENT.equals(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.color_success, getTheme()));
        } else if (Constants.STATUS_LATE.equals(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.color_warning, getTheme()));
        }

        tvVerification.setText(faceVerified
                ? getString(R.string.face_verified)
                : getString(R.string.face_not_verified));
    }

    private void showError(String message) {
        layoutError.setVisibility(View.VISIBLE);
        layoutSuccess.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToDashboard();
    }
}

package com.smartattend.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.request.ReviewRequest;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.DateUtils;
import com.smartattend.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceDetailsActivity extends AppCompatActivity {

    private TextView tvCourseName, tvCourseCode, tvDate, tvCheckInTime, tvStatus, tvVerification;
    private MaterialButton btnReportProblem;

    private ApiService apiService;
    private SessionManager sessionManager;
    private AttendanceRecord record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.attendance_details);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        String recordJson = getIntent().getStringExtra(Constants.EXTRA_ATTENDANCE_RECORD);
        if (recordJson != null) {
            record = new Gson().fromJson(recordJson, AttendanceRecord.class);
        }

        initViews();
        populateData();
    }

    private void initViews() {
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseCode = findViewById(R.id.tvCourseCode);
        tvDate = findViewById(R.id.tvDate);
        tvCheckInTime = findViewById(R.id.tvCheckInTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvVerification = findViewById(R.id.tvVerification);
        btnReportProblem = findViewById(R.id.btnReportProblem);

        btnReportProblem.setOnClickListener(v -> showReviewDialog());
    }

    private void populateData() {
        if (record == null) return;

        tvCourseName.setText(record.getCourseName() != null ? record.getCourseName() : "N/A");
        tvCourseCode.setText(record.getCourseCode() != null ? record.getCourseCode() : "N/A");
        String displayDate = record.getSessionDate() != null
                ? record.getSessionDate() : record.getCheckInTime();
        tvDate.setText(DateUtils.formatDate(displayDate));
        tvCheckInTime.setText(DateUtils.formatDateTime(record.getCheckInTime()));
        tvStatus.setText(record.getStatus() != null ? record.getStatus() : "N/A");
        tvVerification.setText(record.getVerificationStatus() != null
                ? record.getVerificationStatus() : "N/A");

        // Color status
        if (record.getStatus() != null) {
            int color;
            switch (record.getStatus()) {
                case Constants.STATUS_PRESENT:
                    color = getResources().getColor(R.color.color_success, getTheme());
                    break;
                case Constants.STATUS_LATE:
                    color = getResources().getColor(R.color.color_warning, getTheme());
                    break;
                default:
                    color = getResources().getColor(R.color.color_error, getTheme());
                    break;
            }
            tvStatus.setTextColor(color);
        }
    }

    private void showReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        EditText etReason = dialogView.findViewById(R.id.etReason);

        new AlertDialog.Builder(this)
                .setTitle(R.string.report_problem)
                .setView(dialogView)
                .setPositiveButton(R.string.submit, (dialog, which) -> {
                    String reason = etReason.getText() != null
                            ? etReason.getText().toString().trim() : "";
                    if (reason.isEmpty()) {
                        Toast.makeText(this, R.string.error_reason_required, Toast.LENGTH_SHORT).show();
                    } else {
                        submitReview(reason);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitReview(String reason) {
        if (record == null) return;
        ReviewRequest request = new ReviewRequest(record.getId(), reason);
        apiService.submitReview(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    Snackbar.make(findViewById(android.R.id.content),
                            R.string.review_submitted, Snackbar.LENGTH_LONG).show();
                } else {
                    showSnackbar(getString(R.string.error_submit_review));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showSnackbar(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

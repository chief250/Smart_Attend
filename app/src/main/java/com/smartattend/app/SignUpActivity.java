package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.request.SignUpRequest;
import com.smartattend.app.model.request.LoginRequest;
import com.smartattend.app.model.Student;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.model.response.LoginResponse;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private static final int FACE_ENROLLMENT_REQUEST = 200;

    private TextInputLayout tilFullName, tilStudentNumber, tilEmail, tilProgram, tilClassGroup, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etStudentNumber, etEmail, etProgram, etClassGroup, etPassword, etConfirmPassword;
    private TextView tvFaceStatus, tvSignInLink;
    private MaterialButton btnCaptureFace, btnRegister;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private ApiService apiService;
    private String enrolledFaceTemplate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilStudentNumber = findViewById(R.id.tilStudentNumber);
        tilEmail = findViewById(R.id.tilEmail);
        tilProgram = findViewById(R.id.tilProgram);
        tilClassGroup = findViewById(R.id.tilClassGroup);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etStudentNumber = findViewById(R.id.etStudentNumber);
        etEmail = findViewById(R.id.etEmail);
        etProgram = findViewById(R.id.etProgram);
        etClassGroup = findViewById(R.id.etClassGroup);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tvFaceStatus = findViewById(R.id.tvFaceStatus);
        tvSignInLink = findViewById(R.id.tvSignInLink);
        btnCaptureFace = findViewById(R.id.btnCaptureFace);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnCaptureFace.setOnClickListener(v -> {
            Intent intent = new Intent(this, FaceEnrollmentActivity.class);
            startActivityForResult(intent, FACE_ENROLLMENT_REQUEST);
        });

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvSignInLink.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FACE_ENROLLMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            enrolledFaceTemplate = data.getStringExtra(Constants.EXTRA_FACE_TEMPLATE);
            if (enrolledFaceTemplate != null && !enrolledFaceTemplate.isEmpty()) {
                tvFaceStatus.setText("Reference Face Enrolled ✓");
                tvFaceStatus.setTextColor(getResources().getColor(R.color.color_success, getTheme()));
                btnCaptureFace.setText("Re-capture Reference Face 📷");
                Toast.makeText(this, "Reference face template saved!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void attemptRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String studentNumber = etStudentNumber.getText() != null ? etStudentNumber.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String program = etProgram.getText() != null ? etProgram.getText().toString().trim() : "";
        String classGroup = etClassGroup.getText() != null ? etClassGroup.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean valid = true;
        tilFullName.setError(null);
        tilStudentNumber.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            valid = false;
        }

        if (TextUtils.isEmpty(studentNumber)) {
            tilStudentNumber.setError("Student ID is required");
            valid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            valid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }

        if (!valid) return;

        if (enrolledFaceTemplate == null || enrolledFaceTemplate.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please capture your reference face photo before registering.",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        SignUpRequest request = new SignUpRequest(fullName, studentNumber, email, password, program, classGroup, enrolledFaceTemplate);

        apiService.register(request, studentNumber, program, classGroup).enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Student createdStudent = response.body().getData();
                    signInAndCompleteRegistration(email, password, createdStudent, enrolledFaceTemplate);
                } else {
                    setLoading(false);
                    String message = null;
                    if (response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                    if (message == null || message.isEmpty()) {
                        message = "Registration failed. Email or Student ID may already be in use.";
                    }
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                setLoading(false);
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.error_network) + ": " + t.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void signInAndCompleteRegistration(String email, String password, Student createdStudent, String faceTemplate) {
        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                String token = "registered_token_" + System.currentTimeMillis();
                long userId = createdStudent != null ? createdStudent.getUserId() : System.currentTimeMillis();
                long studentId = createdStudent != null ? createdStudent.getId() : System.currentTimeMillis();

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponse login = response.body().getData();
                    token = login.getToken();
                    if (login.getUserId() > 0) userId = login.getUserId();
                    if (login.getStudentId() > 0) studentId = login.getStudentId();
                }

                sessionManager.saveSession(token, userId, Constants.ROLE_STUDENT,
                        createdStudent != null && createdStudent.getFullName() != null ? createdStudent.getFullName() : etFullName.getText().toString(),
                        studentId,
                        createdStudent != null && createdStudent.getStudentNumber() != null ? createdStudent.getStudentNumber() : etStudentNumber.getText().toString(),
                        email,
                        createdStudent != null && createdStudent.getProgram() != null ? createdStudent.getProgram() : etProgram.getText().toString(),
                        createdStudent != null && createdStudent.getClassGroup() != null ? createdStudent.getClassGroup() : etClassGroup.getText().toString());

                sessionManager.saveFaceTemplate(email, faceTemplate);
                ApiClient.reset();
                ApiClient.init(sessionManager);

                Toast.makeText(SignUpActivity.this, "Account Created & Registered in Database! ✓", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SignUpActivity.this, StudentDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                sessionManager.saveSession("registered_token_" + System.currentTimeMillis(),
                        createdStudent != null ? createdStudent.getUserId() : System.currentTimeMillis(),
                        Constants.ROLE_STUDENT, etFullName.getText().toString(),
                        createdStudent != null ? createdStudent.getId() : System.currentTimeMillis(),
                        etStudentNumber.getText().toString(), email,
                        etProgram.getText().toString(), etClassGroup.getText().toString());
                sessionManager.saveFaceTemplate(email, faceTemplate);

                Toast.makeText(SignUpActivity.this, "Account Registered Successfully! ✓", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this, StudentDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showProfileError() {
        Snackbar.make(findViewById(android.R.id.content),
                "Account created, but the student profile could not be loaded. Please sign in again.",
                Snackbar.LENGTH_LONG).show();
    }

    private void openDashboard() {
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnCaptureFace.setEnabled(!loading);
    }
}

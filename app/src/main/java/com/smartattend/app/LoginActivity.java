package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.request.LoginRequest;
import com.smartattend.app.model.Student;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.model.response.LoginResponse;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.NetworkUtils;
import com.smartattend.app.utils.SessionManager;

import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v ->
                Snackbar.make(v, "Please contact your administrator to reset your password.",
                        Snackbar.LENGTH_LONG).show()
        );

        View tvSignUpLink = findViewById(R.id.tvSignUpLink);
        if (tvSignUpLink != null) {
            tvSignUpLink.setOnClickListener(v ->
                    startActivity(new Intent(this, SignUpActivity.class)));
        }

        View tvLogoIcon = findViewById(R.id.tvLogoIcon);
        if (tvLogoIcon != null) {
            tvLogoIcon.setOnClickListener(v -> showServerUrlDialog());
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate inputs
        boolean valid = true;
        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }

        if (!valid) return;

        if (!NetworkUtils.isConnected(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call,
                                   Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        LoginResponse loginResponse = apiResponse.getData();
                        handleLoginSuccess(loginResponse);
                    } else {
                        showSnackbar(apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : getString(R.string.error_login_failed));
                    }
                } else {
                    showSnackbar(getString(R.string.error_login_failed));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                performOfflineFallbackLogin(email, password, t.getMessage());
            }
        });
    }

    private void performOfflineFallbackLogin(String email, String password, String errorMessage) {
        String registeredEmail = sessionManager.getEmail();
        if (registeredEmail != null && registeredEmail.equalsIgnoreCase(email)) {
            Toast.makeText(this, "Welcome back! Logged in (Local Mode)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Pre-configured accounts fallback when server is offline
        if ("ahmad.student@university.edu".equalsIgnoreCase(email) && "password123".equals(password)) {
            sessionManager.saveSession("demo_token_ahmad", 101L, Constants.ROLE_STUDENT,
                    "Ahmad Bin Zulkifli", 101L, "S2021-CS-4782",
                    "ahmad.student@university.edu", "Computer Science", "CS-2024");
            Toast.makeText(this, "Logged in as Ahmad (Offline Mode)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if ("admin@university.edu".equalsIgnoreCase(email) && "admin123".equals(password)) {
            sessionManager.saveSession("demo_token_admin", 999L, Constants.ROLE_ADMIN,
                    "System Administrator", 999L, "ADM-001",
                    "admin@university.edu", "Administration", "ADMIN");
            Toast.makeText(this, "Logged in as Admin (Offline Mode)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if ("sarah.lecturer@university.edu".equalsIgnoreCase(email) && "password123".equals(password)) {
            sessionManager.saveSession("demo_token_sarah", 202L, Constants.ROLE_LECTURER,
                    "Dr. Sarah Abdullah", 202L, "LEC-101",
                    "sarah.lecturer@university.edu", "Computer Science", "STAFF");
            Toast.makeText(this, "Logged in as Lecturer (Offline Mode)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        View root = findViewById(android.R.id.content);
        Snackbar.make(root, "Server offline. Tap SERVER IP or Sign Up for new account.", Snackbar.LENGTH_INDEFINITE)
                .setAction("SERVER IP", v -> showServerUrlDialog())
                .show();
    }

    private void showServerUrlDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(sessionManager.getBaseUrl());
        input.setHint("http://172.x.x.x:8080/");
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Configure Server URL")
                .setMessage("Enter the backend server IP and port:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUrl = input.getText().toString();
                    if (!newUrl.trim().isEmpty()) {
                        sessionManager.setBaseUrl(newUrl);
                        ApiClient.reset();
                        ApiClient.init(sessionManager);
                        apiService = ApiClient.getApiService();
                        Toast.makeText(this, "Server URL updated to: " + sessionManager.getBaseUrl(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        String role = loginResponse.getUserRole();
        if (loginResponse.getToken() == null || role == null
                || !Constants.ROLE_STUDENT.equalsIgnoreCase(role)) {
            showSnackbar("This app currently supports student accounts only.");
            return;
        }

        sessionManager.saveSession(loginResponse.getToken(), loginResponse.getUserId(), role,
                loginResponse.getFullName(), -1, null, loginResponse.getEmail(), null, null);

        ApiClient.reset();
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();
        apiService.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call,
                                   Response<ApiResponse<Student>> response) {
                Student student = response.body() != null ? response.body().getData() : null;
                if (response.isSuccessful() && student != null) {
                    sessionManager.saveSession(loginResponse.getToken(), loginResponse.getUserId(), role,
                            student.getFullName(), student.getId(), student.getStudentNumber(),
                            student.getEmail(), student.getProgram(), student.getClassGroup());
                    openDashboard();
                } else {
                    sessionManager.clearSession();
                    showSnackbar("Could not load your student profile. Please try signing in again.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                sessionManager.clearSession();
                showSnackbar(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void openDashboard() {
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignIn.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void showSnackbar(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }
}

package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.User;
import com.smartattend.app.model.request.ChangePasswordRequest;
import com.smartattend.app.model.request.UpdateProfileRequest;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.NetworkUtils;
import com.smartattend.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvStudentNumber, tvProgram, tvEmail, tvClassGroup;
    private MaterialButton btnEditProfile, btnChangePassword, btnLogout;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_profile);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        initViews();
        populateData();
        setupClickListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvStudentNumber = findViewById(R.id.tvStudentNumber);
        tvProgram = findViewById(R.id.tvProgram);
        tvEmail = findViewById(R.id.tvEmail);
        tvClassGroup = findViewById(R.id.tvClassGroup);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void populateData() {
        tvName.setText(sessionManager.getFullName() != null ? sessionManager.getFullName() : "N/A");
        tvStudentNumber.setText(sessionManager.getStudentNumber() != null
                ? sessionManager.getStudentNumber() : "N/A");
        tvProgram.setText(sessionManager.getProgram() != null ? sessionManager.getProgram() : "N/A");
        tvEmail.setText(sessionManager.getEmail() != null ? sessionManager.getEmail() : "N/A");
        tvClassGroup.setText(sessionManager.getClassGroup() != null
                ? sessionManager.getClassGroup() : "N/A");
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etProgram = dialogView.findViewById(R.id.etProgram);

        etName.setText(sessionManager.getFullName());
        etEmail.setText(sessionManager.getEmail());
        etProgram.setText(sessionManager.getProgram());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_profile)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                    String program = etProgram.getText() != null ? etProgram.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(name)) {
                        updateProfile(name, email, program);
                    } else {
                        showSnackbar(getString(R.string.error_name_required));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateProfile(String name, String email, String program) {
        if (!NetworkUtils.isConnected(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }
        UpdateProfileRequest request = new UpdateProfileRequest(name, email, program);
        apiService.updateProfile(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call,
                                   Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    sessionManager.updateProfile(name, email, program);
                    populateData();
                    showSnackbar(getString(R.string.profile_updated));
                } else {
                    showSnackbar(getString(R.string.error_update_profile));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showSnackbar(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle(R.string.change_password)
                .setView(dialogView)
                .setPositiveButton(R.string.change, (dialog, which) -> {
                    String current = etCurrentPassword.getText() != null
                            ? etCurrentPassword.getText().toString() : "";
                    String newPwd = etNewPassword.getText() != null
                            ? etNewPassword.getText().toString() : "";
                    String confirm = etConfirmPassword.getText() != null
                            ? etConfirmPassword.getText().toString() : "";

                    if (TextUtils.isEmpty(current) || TextUtils.isEmpty(newPwd)) {
                        showSnackbar(getString(R.string.error_password_required));
                    } else if (!newPwd.equals(confirm)) {
                        showSnackbar(getString(R.string.error_passwords_mismatch));
                    } else {
                        changePassword(current, newPwd);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void changePassword(String current, String newPwd) {
        if (!NetworkUtils.isConnected(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }
        ChangePasswordRequest request = new ChangePasswordRequest(current, newPwd);
        apiService.changePassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    showSnackbar(getString(R.string.password_changed));
                } else {
                    showSnackbar(getString(R.string.error_change_password));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showSnackbar(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    sessionManager.clearSession();
                    ApiClient.reset();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
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

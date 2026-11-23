package com.smartattend.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.smartattend.app.HelpActivity;
import com.smartattend.app.LoginActivity;
import com.smartattend.app.R;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.Student;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileStudentId, tvProfileProgram, tvProfileSemester;
    private MaterialButton btnHelp, btnLogout;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getApiService();

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileStudentId = view.findViewById(R.id.tvProfileStudentId);
        tvProfileProgram = view.findViewById(R.id.tvProfileProgram);
        tvProfileSemester = view.findViewById(R.id.tvProfileSemester);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnLogout = view.findViewById(R.id.btnLogout);

        populateProfile();

        btnHelp.setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void populateProfile() {
        tvProfileName.setText(sessionManager.getFullName());
        tvProfileEmail.setText(sessionManager.getEmail());

        apiService.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Student s = response.body().getData();
                    tvProfileName.setText(s.getFullName());
                    tvProfileEmail.setText(s.getEmail());
                    tvProfileStudentId.setText(s.getStudentNumber());
                    tvProfileProgram.setText(s.getProgram());
                    tvProfileSemester.setText("Semester 1 • 2026");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                // Keep local session info
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}

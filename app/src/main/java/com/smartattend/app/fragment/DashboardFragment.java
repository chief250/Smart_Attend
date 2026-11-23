package com.smartattend.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartattend.app.AttendanceHistoryActivity;
import com.smartattend.app.QRScannerActivity;
import com.smartattend.app.R;
import com.smartattend.app.adapter.TodaySessionAdapter;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.AttendanceSession;
import com.smartattend.app.model.Student;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvStudentName, tvStudentId, tvAttendancePercentage, tvTotalSessions, tvPresentSessions, tvAbsentSessions;
    private MaterialButton btnScanQr, btnViewHistory;
    private RecyclerView rvTodayClasses;
    private TodaySessionAdapter sessionAdapter;
    private List<AttendanceSession> todaySessions = new ArrayList<>();
    private SessionManager sessionManager;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getApiService();

        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
        tvTotalSessions = view.findViewById(R.id.tvTotalSessions);
        tvPresentSessions = view.findViewById(R.id.tvPresentSessions);
        tvAbsentSessions = view.findViewById(R.id.tvAbsentSessions);
        btnScanQr = view.findViewById(R.id.btnScanQr);
        btnViewHistory = view.findViewById(R.id.btnViewHistory);
        rvTodayClasses = view.findViewById(R.id.rvTodayClasses);

        rvTodayClasses.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        sessionAdapter = new TodaySessionAdapter(requireContext(), todaySessions, session -> {
            Intent intent = new Intent(requireContext(), QRScannerActivity.class);
            startActivity(intent);
        });
        rvTodayClasses.setAdapter(sessionAdapter);

        btnScanQr.setOnClickListener(v -> startActivity(new Intent(requireContext(), QRScannerActivity.class)));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(requireContext(), AttendanceHistoryActivity.class)));

        loadStudentData();
        loadTodaySessions();

        return view;
    }

    private void loadStudentData() {
        tvStudentName.setText(sessionManager.getFullName());
        tvStudentId.setText(sessionManager.getStudentNumber() != null ? sessionManager.getStudentNumber() : "S2021-CS-4891");

        apiService.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Student s = response.body().getData();
                    tvStudentName.setText(s.getFullName());
                    tvStudentId.setText("ID: " + s.getStudentNumber() + " • " + s.getProgram());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                // Keep default/cached values
            }
        });
    }

    private void loadTodaySessions() {
        apiService.getTodaySessions().enqueue(new Callback<ApiResponse<List<AttendanceSession>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceSession>>> call, Response<ApiResponse<List<AttendanceSession>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    todaySessions.clear();
                    todaySessions.addAll(response.body().getData());
                    sessionAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceSession>>> call, Throwable t) {
                // Offline fallback
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStudentData();
        loadTodaySessions();
    }
}

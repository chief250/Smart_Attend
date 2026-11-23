package com.smartattend.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.smartattend.app.R;
import com.smartattend.app.adapter.AttendanceAdapter;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.database.AttendanceDao;
import com.smartattend.app.database.DatabaseHelper;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView rvAttendance;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvOverallPercentage, tvPresentCount, tvAbsentCount, tvLateCount;
    private AttendanceAdapter adapter;
    private List<AttendanceRecord> records = new ArrayList<>();
    private ApiService apiService;
    private AttendanceDao attendanceDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        apiService = ApiClient.getApiService();
        attendanceDao = new AttendanceDao(DatabaseHelper.getInstance(requireContext()));

        rvAttendance = view.findViewById(R.id.rvAttendance);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvOverallPercentage = view.findViewById(R.id.tvOverallPercentage);
        tvPresentCount = view.findViewById(R.id.tvPresentCount);
        tvAbsentCount = view.findViewById(R.id.tvAbsentCount);
        tvLateCount = view.findViewById(R.id.tvLateCount);

        rvAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AttendanceAdapter(requireContext(), records);
        rvAttendance.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchHistory);

        // Load offline cached attendance
        List<AttendanceRecord> cached = attendanceDao.getAllRecords();
        if (cached != null && !cached.isEmpty()) {
            records.clear();
            records.addAll(cached);
            adapter.notifyDataSetChanged();
        }

        fetchHistory();
        return view;
    }

    private void fetchHistory() {
        swipeRefresh.setRefreshing(true);
        apiService.getAttendanceHistory().enqueue(new Callback<ApiResponse<List<AttendanceRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceRecord>>> call,
                                   Response<ApiResponse<List<AttendanceRecord>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AttendanceRecord> latest = response.body().getData();
                    tvOverallPercentage.setText("N/A");
                    tvPresentCount.setText(String.valueOf(countStatus(latest, "PRESENT")));
                    tvAbsentCount.setText("N/A");
                    tvLateCount.setText(String.valueOf(countStatus(latest, "LATE")));
                    records.clear();
                    records.addAll(latest);
                    adapter.notifyDataSetChanged();
                    attendanceDao.saveRecords(records);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceRecord>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private int countStatus(List<AttendanceRecord> data, String expectedStatus) {
        int count = 0;
        for (AttendanceRecord record : data) {
            if (expectedStatus.equalsIgnoreCase(record.getStatus())) count++;
        }
        return count;
    }
}

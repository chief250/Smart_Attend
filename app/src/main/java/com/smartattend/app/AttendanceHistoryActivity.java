package com.smartattend.app;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.smartattend.app.adapter.AttendanceAdapter;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.database.AttendanceDao;
import com.smartattend.app.database.CourseDao;
import com.smartattend.app.database.DatabaseHelper;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.Course;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.NetworkUtils;
import com.smartattend.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private RecyclerView rvAttendance;
    private SwipeRefreshLayout swipeRefresh;
    private Spinner spinnerCourse;
    private TextView tvPercentage, tvTotalSessions, tvPresent, tvAbsent;
    private AttendanceAdapter adapter;
    private List<AttendanceRecord> allRecords = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private AttendanceDao attendanceDao;
    private CourseDao courseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.attendance_history);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        attendanceDao = new AttendanceDao(dbHelper);
        courseDao = new CourseDao(dbHelper);

        initViews();
        loadFromCache();
        refreshFromApi();
    }

    private void initViews() {
        rvAttendance = findViewById(R.id.rvAttendance);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);

        adapter = new AttendanceAdapter(this, allRecords);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::refreshFromApi);

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByCourse(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFromCache() {
        List<AttendanceRecord> cached = attendanceDao.getAttendanceRecords();
        allRecords.clear();
        allRecords.addAll(cached);
        adapter.updateData(allRecords);
        setupCourseSpinner(courseDao.getCourses());
    }

    private void refreshFromApi() {
        if (!NetworkUtils.isConnected(this)) {
            swipeRefresh.setRefreshing(false);
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        apiService.getAttendanceHistory().enqueue(new Callback<ApiResponse<List<AttendanceRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceRecord>>> call,
                                   Response<ApiResponse<List<AttendanceRecord>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<AttendanceRecord> records = response.body().getData();
                    if (records != null) {
                        updateUI(records);
                        attendanceDao.clearAttendance();
                        attendanceDao.insertAttendanceRecords(records);
                    }
                } else {
                    showSnackbar(getString(R.string.error_loading_data));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceRecord>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showSnackbar(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void updateUI(List<AttendanceRecord> records) {
        // The current backend history endpoint returns check-in records only; it does not
        // provide the course session denominator needed for an accurate percentage/absence count.
        tvPercentage.setText("N/A");
        tvTotalSessions.setText("N/A");
        tvPresent.setText(String.valueOf(countStatus(records, "PRESENT")));
        tvAbsent.setText("N/A");
        allRecords.clear();
        allRecords.addAll(records);
        adapter.updateData(allRecords);
    }

    private int countStatus(List<AttendanceRecord> records, String expectedStatus) {
        int count = 0;
        for (AttendanceRecord record : records) {
            if (expectedStatus.equalsIgnoreCase(record.getStatus())) count++;
        }
        return count;
    }

    private void setupCourseSpinner(List<Course> courseList) {
        courses.clear();
        courses.addAll(courseList);

        List<String> courseNames = new ArrayList<>();
        courseNames.add(getString(R.string.all_courses));
        for (Course c : courses) {
            courseNames.add(c.getCourseCode() + " - " + c.getCourseName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, courseNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(spinnerAdapter);
    }

    private void filterByCourse(int position) {
        if (position == 0) {
            adapter.updateData(allRecords);
        } else {
            Course selectedCourse = courses.get(position - 1);
            List<AttendanceRecord> filtered = new ArrayList<>();
            for (AttendanceRecord r : allRecords) {
                if (selectedCourse.getCourseCode().equals(r.getCourseCode())) {
                    filtered.add(r);
                }
            }
            adapter.updateData(filtered);
        }
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

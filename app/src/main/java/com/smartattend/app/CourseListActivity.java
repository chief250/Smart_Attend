package com.smartattend.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.smartattend.app.adapter.CourseAdapter;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.database.CourseDao;
import com.smartattend.app.database.DatabaseHelper;
import com.smartattend.app.model.Course;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.NetworkUtils;
import com.smartattend.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private SwipeRefreshLayout swipeRefresh;
    private CourseAdapter adapter;
    private List<Course> courses = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private CourseDao courseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_courses);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        courseDao = new CourseDao(DatabaseHelper.getInstance(this));

        rvCourses = findViewById(R.id.rvCourses);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        adapter = new CourseAdapter(this, courses);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadCoursesFromApi);

        loadFromCache();
        loadCoursesFromApi();
    }

    private void loadFromCache() {
        List<Course> cached = courseDao.getCourses();
        courses.clear();
        courses.addAll(cached);
        adapter.updateData(courses);
    }

    private void loadCoursesFromApi() {
        if (!NetworkUtils.isConnected(this)) {
            swipeRefresh.setRefreshing(false);
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        apiService.getMyCourses().enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Course>>> call,
                                   Response<ApiResponse<List<Course>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<Course> data = response.body().getData();
                    if (data != null) {
                        courses.clear();
                        courses.addAll(data);
                        adapter.updateData(courses);
                        courseDao.clearCourses();
                        courseDao.insertCourses(data);
                        for (Course course : data) {
                            apiService.getAttendancePercentage(course.getId()).enqueue(
                                    new Callback<ApiResponse<Double>>() {
                                        @Override
                                        public void onResponse(Call<ApiResponse<Double>> percentageCall,
                                                               Response<ApiResponse<Double>> percentageResponse) {
                                            if (percentageResponse.isSuccessful()
                                                    && percentageResponse.body() != null
                                                    && percentageResponse.body().getData() != null) {
                                                course.setAttendancePercentage(
                                                        percentageResponse.body().getData());
                                                courseDao.insertCourses(data);
                                                adapter.updateData(courses);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ApiResponse<Double>> percentageCall,
                                                              Throwable t) {
                                            // Keep the cached value if the percentage request fails.
                                        }
                                    });
                        }
                    }
                } else {
                    showSnackbar(getString(R.string.error_loading_data));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Course>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
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

package com.smartattend.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.smartattend.app.R;
import com.smartattend.app.adapter.CourseAdapter;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.database.CourseDao;
import com.smartattend.app.database.DatabaseHelper;
import com.smartattend.app.model.Course;
import com.smartattend.app.model.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoursesFragment extends Fragment {

    private RecyclerView rvCourses;
    private SwipeRefreshLayout swipeRefresh;
    private CourseAdapter adapter;
    private List<Course> courseList = new ArrayList<>();
    private ApiService apiService;
    private CourseDao courseDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        apiService = ApiClient.getApiService();
        courseDao = new CourseDao(DatabaseHelper.getInstance(requireContext()));

        rvCourses = view.findViewById(R.id.rvCourses);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        rvCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseAdapter(requireContext(), courseList);
        rvCourses.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchCourses);

        // Load offline cached courses first
        List<Course> cached = courseDao.getAllCourses();
        if (cached != null && !cached.isEmpty()) {
            courseList.clear();
            courseList.addAll(cached);
            adapter.notifyDataSetChanged();
        }

        fetchCourses();
        return view;
    }

    private void fetchCourses() {
        swipeRefresh.setRefreshing(true);
        apiService.getMyCourses().enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Course>>> call, Response<ApiResponse<List<Course>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    courseList.clear();
                    courseList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    courseDao.saveCourses(courseList);
                    for (Course course : courseList) {
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
                                            courseDao.saveCourses(courseList);
                                            adapter.notifyDataSetChanged();
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
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Course>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }
}

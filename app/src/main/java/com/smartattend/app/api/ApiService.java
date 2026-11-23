package com.smartattend.app.api;

import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.AttendanceSession;
import com.smartattend.app.model.Course;
import com.smartattend.app.model.Notification;
import com.smartattend.app.model.Student;
import com.smartattend.app.model.User;
import com.smartattend.app.model.request.ChangePasswordRequest;
import com.smartattend.app.model.request.CheckInRequest;
import com.smartattend.app.model.request.LoginRequest;
import com.smartattend.app.model.request.ReviewRequest;
import com.smartattend.app.model.request.UpdateProfileRequest;
import com.smartattend.app.model.request.SignUpRequest;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.model.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("api/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @POST("api/auth/register")
    Call<ApiResponse<Student>> register(@Body SignUpRequest signUpRequest,
                                        @Query("studentNumber") String studentNumber,
                                        @Query("program") String program,
                                        @Query("classGroup") String classGroup);

    // Student profile
    @GET("api/students/me")
    Call<ApiResponse<Student>> getMyProfile();

    // Courses
    @GET("api/students/me/courses")
    Call<ApiResponse<List<Course>>> getMyCourses();

    // Attendance history
    @GET("api/attendance/history")
    Call<ApiResponse<List<AttendanceRecord>>> getAttendanceHistory();

    // Attendance percentage for a specific course
    @GET("api/attendance/percentage")
    Call<ApiResponse<Double>> getAttendancePercentage(@Query("courseId") long courseId);

    // Check in
    @POST("api/attendance/checkin")
    Call<ApiResponse<AttendanceRecord>> checkIn(@Body CheckInRequest checkInRequest);

    // Validate QR token
    @GET("api/sessions/qr/validate")
    Call<ApiResponse<AttendanceSession>> validateQrToken(@Query("token") String token);

    // Today's sessions
    @GET("api/sessions/today")
    Call<ApiResponse<List<AttendanceSession>>> getTodaySessions();

    // Update profile
    @PUT("api/students/me")
    Call<ApiResponse<User>> updateProfile(@Body UpdateProfileRequest updateProfileRequest);

    // Change password
    @PUT("api/auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest changePasswordRequest);

    // Submit review / dispute
    @POST("api/reviews")
    Call<ApiResponse<Object>> submitReview(@Body ReviewRequest reviewRequest);

    // Notifications
    @GET("api/notifications")
    Call<ApiResponse<List<Notification>>> getNotifications();

    // Mark notification as read
    @PUT("api/notifications/{id}/read")
    Call<ApiResponse<Void>> markNotificationRead(@Path("id") long notificationId);

}

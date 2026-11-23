package com.smartattend.app.utils;

public class Constants {
    // Base URL - override in BuildConfig
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    // SharedPreferences
    public static final String PREFS_NAME = "SmartAttendPrefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_STUDENT_NUMBER = "student_number";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROGRAM = "program";
    public static final String KEY_CLASS_GROUP = "class_group";
    public static final String KEY_FACE_TEMPLATE = "face_template";

    // Intent extras
    public static final String EXTRA_QR_TOKEN = "extra_qr_token";
    public static final String EXTRA_SESSION_ID = "extra_session_id";
    public static final String EXTRA_SESSION = "extra_session";
    public static final String EXTRA_ATTENDANCE_RECORD = "extra_attendance_record";
    public static final String EXTRA_FACE_VERIFIED = "extra_face_verified";
    public static final String EXTRA_FACE_TEMPLATE = "extra_face_template";
    public static final String EXTRA_COURSE_ID = "extra_course_id";
    public static final String EXTRA_COURSE_NAME = "extra_course_name";
    public static final String EXTRA_ATTENDANCE_ID = "extra_attendance_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    // User roles
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_LECTURER = "LECTURER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Attendance statuses
    public static final String STATUS_PRESENT = "PRESENT";
    public static final String STATUS_LATE = "LATE";
    public static final String STATUS_ABSENT = "ABSENT";

    // Session statuses
    public static final String SESSION_ACTIVE = "ACTIVE";
    public static final String SESSION_CLOSED = "CLOSED";
    public static final String SESSION_UPCOMING = "UPCOMING";

    // API Endpoints
    public static final String ENDPOINT_LOGIN = "api/auth/login";
    public static final String ENDPOINT_PROFILE = "api/students/me";
    public static final String ENDPOINT_COURSES = "api/students/me/courses";
    public static final String ENDPOINT_ATTENDANCE_HISTORY = "api/attendance/history";
    public static final String ENDPOINT_CHECK_IN = "api/attendance/checkin";
    public static final String ENDPOINT_SESSIONS_TODAY = "api/sessions/today";

    // Face detection thresholds
    public static final float MIN_FACE_CONFIDENCE = 0.85f;
    public static final int MIN_FACE_WIDTH_PX = 100;
    public static final int MIN_FACE_HEIGHT_PX = 100;

    // Splash delay
    public static final int SPLASH_DELAY_MS = 2000;
}

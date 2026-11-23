package com.smartattend.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, long userId, String userRole, String fullName,
                            long studentId, String studentNumber, String email,
                            String program, String classGroup) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putLong(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_ROLE, userRole);
        editor.putString(Constants.KEY_FULL_NAME, fullName);
        editor.putLong(Constants.KEY_STUDENT_ID, studentId);
        editor.putString(Constants.KEY_STUDENT_NUMBER, studentNumber);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_PROGRAM, program);
        editor.putString(Constants.KEY_CLASS_GROUP, classGroup);
        editor.apply();
    }

    public void saveToken(String token) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public long getUserId() {
        return prefs.getLong(Constants.KEY_USER_ID, -1);
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, null);
    }

    public String getFullName() {
        return prefs.getString(Constants.KEY_FULL_NAME, null);
    }

    public long getStudentId() {
        return prefs.getLong(Constants.KEY_STUDENT_ID, -1);
    }

    public String getStudentNumber() {
        return prefs.getString(Constants.KEY_STUDENT_NUMBER, null);
    }

    public String getEmail() {
        return prefs.getString(Constants.KEY_EMAIL, null);
    }

    public String getProgram() {
        return prefs.getString(Constants.KEY_PROGRAM, null);
    }

    public String getClassGroup() {
        return prefs.getString(Constants.KEY_CLASS_GROUP, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void updateProfile(String fullName, String email, String program) {
        editor.putString(Constants.KEY_FULL_NAME, fullName);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_PROGRAM, program);
        editor.apply();
    }

    public String getBaseUrl() {
        return prefs.getString("custom_base_url", com.smartattend.app.BuildConfig.BASE_URL);
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            String trimmed = baseUrl.trim();
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                trimmed = "http://" + trimmed;
            }
            if (!trimmed.endsWith("/")) {
                trimmed = trimmed + "/";
            }
            editor.putString("custom_base_url", trimmed);
            editor.apply();
        }
    }

    public void saveFaceTemplate(String email, String faceTemplate) {
        if (email != null && !email.trim().isEmpty()) {
            editor.putString(Constants.KEY_FACE_TEMPLATE + "_" + email.toLowerCase().trim(), faceTemplate);
        }
        editor.putString(Constants.KEY_FACE_TEMPLATE, faceTemplate);
        editor.apply();
    }

    public String getFaceTemplate() {
        String email = getEmail();
        if (email != null && !email.trim().isEmpty()) {
            String template = prefs.getString(Constants.KEY_FACE_TEMPLATE + "_" + email.toLowerCase().trim(), null);
            if (template != null) return template;
        }
        return prefs.getString(Constants.KEY_FACE_TEMPLATE, null);
    }

    public String getFaceTemplate(String email) {
        if (email != null && !email.trim().isEmpty()) {
            String template = prefs.getString(Constants.KEY_FACE_TEMPLATE + "_" + email.toLowerCase().trim(), null);
            if (template != null) return template;
        }
        return getFaceTemplate();
    }

    public boolean hasEnrolledFace() {
        String template = getFaceTemplate();
        return template != null && !template.trim().isEmpty();
    }
}

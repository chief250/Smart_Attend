package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.smartattend.app.api.ApiClient;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                String role = sessionManager.getUserRole();
                navigateToDashboard(role);
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, Constants.SPLASH_DELAY_MS);
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (Constants.ROLE_STUDENT.equals(role)) {
            intent = new Intent(this, StudentDashboardActivity.class);
        } else {
            // Default to student dashboard; extend for other roles
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
    }
}

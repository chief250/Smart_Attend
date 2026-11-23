package com.smartattend.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.smartattend.app.api.ApiClient;
import com.smartattend.app.api.ApiService;
import com.smartattend.app.model.AttendanceSession;
import com.smartattend.app.model.response.ApiResponse;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.NetworkUtils;
import com.smartattend.app.utils.SessionManager;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private ProgressBar progressBar;
    private TextView tvInstructions;
    private boolean isProcessing = false;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.scan_qr_code);
        }

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);
        apiService = ApiClient.getApiService();

        barcodeView = findViewById(R.id.barcodeView);
        progressBar = findViewById(R.id.progressBar);
        tvInstructions = findViewById(R.id.tvInstructions);

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (!isProcessing && result.getText() != null) {
                    isProcessing = true;
                    barcodeView.pause();
                    validateQrToken(result.getText());
                }
            }
        });
    }

    private void validateQrToken(String token) {
        if (!NetworkUtils.isConnected(this)) {
            showError(getString(R.string.error_no_internet));
            isProcessing = false;
            barcodeView.resume();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvInstructions.setText(R.string.validating_qr);

        apiService.validateQrToken(token).enqueue(new Callback<ApiResponse<AttendanceSession>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceSession>> call,
                                   Response<ApiResponse<AttendanceSession>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AttendanceSession> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AttendanceSession session = apiResponse.getData();
                        proceedToFaceVerification(token, session);
                    } else {
                        showInvalidQrDialog(apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : getString(R.string.error_qr_invalid));
                    }
                } else {
                    showInvalidQrDialog(getString(R.string.error_qr_expired));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceSession>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError(getString(R.string.error_network) + ": " + t.getMessage());
                isProcessing = false;
                barcodeView.resume();
            }
        });
    }

    private void proceedToFaceVerification(String qrToken, AttendanceSession session) {
        Intent intent = new Intent(this, FaceVerificationActivity.class);
        intent.putExtra(Constants.EXTRA_QR_TOKEN, qrToken);
        intent.putExtra(Constants.EXTRA_SESSION, new Gson().toJson(session));
        startActivity(intent);
        finish();
    }

    private void showInvalidQrDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_qr_invalid_title)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    isProcessing = false;
                    tvInstructions.setText(R.string.qr_instructions);
                    barcodeView.resume();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showError(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

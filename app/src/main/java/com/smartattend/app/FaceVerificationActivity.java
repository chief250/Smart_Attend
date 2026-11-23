package com.smartattend.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.view.FaceOverlayView;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceVerificationActivity extends AppCompatActivity {

    private static final String TAG = "FaceVerification";
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private PreviewView previewView;
    private FaceOverlayView faceOverlayView;
    private Button btnCapture;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    private String qrToken;
    private String sessionJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.face_verification);
        }

        qrToken = getIntent().getStringExtra(Constants.EXTRA_QR_TOKEN);
        sessionJson = getIntent().getStringExtra(Constants.EXTRA_SESSION);

        previewView = findViewById(R.id.previewView);
        faceOverlayView = findViewById(R.id.faceOverlayView);
        btnCapture = findViewById(R.id.btnCapture);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Configure ML Kit face detector
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(options);

        btnCapture.setOnClickListener(v -> captureAndVerify());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera binding failed", e);
                Toast.makeText(this, R.string.error_camera, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndVerify() {
        if (imageCapture == null) {
            Toast.makeText(this, R.string.error_camera_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }

        btnCapture.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.analyzing_face);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        if (image.getImage() == null) {
                            image.close();
                            handleVerificationFailure(getString(R.string.error_capture_failed));
                            return;
                        }
                        detectFace(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        handleVerificationFailure(getString(R.string.error_capture_failed));
                    }
                });
    }

    private void detectFace(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());
        faceDetector.process(image)
                .addOnSuccessListener(this::processFaceResults)
                .addOnFailureListener(e -> handleVerificationFailure(e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void processFaceResults(List<Face> faces) {
        progressBar.setVisibility(View.GONE);
        btnCapture.setEnabled(true);

        if (faces.isEmpty()) {
            tvStatus.setText(R.string.face_not_detected);
            showRetryOption(getString(R.string.face_not_detected_message));
            return;
        }

        Face face = faces.get(0);
        android.graphics.Rect bounds = face.getBoundingBox();
        boolean sizeOk = bounds.width() >= Constants.MIN_FACE_WIDTH_PX
                && bounds.height() >= Constants.MIN_FACE_HEIGHT_PX;

        // Check smile / eye open probability as additional confidence metrics
        Float leftEyeProb = face.getLeftEyeOpenProbability();
        Float rightEyeProb = face.getRightEyeOpenProbability();
        boolean eyesOk = (leftEyeProb == null || leftEyeProb > 0.5f)
                && (rightEyeProb == null || rightEyeProb > 0.5f);

        if (sizeOk && eyesOk) {
            com.smartattend.app.utils.SessionManager sessionManager = new com.smartattend.app.utils.SessionManager(this);
            String enrolledTemplate = sessionManager.getFaceTemplate();
            String liveTemplate = FaceEnrollmentActivity.extractFaceMetrics(face);

            if (liveTemplate == null) {
                showRetryOption("Could not extract facial landmarks clearly. Please face the camera directly with good lighting.");
                return;
            }

            if (enrolledTemplate == null || enrolledTemplate.trim().isEmpty()) {
                // Auto-enroll reference face on first capture for this student
                sessionManager.saveFaceTemplate(sessionManager.getEmail(), liveTemplate);
                tvStatus.setText("Face registered & verified successfully! ✓");
                Toast.makeText(this, "Reference face registered for this student! ✓", Toast.LENGTH_SHORT).show();
                proceedToConfirmation(true);
                return;
            }

            boolean matchesRegisteredStudent = matchFaceTemplates(liveTemplate, enrolledTemplate);

            if (matchesRegisteredStudent) {
                tvStatus.setText("Face landmark check passed ✓");
                proceedToConfirmation(true);
            } else {
                tvStatus.setText("Face landmark check failed: mismatch");
                showFaceMismatchDialog(liveTemplate, sessionManager);
            }
        } else if (!sizeOk) {
            showRetryOption(getString(R.string.face_too_small));
        } else {
            showRetryOption(getString(R.string.eyes_not_open));
        }
    }

    private void showFaceMismatchDialog(String liveTemplate, com.smartattend.app.utils.SessionManager sessionManager) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.verification_failed_title)
                .setMessage("Captured face does not match the registered student face. If your appearance, angle, or lighting changed, you can update your reference face.")
                .setPositiveButton(R.string.retry, (d, w) -> {
                    tvStatus.setText(R.string.position_face);
                })
                .setNeutralButton("Update Face Reference", (d, w) -> {
                    sessionManager.saveFaceTemplate(sessionManager.getEmail(), liveTemplate);
                    Toast.makeText(this, "Reference face updated successfully! ✓", Toast.LENGTH_SHORT).show();
                    proceedToConfirmation(true);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    public static boolean matchFaceTemplates(String liveTemplate, String enrolledTemplate) {
        // ML Kit Face Detection provides landmarks, not identity embeddings. This geometric
        // comparison is only a demo gate; it is not a secure biometric identity check.
        if (liveTemplate == null || enrolledTemplate == null) return false;
        try {
            if (liveTemplate.startsWith("METRICS:") && enrolledTemplate.startsWith("METRICS:")) {
                String[] liveParts = liveTemplate.substring(8).split(",");
                String[] enrolledParts = enrolledTemplate.substring(8).split(",");
                if (liveParts.length == 6 && enrolledParts.length == 6) {
                    float sumSq = 0f;
                    for (int i = 0; i < 6; i++) {
                        float diff = Float.parseFloat(liveParts[i]) - Float.parseFloat(enrolledParts[i]);
                        if (Float.isNaN(diff) || Float.isInfinite(diff)) return false;
                        sumSq += diff * diff;
                    }
                    float dist = (float) Math.sqrt(sumSq);
                    Log.d(TAG, "Face Landmark Vector Distance: " + dist);
                    return dist <= 0.32f;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error matching face templates", e);
            return false;
        }
    }

    private void handleVerificationFailure(String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            tvStatus.setText(R.string.face_verification_failed);
            showRetryOption(message);
        });
    }

    private void showRetryOption(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.verification_failed_title)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (d, w) -> {
                    tvStatus.setText(R.string.position_face);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void proceedToConfirmation(boolean faceVerified) {
        Intent intent = new Intent(this, AttendanceConfirmationActivity.class);
        intent.putExtra(Constants.EXTRA_QR_TOKEN, qrToken);
        intent.putExtra(Constants.EXTRA_SESSION, sessionJson);
        intent.putExtra(Constants.EXTRA_FACE_VERIFIED, faceVerified);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        faceDetector.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

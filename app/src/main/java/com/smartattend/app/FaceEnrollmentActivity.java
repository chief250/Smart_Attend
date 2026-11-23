package com.smartattend.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
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
import com.google.mlkit.vision.face.FaceLandmark;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.view.FaceOverlayView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceEnrollmentActivity extends AppCompatActivity {

    private static final String TAG = "FaceEnrollment";
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private PreviewView previewView;
    private FaceOverlayView faceOverlayView;
    private Button btnCapture;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_enrollment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Face Enrollment");
        }

        previewView = findViewById(R.id.previewView);
        faceOverlayView = findViewById(R.id.faceOverlayView);
        btnCapture = findViewById(R.id.btnCapture);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .build();
        faceDetector = FaceDetection.getClient(options);

        btnCapture.setOnClickListener(v -> captureFaceTemplate());

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
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureFaceTemplate() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCapture.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Extracting facial features...");

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        android.media.Image mediaImage = image.getImage();
                        if (mediaImage == null) {
                            image.close();
                            handleFailure("Failed to capture image");
                            return;
                        }
                        InputImage input = InputImage.fromMediaImage(mediaImage,
                                image.getImageInfo().getRotationDegrees());
                        faceDetector.process(input)
                                .addOnSuccessListener(FaceEnrollmentActivity.this::processEnrollmentFaces)
                                .addOnFailureListener(e -> handleFailure(e.getMessage()))
                                .addOnCompleteListener(task -> image.close());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        handleFailure("Camera capture error");
                    }
                });
    }

    private void processEnrollmentFaces(List<Face> faces) {
                    progressBar.setVisibility(View.GONE);
                    btnCapture.setEnabled(true);

                    if (faces.isEmpty()) {
                        tvStatus.setText("No face detected. Look directly at the camera.");
                        Toast.makeText(this, "No face detected in frame", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Face face = faces.get(0);
                    String template = extractFaceMetrics(face);
                    if (template != null) {
                        tvStatus.setText("Reference Face Enrolled Successfully! ✓");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Constants.EXTRA_FACE_TEMPLATE, template);
                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(this, "Reference face template created!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        tvStatus.setText("Could not extract landmarks. Please align face clearly.");
                        Toast.makeText(this, "Face landmark detection incomplete. Try again.", Toast.LENGTH_SHORT).show();
                    }
    }

    public static String extractFaceMetrics(Face face) {
        if (face == null) return null;
        Rect bounds = face.getBoundingBox();
        float width = Math.max(1, bounds.width());
        float height = Math.max(1, bounds.height());

        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
        FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
        FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);

        if (leftEye == null || rightEye == null || noseBase == null) return null;

        PointF pLeftEye = leftEye.getPosition();
        PointF pRightEye = rightEye.getPosition();
        PointF pNose = noseBase.getPosition();

        float eyeDist = distance(pLeftEye, pRightEye) / width;
        PointF eyeCenter = new PointF((pLeftEye.x + pRightEye.x) / 2f, (pLeftEye.y + pRightEye.y) / 2f);
        float noseEyeDist = distance(pNose, eyeCenter) / height;

        float mouthWidth = 0.35f;
        float noseMouthDist = 0.22f;
        if (mouthLeft != null && mouthRight != null) {
            PointF pMouthLeft = mouthLeft.getPosition();
            PointF pMouthRight = mouthRight.getPosition();
            mouthWidth = distance(pMouthLeft, pMouthRight) / width;
            PointF mouthCenter = new PointF((pMouthLeft.x + pMouthRight.x) / 2f, (pMouthLeft.y + pMouthRight.y) / 2f);
            noseMouthDist = distance(pNose, mouthCenter) / height;
        }

        float leftEyeNoseDist = distance(pLeftEye, pNose) / width;
        float rightEyeNoseDist = distance(pRightEye, pNose) / width;

        return String.format(Locale.US, "METRICS:%.4f,%.4f,%.4f,%.4f,%.4f,%.4f",
                eyeDist, noseEyeDist, mouthWidth, noseMouthDist, leftEyeNoseDist, rightEyeNoseDist);
    }

    private static float distance(PointF p1, PointF p2) {
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void handleFailure(String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            tvStatus.setText("Face Enrollment Failed");
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
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

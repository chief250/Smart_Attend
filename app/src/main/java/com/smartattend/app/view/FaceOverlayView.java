package com.smartattend.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FaceOverlayView extends View {

    private Paint ovalPaint;
    private Paint borderPaint;
    private Paint dimPaint;
    private RectF ovalRect;

    public FaceOverlayView(Context context) {
        super(context);
        init();
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Oval fill (semi-transparent)
        ovalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ovalPaint.setStyle(Paint.Style.FILL);
        ovalPaint.setColor(Color.argb(30, 255, 255, 255));

        // Oval border
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(4f);

        // Dim overlay for area outside oval
        dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dimPaint.setStyle(Paint.Style.FILL);
        dimPaint.setColor(Color.argb(100, 0, 0, 0));

        ovalRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float ovalWidth = width * 0.65f;
        float ovalHeight = height * 0.45f;
        float centerX = width / 2f;
        float centerY = height * 0.42f;

        ovalRect.set(
                centerX - ovalWidth / 2,
                centerY - ovalHeight / 2,
                centerX + ovalWidth / 2,
                centerY + ovalHeight / 2
        );

        // Draw dim background (entire view)
        canvas.drawRect(0, 0, width, height, dimPaint);

        // Clear oval area (draw oval with transparent paint using PorterDuff or just fill white)
        Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clearPaint.setStyle(Paint.Style.FILL);
        clearPaint.setColor(Color.argb(0, 0, 0, 0));
        clearPaint.setXfermode(new android.graphics.PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.CLEAR));

        // Enable layer
        int saveCount = canvas.saveLayer(0, 0, width, height, null);
        canvas.drawRect(0, 0, width, height, dimPaint);
        canvas.drawOval(ovalRect, clearPaint);
        canvas.restoreToCount(saveCount);

        // Draw oval border
        canvas.drawOval(ovalRect, borderPaint);

        // Draw corner tick marks
        float tickLen = 30f;
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setColor(0xFF1565C0); // primary blue
        tickPaint.setStrokeWidth(6f);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        // Top-left
        canvas.drawLine(ovalRect.left, ovalRect.top + tickLen, ovalRect.left, ovalRect.top, tickPaint);
        canvas.drawLine(ovalRect.left, ovalRect.top, ovalRect.left + tickLen, ovalRect.top, tickPaint);
        // Top-right
        canvas.drawLine(ovalRect.right - tickLen, ovalRect.top, ovalRect.right, ovalRect.top, tickPaint);
        canvas.drawLine(ovalRect.right, ovalRect.top, ovalRect.right, ovalRect.top + tickLen, tickPaint);
        // Bottom-left
        canvas.drawLine(ovalRect.left, ovalRect.bottom - tickLen, ovalRect.left, ovalRect.bottom, tickPaint);
        canvas.drawLine(ovalRect.left, ovalRect.bottom, ovalRect.left + tickLen, ovalRect.bottom, tickPaint);
        // Bottom-right
        canvas.drawLine(ovalRect.right - tickLen, ovalRect.bottom, ovalRect.right, ovalRect.bottom, tickPaint);
        canvas.drawLine(ovalRect.right, ovalRect.bottom, ovalRect.right, ovalRect.bottom - tickLen, tickPaint);
    }
}

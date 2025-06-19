package com.chores.app.kids.chores_app_for_kids.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.chores.app.kids.chores_app_for_kids.R;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private int progress = 0;
    private int maxProgress = 100;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.progress_background));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - 12;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw progress arc
        float sweepAngle = (360f * progress) / maxProgress;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(int progress) {
        this.progress = Math.min(progress, maxProgress);
        invalidate();
    }

    public int getProgress() {
        return progress;
    }
}
package com.chores.app.kids.chores_app_for_kids.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.chores.app.kids.chores_app_for_kids.R;
import java.util.List;

public class WeeklyChartView extends View {
    private Paint linePaint;
    private Paint fillPaint;
    private Paint pointPaint;
    private Path linePath;
    private Path fillPath;
    private List<Integer> data;
    private String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public WeeklyChartView(Context context) {
        super(context);
        init();
    }

    public WeeklyChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_color));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_color_alpha));
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_color));
        pointPaint.setStyle(Paint.Style.FILL);

        linePath = new Path();
        fillPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) return;

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom() - 40; // Space for labels

        int maxValue = getMaxValue();
        if (maxValue == 0) maxValue = 1;

        float stepX = (float) width / (data.size() - 1);

        linePath.reset();
        fillPath.reset();

        // Draw chart
        for (int i = 0; i < data.size(); i++) {
            float x = getPaddingLeft() + i * stepX;
            float y = getPaddingTop() + height - (data.get(i) * height / maxValue);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, getPaddingTop() + height);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            // Draw points
            canvas.drawCircle(x, y, 8, pointPaint);

            // Draw day labels
            Paint textPaint = new Paint();
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            textPaint.setTextSize(24);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(dayLabels[i], x, getHeight() - 10, textPaint);
        }

        // Complete fill path
        fillPath.lineTo(getPaddingLeft() + (data.size() - 1) * stepX, getPaddingTop() + height);
        fillPath.close();

        // Draw fill and line
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }

    private int getMaxValue() {
        int max = 0;
        for (int value : data) {
            max = Math.max(max, value);
        }
        return max;
    }

    public void setData(List<Integer> data) {
        this.data = data;
        invalidate();
    }
}
package com.example.medictown.ui.admin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RevenueChartView extends View {

    public static class ChartPoint {
        final String label;
        final double value;

        ChartPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<ChartPoint> points = new ArrayList<>();
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF chartBounds = new RectF();

    public RevenueChartView(Context context) {
        super(context);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setColor(Color.parseColor("#EEF2F5"));
        gridPaint.setStrokeWidth(dp(1));

        linePaint.setColor(Color.parseColor("#00A876"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(dp(3));

        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);

        dotStrokePaint.setColor(Color.parseColor("#00A876"));
        dotStrokePaint.setStyle(Paint.Style.STROKE);
        dotStrokePaint.setStrokeWidth(dp(2));

        labelPaint.setColor(Color.parseColor("#BBCCC2"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(sp(10));

        emptyPaint.setColor(Color.parseColor("#8A9990"));
        emptyPaint.setTextAlign(Paint.Align.CENTER);
        emptyPaint.setTextSize(sp(12));
    }

    public void setPoints(List<ChartPoint> newPoints) {
        points.clear();
        if (newPoints != null) {
            points.addAll(newPoints);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float labelHeight = dp(26);
        chartBounds.set(
                getPaddingLeft() + dp(10),
                getPaddingTop() + dp(8),
                getWidth() - getPaddingRight() - dp(10),
                getHeight() - getPaddingBottom() - labelHeight
        );

        drawGrid(canvas);

        if (points.isEmpty()) {
            canvas.drawText(
                    "Chua co du lieu",
                    getWidth() / 2f,
                    chartBounds.centerY(),
                    emptyPaint
            );
            return;
        }

        List<PointF> coordinates = buildCoordinates();
        drawFill(canvas, coordinates);
        drawLine(canvas, coordinates);
        drawDots(canvas, coordinates);
        drawLabels(canvas, coordinates);
    }

    private void drawGrid(Canvas canvas) {
        for (int index = 0; index < 3; index++) {
            float y = chartBounds.top + (chartBounds.height() / 3f) * index;
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint);
        }
    }

    private List<PointF> buildCoordinates() {
        double max = 0;
        for (ChartPoint point : points) {
            max = Math.max(max, point.value);
        }

        List<PointF> coordinates = new ArrayList<>();
        float usableHeight = chartBounds.height() * 0.9f;
        int lastIndex = Math.max(points.size() - 1, 1);
        for (int index = 0; index < points.size(); index++) {
            ChartPoint point = points.get(index);
            float x = points.size() == 1
                    ? chartBounds.centerX()
                    : chartBounds.left + (chartBounds.width() * index / lastIndex);
            float y = max <= 0
                    ? chartBounds.bottom
                    : chartBounds.bottom - (float) (point.value / max) * usableHeight;
            coordinates.add(new PointF(x, y));
        }
        return coordinates;
    }

    private void drawFill(Canvas canvas, List<PointF> coordinates) {
        if (coordinates.isEmpty()) {
            return;
        }

        Path fillPath = createLinePath(coordinates);
        PointF first = coordinates.get(0);
        PointF last = coordinates.get(coordinates.size() - 1);
        fillPath.lineTo(last.x, chartBounds.bottom);
        fillPath.lineTo(first.x, chartBounds.bottom);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(
                0,
                chartBounds.top,
                0,
                chartBounds.bottom,
                Color.parseColor("#3300A876"),
                Color.parseColor("#1100A876"),
                Shader.TileMode.CLAMP
        ));
        canvas.drawPath(fillPath, fillPaint);
        fillPaint.setShader(null);
    }

    private void drawLine(Canvas canvas, List<PointF> coordinates) {
        if (coordinates.size() == 1) {
            PointF point = coordinates.get(0);
            canvas.drawLine(chartBounds.left, point.y, chartBounds.right, point.y, linePaint);
            return;
        }
        canvas.drawPath(createLinePath(coordinates), linePaint);
    }

    private Path createLinePath(List<PointF> coordinates) {
        Path path = new Path();
        PointF first = coordinates.get(0);
        path.moveTo(first.x, first.y);

        for (int index = 1; index < coordinates.size(); index++) {
            PointF previous = coordinates.get(index - 1);
            PointF current = coordinates.get(index);
            float midX = (previous.x + current.x) / 2f;
            path.cubicTo(midX, previous.y, midX, current.y, current.x, current.y);
        }
        return path;
    }

    private void drawDots(Canvas canvas, List<PointF> coordinates) {
        for (int index = 0; index < coordinates.size(); index++) {
            if (!shouldEmphasizePoint(index)) {
                continue;
            }
            PointF point = coordinates.get(index);
            canvas.drawCircle(point.x, point.y, dp(4), dotPaint);
            canvas.drawCircle(point.x, point.y, dp(4), dotStrokePaint);
        }
    }

    private void drawLabels(Canvas canvas, List<PointF> coordinates) {
        Paint.FontMetrics metrics = labelPaint.getFontMetrics();
        float y = chartBounds.bottom + dp(18) - ((metrics.ascent + metrics.descent) / 2f);
        for (int index = 0; index < coordinates.size(); index++) {
            if (!shouldShowLabel(index)) {
                continue;
            }
            canvas.drawText(points.get(index).label, coordinates.get(index).x, y, labelPaint);
        }
    }

    private boolean shouldShowLabel(int index) {
        int count = points.size();
        if (count <= 7) {
            return true;
        }
        if (index == 0 || index == count - 1) {
            return true;
        }
        int step = (int) Math.ceil((count - 1) / 5.0);
        return index % step == 0;
    }

    private boolean shouldEmphasizePoint(int index) {
        int count = points.size();
        if (count <= 10) {
            return points.get(index).value > 0 || count == 1;
        }
        if (index == 0 || index == count - 1) {
            return true;
        }
        return shouldShowLabel(index) && points.get(index).value > 0;
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}

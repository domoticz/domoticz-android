package nl.hnogames.domoticz.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import nl.hnogames.domoticz.R;

public class EnergyFlowView extends View {

    private Paint flowPaint;
    private Paint dashedPaint;
    private Path gridToHomePath;
    private Path batteryToHomePath;
    private Path solarToHomePath;

    private float animationPhase = 0f;
    private ValueAnimator flowAnimator;

    private int solarPower = 0;
    private int batteryPower = 0;
    private int gridPower = 0;
    private int homePower = 0;

    private boolean isLandscape = false;

    public EnergyFlowView(Context context) {
        super(context);
        init();
    }

    public EnergyFlowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnergyFlowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        flowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flowPaint.setStyle(Paint.Style.STROKE);
        flowPaint.setStrokeWidth(4f);
        flowPaint.setColor(ContextCompat.getColor(getContext(), R.color.material_orange_600));

        dashedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashedPaint.setStyle(Paint.Style.STROKE);
        dashedPaint.setStrokeWidth(4f);
        dashedPaint.setColor(ContextCompat.getColor(getContext(), R.color.material_grey_400_));
        dashedPaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        // Start animation
        flowAnimator = ValueAnimator.ofFloat(0f, 1f);
        flowAnimator.setDuration(2000);
        flowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flowAnimator.addUpdateListener(animation -> {
            animationPhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        flowAnimator.start();

        // Check orientation
        isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        isLandscape = w > h;
        createPaths();
    }

    private void createPaths() {
        int width = getWidth();
        int height = getHeight();

        if (isLandscape) {
            // Landscape: Grid LEFT, Home CENTER, Solar/Battery RIGHT
            float gridX = width * 0.15f;
            float gridY = height * 0.5f;
            float homeX = width * 0.5f;
            float homeY = height * 0.5f;
            float solarX = width * 0.78f;
            float solarY = height * 0.25f;
            float batteryX = width * 0.78f;
            float batteryY = height * 0.75f;

            // Grid to Home (horizontal line)
            gridToHomePath = new Path();
            gridToHomePath.moveTo(gridX + 55, gridY);
            gridToHomePath.lineTo(homeX - 65, homeY);

            // Solar to Home (diagonal from top-right)
            solarToHomePath = new Path();
            solarToHomePath.moveTo(solarX - 50, solarY + 40);
            solarToHomePath.cubicTo(
                solarX - 100, solarY + 80,
                homeX + 100, homeY - 50,
                homeX + 50, homeY - 30
            );

            // Battery to Home (diagonal from bottom-right)
            batteryToHomePath = new Path();
            batteryToHomePath.moveTo(batteryX - 50, batteryY - 40);
            batteryToHomePath.cubicTo(
                batteryX - 100, batteryY - 80,
                homeX + 100, homeY + 50,
                homeX + 50, homeY + 30
            );

        } else {
            // Portrait: Grid TOP, Home CENTER, Battery LEFT, Solar RIGHT, Gas/Water/Car BOTTOM
            float gridX = width * 0.5f;
            float gridY = height * 0.15f;
            float homeX = width * 0.5f;
            float homeY = height * 0.5f;
            float batteryX = width * 0.25f;
            float batteryY = homeY + 10;
            float solarX = width * 0.75f;
            float solarY = homeY + 10;

            // Home circle radius is approximately 70dp
            float homeRadius = 70f;

            // Grid to Home (vertical line from top, stopping at top of home circle)
            gridToHomePath = new Path();
            gridToHomePath.moveTo(gridX, gridY + 60);
            gridToHomePath.lineTo(homeX, homeY - homeRadius);

            // Solar to Home (from right, connecting to right side of home circle)
            solarToHomePath = new Path();
            solarToHomePath.moveTo(solarX - 55, solarY);
            solarToHomePath.lineTo(homeX + homeRadius, homeY);

            // Battery to Home (from left, connecting to left side of home circle)
            batteryToHomePath = new Path();
            batteryToHomePath.moveTo(batteryX + 55, batteryY);
            batteryToHomePath.lineTo(homeX - homeRadius, homeY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (gridToHomePath == null) {
            createPaths();
            return;
        }

        // Always draw grid to home connection
        if (gridPower > 5) {
            // Importing from grid
            drawAnimatedPath(canvas, gridToHomePath, flowPaint);
        } else if (gridPower < -5) {
            // Exporting to grid
            drawAnimatedPathReverse(canvas, gridToHomePath, flowPaint);
        } else {
            // No power flow - show dashed line
            canvas.drawPath(gridToHomePath, dashedPaint);
        }

        // Always draw solar to home connection
        if (solarPower > 5) {
            drawAnimatedPath(canvas, solarToHomePath, flowPaint);
        } else {
            // No power flow - show dashed line
            canvas.drawPath(solarToHomePath, dashedPaint);
        }

        // Always draw battery connection
        if (batteryPower < -5) {
            // Battery discharging (to home)
            drawAnimatedPath(canvas, batteryToHomePath, flowPaint);
        } else if (batteryPower > 5) {
            // Battery charging (from home/solar)
            drawAnimatedPathReverse(canvas, batteryToHomePath, flowPaint);
        } else {
            // No power flow - show dashed line
            canvas.drawPath(batteryToHomePath, dashedPaint);
        }
    }

    private void drawAnimatedPath(Canvas canvas, Path path, Paint paint) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float length = pathMeasure.getLength();

        // Create animated dashed effect
        DashPathEffect dashEffect = new DashPathEffect(
            new float[]{30, 20},
            length * animationPhase
        );

        paint.setPathEffect(dashEffect);
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    private void drawAnimatedPathReverse(Canvas canvas, Path path, Paint paint) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float length = pathMeasure.getLength();

        // Create animated dashed effect (reverse direction)
        DashPathEffect dashEffect = new DashPathEffect(
            new float[]{30, 20},
            length * (1 - animationPhase)
        );

        paint.setPathEffect(dashEffect);
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    public void updatePowerValues(int solar, int battery, int grid, int home) {
        this.solarPower = solar;
        this.batteryPower = battery;
        this.gridPower = grid;
        this.homePower = home;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (flowAnimator != null) {
            flowAnimator.cancel();
        }
    }
}






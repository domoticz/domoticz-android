package nl.hnogames.domoticz.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Draws animated energy-flow lines between the four main nodes.
 *
 * Diamond layout (supplied by fragment via setNodeCenters):
 *
 *         [Grid]        ← top-center
 *   [Bat]       [Solar] ← middle row
 *         [Home]        ← bottom-center
 *
 * Each node connects to a shared central hub point via a cubic bezier curve,
 * matching the web dashboard's cross/flow appearance.
 *
 * Flow conventions:
 *   gridW    > 0  → grid → home  (consuming from grid)
 *   gridW    < 0  → home → grid  (exporting to grid)
 *   solarW   > 0  → solar → home (and/or solar → grid when exporting)
 *   batteryW > 0  → home → battery (charging)
 *   batteryW < 0  → battery → home (discharging)
 */
public class EnergyFlowView extends View {

    private static final int   COLOR_GRID    = Color.parseColor("#00BCD4"); // cyan
    private static final int   COLOR_SOLAR   = Color.parseColor("#FFC107"); // yellow/amber
    private static final int   COLOR_BATTERY = Color.parseColor("#8BC34A"); // green
    private static final int   COLOR_IDLE    = Color.parseColor("#BDBDBD"); // grey dashes

    private static final int   ACTIVE_THRESHOLD = 10;   // watts
    private static final float BASE_STROKE_DP   = 4f;
    private static final float MAX_STROKE_DP    = 9f;
    private static final float BALL_RADIUS_DP   = 5.5f;
    private static final int   ANIM_MS          = 1800;

    private Paint gridPaint, solarPaint, batteryPaint, idlePaint;
    private Paint gridBallPaint, solarBallPaint, batteryBallPaint;

    // Four bezier arm paths: node-edge → hub center
    private Path pathGridHub, pathSolarHub, pathBatHub, pathHomeHub;

    private float animPhase = 0f;
    private ValueAnimator animator;

    // Power values (signed watts)
    private int solarW = 0, batteryW = 0, gridW = 0;

    // Node centres + radii in this view's local coordinate space
    private float gridCx, gridCy, gridR;
    private float batCx,  batCy,  batR;
    private float solCx,  solCy,  solR;
    private float homeCx, homeCy, homeR;

    // Computed hub center
    private float hubX, hubY;

    private boolean ready = false;

    public EnergyFlowView(Context context) { super(context); init(); }
    public EnergyFlowView(Context context, @Nullable AttributeSet a) { super(context, a); init(); }
    public EnergyFlowView(Context context, @Nullable AttributeSet a, int s) { super(context, a, s); init(); }

    private void init() {
        gridPaint    = linePaint(COLOR_GRID);
        solarPaint   = linePaint(COLOR_SOLAR);
        batteryPaint = linePaint(COLOR_BATTERY);

        idlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        idlePaint.setStyle(Paint.Style.STROKE);
        idlePaint.setStrokeWidth(dpToPx(2.5f));
        idlePaint.setColor(COLOR_IDLE);
        idlePaint.setPathEffect(new DashPathEffect(
                new float[]{dpToPx(10), dpToPx(8)}, 0));

        gridBallPaint    = ballPaint(COLOR_GRID);
        solarBallPaint   = ballPaint(COLOR_SOLAR);
        batteryBallPaint = ballPaint(COLOR_BATTERY);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIM_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            animPhase = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private Paint linePaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dpToPx(BASE_STROKE_DP));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(color);
        return p;
    }

    private Paint ballPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        return p;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Pass node centres and radii in this view's local coordinate space.
     * The fragment computes these by calling getLocationInWindow on each card
     * and subtracting this view's window location.
     */
    public void setNodeCenters(float gridX, float gridY, float gridRadius,
                               float batteryX, float batteryY, float batteryRadius,
                               float solarX, float solarY, float solarRadius,
                               float homeX, float homeY, float homeRadius) {
        gridCx = gridX; gridCy = gridY; gridR = gridRadius;
        batCx  = batteryX; batCy = batteryY; batR = batteryRadius;
        solCx  = solarX;  solCy = solarY;  solR = solarRadius;
        homeCx = homeX;  homeCy = homeY; homeR = homeRadius;
        ready = true;
        buildPaths();
        invalidate();
    }

    public void updatePowerValues(int solar, int battery, int grid, int home) {
        solarW   = solar;
        batteryW = battery;
        gridW    = grid;
        invalidate();
    }

    // kept for compat — ignored (view is inside node_area, no scroll offset needed)
    public void setScrollOffsetY(float offset) { }

    // ── Path building ────────────────────────────────────────────────────────

    private void buildPaths() {
        if (!ready) return;

        // Hub = geometric center of the four nodes
        hubX = (gridCx + batCx + solCx + homeCx) / 4f;
        hubY = (gridCy + batCy + solCy + homeCy) / 4f;

        // Each arm: cubic bezier from node-edge toward the hub, curving
        // with a control point biased toward the hub for that smooth S-curve look.
        pathGridHub  = armPath(gridCx, gridCy, gridR,  hubX, hubY);
        pathBatHub   = armPath(batCx,  batCy,  batR,   hubX, hubY);
        pathSolarHub = armPath(solCx,  solCy,  solR,   hubX, hubY);
        pathHomeHub  = armPath(homeCx, homeCy, homeR,  hubX, hubY);
    }

    /**
     * Creates a cubic bezier path from the edge of circle (ax,ay,ar) to the hub (hx,hy).
     * Control points create the characteristic "fan" / "cross" look seen in the web version:
     * the line leaves the node perpendicular to the node→hub direction, then sweeps to hub.
     */
    private Path armPath(float ax, float ay, float ar, float hx, float hy) {
        float dx = hx - ax, dy = hy - ay;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) return new Path();

        // Unit vector from node center to hub
        float ux = dx / dist, uy = dy / dist;
        float gap = dpToPx(4f);

        // Start: on the edge of the node circle, facing hub
        float sx = ax + ux * (ar + gap);
        float sy = ay + uy * (ar + gap);

        // End: the hub point itself
        float ex = hx, ey = hy;

        // Control points: place them at 35% and 65% along, but offset
        // perpendicular to the arm direction to create the graceful curve.
        // The perpendicular offset gives the "funnel / cross" shape.
        float perpX = -uy, perpY = ux;  // perpendicular unit vector
        float curvature = dist * 0.25f; // how much the curve bows — 25% of arm length

        float cp1x = sx + ux * dist * 0.35f + perpX * curvature;
        float cp1y = sy + uy * dist * 0.35f + perpY * curvature;
        float cp2x = ex - ux * dist * 0.35f + perpX * curvature;
        float cp2y = ey - uy * dist * 0.35f + perpY * curvature;

        Path p = new Path();
        p.moveTo(sx, sy);
        p.cubicTo(cp1x, cp1y, cp2x, cp2y, ex, ey);
        return p;
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!ready || pathGridHub == null) return;

        // ── Grid arm (cyan) ─────────────────────────────────────────────────
        // gridW > 0 → energy flows grid → hub → home  (forward along arm = node→hub)
        // gridW < 0 → exporting, flow reverses         (hub → grid = backward)
        boolean gridForward = gridW >= 0;
        drawArm(canvas, pathGridHub, Math.abs(gridW),
                gridPaint, gridBallPaint, gridForward);

        // ── Solar arm (yellow) ──────────────────────────────────────────────
        // Solar always produces → flows solar → hub
        drawArm(canvas, pathSolarHub, solarW,
                solarPaint, solarBallPaint, true);

        // ── Battery arm (green) ─────────────────────────────────────────────
        // batteryW > 0 → charging: flow home→hub→bat = arm backward (hub→bat = backward from node end)
        // batteryW < 0 → discharging: flow bat→hub = forward
        boolean batForward = batteryW <= 0;
        drawArm(canvas, pathBatHub, Math.abs(batteryW),
                batteryPaint, batteryBallPaint, batForward);

        // ── Home arm (cyan, same color as grid since home receives from grid) ─
        // Home arm always shows energy flowing INTO home from hub
        int homePower = Math.abs(gridW) + solarW - Math.abs(batteryW);
        if (homePower < 0) homePower = 0;
        // Home arm direction: hub → home = backward along path (path goes node→hub)
        drawArm(canvas, pathHomeHub, homePower,
                gridPaint, gridBallPaint, false);

        // Draw hub dot where all arms meet
        drawHubDot(canvas);
    }

    private void drawArm(Canvas canvas, Path path, int watts,
                         Paint linePaint, Paint ballPaint, boolean forward) {
        if (path == null) return;
        if (Math.abs(watts) >= ACTIVE_THRESHOLD) {
            linePaint.setStrokeWidth(dpToPx(clampStroke(Math.abs(watts))));
            linePaint.setPathEffect(null);
            canvas.drawPath(path, linePaint);
            drawBalls(canvas, path, forward, ballPaint, Math.abs(watts));
        } else {
            canvas.drawPath(path, idlePaint);
        }
    }

    /** Small filled circle at the hub junction, like the web version's center dot. */
    private void drawHubDot(Canvas canvas) {
        float r = dpToPx(5f);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.parseColor("#9E9E9E")); // neutral grey dot
        canvas.drawCircle(hubX, hubY, r, p);
    }

    private void drawBalls(Canvas canvas, Path path, boolean forward,
                           Paint paint, int watts) {
        PathMeasure pm = new PathMeasure(path, false);
        float len = pm.getLength();
        if (len <= 0) return;

        float r   = dpToPx(BALL_RADIUS_DP);
        float minSp = dpToPx(38f);
        float t     = Math.min(1f, watts / 5000f);
        float spacing = Math.max(minSp, len - t * (len - minSp));

        int count = Math.max(1, (int) (len / spacing));
        float base  = animPhase * spacing;
        float[] pos = new float[2];

        for (int i = 0; i < count; i++) {
            float dist = (base + i * spacing) % len;
            if (!forward) dist = len - dist;
            dist = Math.max(0, Math.min(len - 0.1f, dist));
            if (pm.getPosTan(dist, pos, null))
                canvas.drawCircle(pos[0], pos[1], r, paint);
        }
    }

    private float clampStroke(int watts) {
        return BASE_STROKE_DP + Math.min(1f, watts / 5000f) * (MAX_STROKE_DP - BASE_STROKE_DP);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }
}

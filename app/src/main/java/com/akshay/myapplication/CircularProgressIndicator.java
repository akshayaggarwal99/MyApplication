package com.akshay.myapplication;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CircularProgressIndicator extends View {

    public static final int DIRECTION_COUNTERCLOCKWISE = 1;

    private static final int DEFAULT_PROGRESS_START_ANGLE = 270;
    private static final int ANGLE_START_PROGRESS_BACKGROUND = 0;
    private static final int ANGLE_END_PROGRESS_BACKGROUND = 360;


    private static final int DEFAULT_STROKE_WIDTH_DP = 8;

    private static final int DEFAULT_ANIMATION_DURATION = 1_000;

    private static final String PROPERTY_ANGLE = "angle";


    private Paint progressPaint;
    private Paint progressBackgroundPaint;
    private Paint blobPaint;

    private int startAngle = DEFAULT_PROGRESS_START_ANGLE;
    private int sweepAngle = 0;

    private RectF circleBounds;


    private float radius;

    private double maxProgressValue = 100.0;
    private double progressValue = 0.0;


    private int direction = DIRECTION_COUNTERCLOCKWISE;

    private ValueAnimator progressAnimator;

    @NonNull
    private Interpolator animationInterpolator = new AccelerateDecelerateInterpolator();

    public CircularProgressIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {

        int progressStrokeWidth = dp2px(DEFAULT_STROKE_WIDTH_DP);
        int progressBackgroundStrokeWidth = progressStrokeWidth;

        int blobWidth = progressStrokeWidth;

        Paint.Cap progressStrokeCap = Paint.Cap.ROUND;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressIndicator);

            progressStrokeWidth = a.getDimensionPixelSize(R.styleable.CircularProgressIndicator_progressStrokeWidth, progressStrokeWidth);
            progressBackgroundStrokeWidth = a.getDimensionPixelSize(R.styleable.CircularProgressIndicator_progressBackgroundStrokeWidth, progressStrokeWidth);

            blobWidth =  progressStrokeWidth;

            startAngle = a.getInt(R.styleable.CircularProgressIndicator_startAngle, DEFAULT_PROGRESS_START_ANGLE);
            if (startAngle < 0 || startAngle > 360) {
                startAngle = DEFAULT_PROGRESS_START_ANGLE;
            }

            direction = a.getInt(R.styleable.CircularProgressIndicator_direction, DIRECTION_COUNTERCLOCKWISE);

            progressStrokeCap = Paint.Cap.ROUND;

            a.recycle(); //Shared resource must be recycled
        }

        //initialising paints
        progressPaint = new Paint();
        progressPaint.setStrokeCap(progressStrokeCap);
        progressPaint.setStrokeWidth(progressStrokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(getResources().getColor(R.color.progressColor));
        progressPaint.setAntiAlias(true);

        Paint.Style progressBackgroundStyle = Paint.Style.STROKE;
        progressBackgroundPaint = new Paint();
        progressBackgroundPaint.setStyle(progressBackgroundStyle);
        progressBackgroundPaint.setStrokeWidth(progressBackgroundStrokeWidth);
        progressBackgroundPaint.setColor(Color.GRAY);
        progressBackgroundPaint.setAntiAlias(true);

        blobPaint = new Paint();
        blobPaint.setStrokeCap(Paint.Cap.ROUND);
        blobPaint.setStrokeWidth(blobWidth);
        blobPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        blobPaint.setColor(getResources().getColor(R.color.blobColor));
        blobPaint.setAntiAlias(true);
        blobPaint.setStrokeWidth(70);

        circleBounds = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        calculateBounds(w, h);
    }

    private void calculateBounds(int w, int h) {
        radius = w / 2f;

        float blobWidth = blobPaint.getStrokeWidth();
        float progressWidth = progressPaint.getStrokeWidth();
        float progressBackgroundWidth = progressBackgroundPaint.getStrokeWidth();
        float strokeSizeOffset = Math.max(blobWidth, Math.max(progressWidth, progressBackgroundWidth)); // to prevent blob from drawing over the bounds
        float halfOffset = strokeSizeOffset / 2f;

        circleBounds.left = halfOffset;
        circleBounds.top = halfOffset;
        circleBounds.right = w - halfOffset;
        circleBounds.bottom = h - halfOffset;

        radius = circleBounds.width() / 2f;

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgressBackground(canvas);
        drawProgress(canvas);
        drawBlob(canvas);
    }

    private void drawProgressBackground(Canvas canvas) {
        canvas.drawArc(circleBounds, ANGLE_START_PROGRESS_BACKGROUND, ANGLE_END_PROGRESS_BACKGROUND,
                false, progressBackgroundPaint);
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawArc(circleBounds, startAngle, sweepAngle, false, progressPaint);
    }

    private void drawBlob(Canvas canvas) {
        double angleRadians = Math.toRadians(startAngle + sweepAngle + 180);
        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);
        float x = circleBounds.centerX() - radius * cos;
        float y = circleBounds.centerY() - radius * sin;

        canvas.drawPoint(x, y, blobPaint);
    }


    public void setMaxProgress(double maxProgress) {
        maxProgressValue = maxProgress;
        if (maxProgressValue < progressValue) {
            setCurrentProgress(maxProgress);
        }
        invalidate();
    }

    public void setCurrentProgress(double currentProgress) {
        if (currentProgress > maxProgressValue) {
            maxProgressValue = currentProgress;
        }

        setProgress(currentProgress, maxProgressValue);
    }

    public void setProgress(double current, double max) {
        final double finalAngle;

        if (direction == DIRECTION_COUNTERCLOCKWISE) {
            finalAngle = -(current / max * 360);
        } else {
            finalAngle = current / max * 360;
        }

        double oldCurrentProgress = progressValue;

        maxProgressValue = max;
        progressValue = Math.min(current, max);

        stopProgressAnimation();

        startProgressAnimation(oldCurrentProgress, finalAngle);

    }

    private void startProgressAnimation(double oldCurrentProgress, final double finalAngle) {
        final PropertyValuesHolder angleProperty = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, sweepAngle, (int) finalAngle);

        progressAnimator = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return (startValue + (endValue - startValue) * fraction);
            }
        }, oldCurrentProgress, progressValue);
        progressAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        progressAnimator.setValues(angleProperty);
        progressAnimator.setInterpolator(animationInterpolator);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sweepAngle = (int) animation.getAnimatedValue(PROPERTY_ANGLE);
                invalidate();
            }
        });
        progressAnimator.addListener(new DefaultAnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                sweepAngle = (int) finalAngle;
                invalidate();
                progressAnimator = null;
            }
        });
        progressAnimator.start();
    }

    private void stopProgressAnimation() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    private int dp2px(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

}

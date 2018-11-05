package com.zorro.seekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pangli on 2018/11/5 15:22
 * 备注：   
 */
public class ClearCircleView extends View {
    private static final int DEF_VIEW_SIZE = 200;
    private static final int UNIT_TIME = 300;

    private Point circelCenterPoint;
    private int maxRadius;
    private ArrayList<Integer> waveColors;

    private List<Paint> paints;
    private List<CircleRadiuEntity> circleRadiusList;

    public ClearCircleView(Context context) {
        this(context, null);
    }

    public ClearCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTools();
    }

    private void initTools() {
        paints = new ArrayList<>();
        circleRadiusList = new ArrayList<>();
        waveColors = new ArrayList<>();
        waveColors.add(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        waveColors.add(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        waveColors.add(ContextCompat.getColor(getContext(), R.color.colorAccent));
        for (int i = 0; i < waveColors.size() - 1; i++) {
            Paint tempPaint = new Paint();
            tempPaint.setColor(waveColors.get(i));
            tempPaint.setAntiAlias(true);
            if (i == 1) {
                tempPaint.setStrokeWidth(5);
            } else {
                tempPaint.setStrokeWidth(5 + 5 * i);
            }
            tempPaint.setStrokeJoin(Paint.Join.ROUND);
            tempPaint.setStrokeCap(Paint.Cap.ROUND);
            tempPaint.setStyle(Paint.Style.STROKE);
            paints.add(tempPaint);
            CircleRadiuEntity c = new CircleRadiuEntity();
            circleRadiusList.add(c);
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        circelCenterPoint = new Point(w / 2, h / 2);
        maxRadius = Math.min(w, h) / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthMeaure;
        int heightMeaure;

        if (widthMeasureMode == MeasureSpec.AT_MOST || widthMeasureMode == MeasureSpec.UNSPECIFIED) {
            widthMeaure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources()
                    .getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthMeaure, MeasureSpec.EXACTLY);
        }

        if (heightMeasureMode == MeasureSpec.AT_MOST || heightMeasureMode == MeasureSpec.UNSPECIFIED) {
            heightMeaure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_SIZE, getResources()
                    .getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeaure, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint tempPaint = new Paint();
        tempPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        tempPaint.setAntiAlias(true);
        tempPaint.setStrokeWidth(5);
        tempPaint.setStrokeJoin(Paint.Join.ROUND);
        tempPaint.setStrokeCap(Paint.Cap.ROUND);
        tempPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(circelCenterPoint.x, circelCenterPoint.y, 50, tempPaint);
        for (int i = 0; i < paints.size(); i++) {
            Paint currentCirclePaint = paints.get(i);
            int currentCircleRadius = circleRadiusList.get(i).getRadius();
            canvas.drawCircle(circelCenterPoint.x, circelCenterPoint.y, currentCircleRadius, currentCirclePaint);
        }
    }

    public void startWave() {
        for (int i = 0; i < waveColors.size() - 1; i++) {
            ValueAnimator valueAnimatorC = ValueAnimator.ofFloat(0F, 1F);
            final int finalI = i;
            valueAnimatorC.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float aFloat = Float.valueOf(animation.getAnimatedValue().toString());
                    circleRadiusList.get(finalI).setRadius((int) (50 + 50 * aFloat));
                    invalidate();
                }
            });
            valueAnimatorC.setRepeatCount(1);
            valueAnimatorC.setRepeatMode(ValueAnimator.REVERSE);
            valueAnimatorC.setDuration(UNIT_TIME * waveColors.size());
            valueAnimatorC.setStartDelay(i * UNIT_TIME);
            valueAnimatorC.start();
        }
    }

    class CircleRadiuEntity {
        int radius;

        int getRadius() {
            return radius;
        }

        void setRadius(int radius) {
            this.radius = radius;
        }
    }
}


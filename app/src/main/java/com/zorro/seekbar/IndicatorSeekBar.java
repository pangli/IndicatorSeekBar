package com.zorro.seekbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.math.BigDecimal;


/**
 * Created by pangli on 2018/11/5 15:22
 * 备注：   
 */
public class IndicatorSeekBar extends View {

    private float mMin; // min
    private float mMax; // max
    private float mProgress; // real time value
    private boolean isFloatType; // support for float type output
    private LinearGradient linearGradient;
    private int[] colorGradient;
    private int mTrackSize; // height of right-track(on the right of thumb)
    private int mThumbRadius; // radius of thumb
    private int mThumbRadiusOnDragging; // radius of thumb when be dragging
    private int mThumbColor; // color of thumb
    private int mThumbTextSize; // text size of progress-text
    private int mThumbTextColor; // text color of progress-text
    private int mSectionCount; // shares of whole progress(max - min)
    private boolean isAutoAdjustSectionMark; // auto scroll to the nearest section_mark or not
    private boolean isShowProgressInFloat; // show bubble-progress in float or not
    private boolean isTouchToSeek; // touch anywhere on track to quickly seek
    private long mAnimDuration; // duration of animation

    private float mDelta; // max - min
    private float mSectionValue; // (mDelta / mSectionCount)
    private float mThumbCenterX; // X coordinate of thumb's center
    private float mTrackLength; // pixel length of whole track
    private float mSectionOffset; // pixel length of one section
    private boolean isThumbOnDragging; // is thumb on dragging or not

    private OnProgressChangedListener mProgressListener; // progress changing listener
    private float mLeft; // space between left of track and left of the view
    private float mRight; // space between right of track and left of the view
    private Paint mPaint, mCirclePaint, mTextPaint;
    private Rect mRectText;

    public IndicatorSeekBar(Context context) {
        this(context, null);
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicatorSeekBar, defStyleAttr, 0);
        mMin = a.getFloat(R.styleable.IndicatorSeekBar_isb_min, 1.0f);
        mMax = a.getFloat(R.styleable.IndicatorSeekBar_isb_max, 6.0f);
        mProgress = a.getFloat(R.styleable.IndicatorSeekBar_isb_progress, mMin);
        isFloatType = a.getBoolean(R.styleable.IndicatorSeekBar_isb_is_float_type, false);
        mTrackSize = a.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_track_size, dp2px(2));
        mThumbRadius = a.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_thumb_radius, mTrackSize * 2);
        mThumbRadiusOnDragging = a.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_thumb_radius_on_dragging,
                mThumbRadius + dp2px(4));
        mSectionCount = (int) (mMax - mMin);
        mThumbColor = a.getColor(R.styleable.IndicatorSeekBar_isb_thumb_color, Color.RED);
        mThumbTextSize = a.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_thumb_text_size, sp2px(14));
        mThumbTextColor = a.getColor(R.styleable.IndicatorSeekBar_isb_thumb_text_color, Color.RED);
        isAutoAdjustSectionMark = a.getBoolean(R.styleable.IndicatorSeekBar_isb_auto_adjust_section_mark, false);
        isShowProgressInFloat = a.getBoolean(R.styleable.IndicatorSeekBar_isb_show_progress_in_float, false);
        int duration = a.getInteger(R.styleable.IndicatorSeekBar_isb_anim_duration, -1);
        mAnimDuration = duration < 0 ? 200 : duration;
        isTouchToSeek = a.getBoolean(R.styleable.IndicatorSeekBar_isb_touch_to_seek, false);
        setEnabled(a.getBoolean(R.styleable.IndicatorSeekBar_android_enabled, isEnabled()));
        a.recycle();

        colorGradient = new int[]{ContextCompat.getColor(context, R.color.color_69FBEC), ContextCompat.getColor
                (context, R.color.color_1895F9), ContextCompat.getColor(context, R.color.color_5718B3)};
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mCirclePaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mRectText = new Rect();

        initConfigByPriority();
    }

    private void initConfigByPriority() {
        if (mMin == mMax) {
            mMin = 1.0f;
            mMax = 6.0f;
        }
        if (mMin > mMax) {
            float tmp = mMax;
            mMax = mMin;
            mMin = tmp;
        }
        if (mProgress < mMin) {
            mProgress = mMin;
        }
        if (mProgress > mMax) {
            mProgress = mMax;
        }

        if (mSectionCount <= 0) {
            mSectionCount = 6;
        }
        mDelta = mMax - mMin;
        mSectionValue = mDelta / mSectionCount;

        if (mSectionValue < 1) {
            isFloatType = true;
        }
        if (isFloatType) {
            isShowProgressInFloat = true;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = mThumbRadiusOnDragging * 2; // 默认高度为拖动时thumb圆的直径
        setMeasuredDimension(resolveSize(dp2px(180), widthMeasureSpec), height);
        mLeft = getPaddingLeft() + mThumbRadiusOnDragging;
        mRight = getMeasuredWidth() - getPaddingRight() - mThumbRadiusOnDragging;
        mTrackLength = mRight - mLeft;
        mSectionOffset = mTrackLength * 1f / mSectionCount;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float xLeft = mLeft;
        float xRight = mRight;
        float yTop = getPaddingTop() + mThumbRadiusOnDragging;

        // draw track
        if (linearGradient == null) {
            linearGradient = new LinearGradient(xLeft, yTop, xRight, yTop, colorGradient, null, Shader.TileMode.MIRROR);
        }
        mPaint.setShader(linearGradient);
        mPaint.setStrokeWidth(mTrackSize);
        canvas.drawLine(xLeft, yTop, xRight, yTop, mPaint);

        // draw step
        float x_;
        float r = (mTrackSize + dp2px(2)) / 2f;
        for (int i = 1; i <= mSectionCount - 1; i++) {
            x_ = xLeft + i * mSectionOffset;
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setColor(Color.WHITE);
            canvas.drawCircle(x_, yTop, r, mCirclePaint);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
            canvas.drawCircle(x_, yTop, r, mPaint);
        }

        //draw sectionMark
        if (!isThumbOnDragging) {
            mThumbCenterX = xLeft + mTrackLength / mDelta * (mProgress - mMin);
        }
        canvas.drawCircle(mThumbCenterX, yTop, mThumbRadius, mCirclePaint);
        //canvas.drawCircle(mThumbCenterX, yTop, isThumbOnDragging ? mThumbRadiusOnDragging : mThumbRadius,
        // mCirclePaint);
        mCirclePaint.setColor(mThumbColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(2);
        //canvas.drawCircle(mThumbCenterX, yTop, isThumbOnDragging ? mThumbRadiusOnDragging : mThumbRadius, mPaint);
        canvas.drawCircle(mThumbCenterX, yTop, mThumbRadius, mCirclePaint);

        //draw Progress text
        mTextPaint.setTextSize(mThumbTextSize);
        mTextPaint.setColor(mThumbTextColor);
        mTextPaint.getTextBounds(String.valueOf(getProgress()), 0, String.valueOf(getProgress()).length(), mRectText);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float baseline = yTop + (fm.descent - fm.ascent) / 2f - fm.descent;
        canvas.drawText(String.valueOf(getProgress()), mThumbCenterX, baseline, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    float dx;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                getParent().requestDisallowInterceptTouchEvent(true);
                isThumbOnDragging = isThumbTouched(event);
                if (isThumbOnDragging) {
                    invalidate();
                } else if (isTouchToSeek && isTrackTouched(event)) {
                    isThumbOnDragging = true;
                    mThumbCenterX = event.getX();
                    if (mThumbCenterX < mLeft) {
                        mThumbCenterX = mLeft;
                    }
                    if (mThumbCenterX > mRight) {
                        mThumbCenterX = mRight;
                    }
                    mProgress = calculateProgress();
                    invalidate();
                }
                dx = mThumbCenterX - event.getX();

                break;
            case MotionEvent.ACTION_MOVE:
                if (isThumbOnDragging) {
                    boolean flag = true;
                    mThumbCenterX = event.getX() + dx;
                    if (mThumbCenterX < mLeft) {
                        mThumbCenterX = mLeft;
                    }
                    if (mThumbCenterX > mRight) {
                        mThumbCenterX = mRight;
                    }
                    if (flag) {
                        mProgress = calculateProgress();
                        invalidate();
                        if (mProgressListener != null) {
                            mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                if (isAutoAdjustSectionMark) {
                    if (isTouchToSeek) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                autoAdjustSection();
                            }
                        }, mAnimDuration);
                    } else {
                        autoAdjustSection();
                    }
                }
                if (mProgressListener != null) {
                    mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
                    mProgressListener.getProgressOnActionUp(this, getProgress(), getProgressFloat());
                }

                break;
        }

        return isThumbOnDragging || isTouchToSeek || super.onTouchEvent(event);
    }

    /**
     * Detect effective touch of thumb
     */
    private boolean isThumbTouched(MotionEvent event) {
        if (!isEnabled())
            return false;

        float distance = mTrackLength / mDelta * (mProgress - mMin);
        float x = mLeft + distance;
        float y = getMeasuredHeight() / 2f;
        return (event.getX() - x) * (event.getX() - x) + (event.getY() - y) * (event.getY() - y)
                <= (mLeft + dp2px(8)) * (mLeft + dp2px(8));
    }

    /**
     * Detect effective touch of track
     */
    private boolean isTrackTouched(MotionEvent event) {
        return isEnabled() && event.getX() >= getPaddingLeft() && event.getX() <= getMeasuredWidth() - getPaddingRight()
                && event.getY() >= getPaddingTop() && event.getY() <= getMeasuredHeight() - getPaddingBottom();
    }

    /**
     * Auto scroll to the nearest section mark
     */
    private void autoAdjustSection() {
        int i;
        float x = 0;
        for (i = 0; i <= mSectionCount; i++) {
            x = i * mSectionOffset + mLeft;
            if (x <= mThumbCenterX && mThumbCenterX - x <= mSectionOffset) {
                break;
            }
        }

        BigDecimal bigDecimal = BigDecimal.valueOf(mThumbCenterX);
        float x_ = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        boolean onSection = x_ == x; // 就在section处，不作valueAnim，优化性能

        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator valueAnim = null;
        if (!onSection) {
            if (mThumbCenterX - x <= mSectionOffset / 2f) {
                valueAnim = ValueAnimator.ofFloat(mThumbCenterX, x);
            } else {
                valueAnim = ValueAnimator.ofFloat(mThumbCenterX, (i + 1) * mSectionOffset + mLeft);
            }
            valueAnim.setInterpolator(new LinearInterpolator());
            valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mThumbCenterX = (float) animation.getAnimatedValue();
                    mProgress = calculateProgress();
                    invalidate();
                    if (mProgressListener != null) {
                        mProgressListener.onProgressChanged(IndicatorSeekBar.this, getProgress(),
                                getProgressFloat(), true);
                    }
                }
            });
        }
        if (!onSection) {
            animatorSet.setDuration(mAnimDuration).playTogether(valueAnim);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgress = calculateProgress();
                isThumbOnDragging = false;
                invalidate();
                if (mProgressListener != null) {
                    mProgressListener.getProgressOnFinally(IndicatorSeekBar.this, getProgress(),
                            getProgressFloat(), true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mProgress = calculateProgress();
                isThumbOnDragging = false;
                invalidate();
            }
        });
        animatorSet.start();
    }

    private String float2String(float value) {
        return String.valueOf(formatFloat(value));
    }

    private float formatFloat(float value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }


    private float calculateProgress() {

        return (mThumbCenterX - mLeft) * mDelta / mTrackLength + mMin;
    }

    /////// Api begins /////////////////////////////////////////////////////////////////////////////


    public float getMin() {
        return mMin;
    }

    public float getMax() {
        return mMax;
    }

    public void setProgress(float progress) {
        mProgress = progress;

        if (mProgressListener != null) {
            mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), false);
            mProgressListener.getProgressOnFinally(this, getProgress(), getProgressFloat(), false);
        }
        postInvalidate();
    }

    public int getProgress() {
        return Math.round(mProgress);
    }

    public float getProgressFloat() {
        return formatFloat(mProgress);
    }

    public OnProgressChangedListener getOnProgressChangedListener() {
        return mProgressListener;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        mProgressListener = onProgressChangedListener;
    }

    public void setThumbColor(@ColorInt int thumbColor) {
        if (mThumbColor != thumbColor) {
            mThumbColor = thumbColor;
            invalidate();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("save_instance", super.onSaveInstanceState());
        bundle.putFloat("progress", mProgress);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mProgress = bundle.getFloat("progress");
            super.onRestoreInstanceState(bundle.getParcelable("save_instance"));
            setProgress(mProgress);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * Listen to progress onChanged, onActionUp, onFinally
     */
    public interface OnProgressChangedListener {

        void onProgressChanged(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat, boolean
                fromUser);

        void getProgressOnActionUp(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat);

        void getProgressOnFinally(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat, boolean
                fromUser);
    }

    /**
     * Listener adapter
     * <br/>
     * usage like {@link AnimatorListenerAdapter}
     */
    public static abstract class OnProgressChangedListenerAdapter implements OnProgressChangedListener {

        @Override
        public void onProgressChanged(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat, boolean
                fromUser) {
        }

        @Override
        public void getProgressOnActionUp(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat) {
        }

        @Override
        public void getProgressOnFinally(IndicatorSeekBar indicatorSeekBar, int progress, float progressFloat,
                                         boolean
                                                 fromUser) {
        }
    }

    int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem()
                .getDisplayMetrics());
    }

    int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }
}

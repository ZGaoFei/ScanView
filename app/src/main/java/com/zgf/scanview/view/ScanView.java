package com.zgf.scanview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.zgf.scanview.R;


public class ScanView extends View {
    private static final float DURATION_COE_DEFAULT = 1.0F;
    private static final int BG_COE_DEFAULT = 2;
    private Paint mScanLinePaint;

    private int mViewWidth;
    private int mViewHeight;

    private ValueAnimator mLineAnimator;
    private ValueAnimator mBgAnimator;
    private ValueAnimator mBgAnimator2;

    private int mLinePosition;
    private int mBgPosition;

    private float mDurationCoe = DURATION_COE_DEFAULT; // 动画执行的时间系数
    private int mBgCoe = BG_COE_DEFAULT; // 后面背景的系数，占view高度的比例

    private boolean mIsDown = true; // 动画向下运行

    public enum ScanOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private ScanOrientation mOrientation = ScanOrientation.VERTICAL;

    private int[] mUpColors = new int[]{
            ContextCompat.getColor(getContext(), R.color.color_FF7FFF00),
            ContextCompat.getColor(getContext(), R.color.color_887FFF00),
            ContextCompat.getColor(getContext(), R.color.color_007FFF00)};
    private int[] mDownColors = new int[]{
            ContextCompat.getColor(getContext(), R.color.color_007FFF00),
            ContextCompat.getColor(getContext(), R.color.color_887FFF00),
            ContextCompat.getColor(getContext(), R.color.color_FF7FFF00)};

    public ScanView(Context context) {
        this(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScanLinePaint = new Paint();
        mScanLinePaint.setAntiAlias(true);
        mScanLinePaint.setStrokeWidth(5);
        mScanLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int w;
        int h;
        if (widthMode == MeasureSpec.EXACTLY) {
            w = width;
        } else {
            w = 100; // wrap_content宽度
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            h = height;
        } else {
            h = 100; // wrap_content高度
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        lineAnimator();
        bgAnimator();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // give parent view group deal
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawLine(canvas);

        drawTransparentRect(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 此方法比onMeasure方法更早的运行，因此不能将启动动画放在此方法中
        // lineAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        endAnimator();
    }

    private void drawLine(Canvas canvas) {
        // 将横线的颜色设置为背景色一致
        if (mUpColors != null) {
            mScanLinePaint.setColor(mUpColors[0]);
        }
        if (mOrientation == ScanOrientation.VERTICAL) {
            if (mLinePosition + 10 > mViewHeight) {
                canvas.drawRect(0, mViewHeight - 10, mViewWidth, mViewHeight, mScanLinePaint);
            } else if (mLinePosition < 10) {
                canvas.drawRect(0, 0, mViewWidth, 10, mScanLinePaint);
            } else {
                canvas.drawRect(0, mLinePosition, mViewWidth, mLinePosition + 10, mScanLinePaint);
            }
        } else {
            if (mLinePosition + 10 > mViewWidth) {
                canvas.drawRect(mViewWidth - 10, 0, mViewWidth, mViewHeight, mScanLinePaint);
            } else if (mLinePosition < 10) {
                canvas.drawRect(0, 0, 10, mViewHeight, mScanLinePaint);
            } else {
                canvas.drawRect(mLinePosition, 0, mLinePosition + 10, mViewHeight, mScanLinePaint);
            }
        }
    }

    private void drawTransparentRect(Canvas canvas) {
        int bgParams;
        if (mOrientation == ScanOrientation.VERTICAL) {
            bgParams = mViewHeight / mBgCoe;
        } else {
            bgParams = mViewWidth / mBgCoe;
        }
        int startPosition;
        int endPosition;
        int[] colors;
        if (mIsDown) {
            startPosition = mBgPosition - bgParams;
            endPosition = mBgPosition;
            colors = mDownColors;
        } else {
            startPosition = mBgPosition + 10;
            endPosition = mBgPosition + bgParams + 10;
            colors = mUpColors;
        }
        LinearGradient linearGradient;
        if (mOrientation == ScanOrientation.VERTICAL) {
            linearGradient = new LinearGradient(0, startPosition, 0, endPosition, colors, null, Shader.TileMode.CLAMP);
            mScanLinePaint.setShader(linearGradient);
            canvas.drawRect(0, startPosition, mViewWidth, endPosition, mScanLinePaint);
        } else {
            linearGradient = new LinearGradient(startPosition, 0, endPosition, 0, colors, null, Shader.TileMode.CLAMP);
            mScanLinePaint.setShader(linearGradient);
            canvas.drawRect(startPosition, 0, endPosition, mViewHeight, mScanLinePaint);
        }
    }

    /**
     * 动画在onAttachedToWindow中执行会比onMeasure执行的快，
     * 因此无法获取view的高度
     */
    private void lineAnimator() {
        int a;
        if (mOrientation == ScanOrientation.VERTICAL) {
            a = mViewHeight;
        } else {
            a = mViewWidth;
        }
        if (mLineAnimator == null) {
            mLineAnimator = ValueAnimator.ofInt(0, a);
            mLineAnimator.setDuration(getDuration(a));
            mLineAnimator.setInterpolator(new LinearInterpolator());
            mLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mLinePosition = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
        } else {
            if (mIsDown) {
                mLineAnimator.setIntValues(0, a);
            } else {
                mLineAnimator.setIntValues(a, 0);
            }
            mLineAnimator.setDuration(getDuration(a));
        }
        mLineAnimator.start();
    }

    private void bgAnimator() {
        final int a;
        final int coe;
        if (mOrientation == ScanOrientation.VERTICAL) {
            coe = mViewHeight / mBgCoe;
            a = mViewHeight + coe;
        } else {
            coe = mViewWidth / mBgCoe;
            a = mViewWidth + coe;
        }
        if (mBgAnimator == null) {
            mBgAnimator = ValueAnimator.ofInt(0, a);
            mBgAnimator.setDuration(getDuration(a));
            mBgAnimator.setInterpolator(new LinearInterpolator());
            mBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mBgPosition = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
            mBgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsDown = !mIsDown;
                    lineAnimator();
                    bgAnimator2();
                }
            });
        } else {
            mBgAnimator.setIntValues(0, a);
            mBgAnimator.setDuration(getDuration(a));
        }
        mBgAnimator.start();
    }

    private void bgAnimator2() {
        final int a;
        final int coe;
        if (mOrientation == ScanOrientation.VERTICAL) {
            coe = mViewHeight / mBgCoe;
            a = mViewHeight;
        } else {
            coe = mViewWidth / mBgCoe;
            a = mViewWidth;
        }
        if (mBgAnimator2 == null) {
            mBgAnimator2 = ValueAnimator.ofInt(a, -coe);
            mBgAnimator2.setDuration(getDuration(a + coe));
            mBgAnimator2.setInterpolator(new LinearInterpolator());
            mBgAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mBgPosition = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
            mBgAnimator2.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsDown = !mIsDown;

                    lineAnimator();
                    bgAnimator();
                }
            });
        } else {
            mBgAnimator2.setIntValues(a, -coe);
            mBgAnimator2.setDuration(getDuration(a + coe));
        }
        mBgAnimator2.start();
    }

    public void setDurationCoe(float durationCoe) {
        if (durationCoe > 0) {
            this.mDurationCoe = durationCoe;
        }
    }

    public void setBgCoe(int bgCoe) {
        if (bgCoe > 0) {
            this.mBgCoe = bgCoe;
        }
    }

    public void setOrientation(ScanOrientation orientation) {
        if (orientation != null && orientation != mOrientation) {
            mOrientation = orientation;
            if (mLineAnimator != null && mLineAnimator.isRunning()) {
                mLineAnimator.end();
            }
            if (mBgAnimator != null && mBgAnimator.isRunning()) {
                mBgAnimator.end();
            }
            lineAnimator();
            if (mIsDown) {
                bgAnimator();
            } else {
                bgAnimator2();
            }
        }
    }

    private int getDuration(int height) {
        return (int) (height * mDurationCoe);
    }

    private void endAnimator() {
        if (mLineAnimator != null) {
            mLineAnimator.cancel();
            mLineAnimator.removeAllUpdateListeners();
        }
        mLineAnimator = null;

        if (mBgAnimator != null) {
            mBgAnimator.cancel();
            mBgAnimator.removeAllUpdateListeners();
        }
        mBgAnimator = null;

        if (mBgAnimator2 != null) {
            mBgAnimator2.cancel();
            mBgAnimator2.removeAllUpdateListeners();
        }
        mBgAnimator2 = null;
    }

    public void setBgColor(int... colors) {
        if (colors == null) {
            return;
        }
        mUpColors = new int[colors.length];
        mUpColors = colors;
        int[] cls = new int[colors.length];
        int j = 0;
        for (int i = colors.length - 1; i >= 0; i--) {
            cls[j] = colors[i];
            j++;
        }
        mDownColors = new int[colors.length];
        mDownColors = cls;
    }

}

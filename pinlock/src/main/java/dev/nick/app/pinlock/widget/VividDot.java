package dev.nick.app.pinlock.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.List;

import dev.nick.app.pinlock.R;
import dev.nick.app.pinlock.secure.PinKey;
import dev.nick.app.pinlock.secure.VividDotListener;
import dev.nick.app.pinlock.utils.Lists;
import dev.nick.app.pinlock.utils.Logger;
import dev.nick.app.pinlock.utils.PreferenceHelper;
import dev.nick.app.pinlock.utils.ViewAnimatorUtil;

/**
 * Draw dots when user input pin key.
 */
public class VividDot extends View {

    private int mRadius;
    private int mDotPadding;
    private float mCurrDotScale;
    private int mCurrDotCt;
    private InputMode mInputOp = InputMode.None;
    private int mMaxDotCount;
    private List<Dot> mDots;
    private int mWidth, mHeight;
    private Paint mDotPaint, mEmptyDotPaint;
    private VividDotListener mDotListener;

    private VividDot(Context context) {
        super(context);
    }

    public VividDot(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrAndInit(context, attrs);
    }

    public VividDot(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrAndInit(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VividDot(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttrAndInit(context, attrs);
    }

    public void setListener(VividDotListener listener) {
        mDotListener = listener;
    }

    private void parseAttrAndInit(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.dot);
        int dotColor = array.getColor(R.styleable.dot_color, Color.WHITE);
        mRadius = array.getDimensionPixelSize(R.styleable.dot_size, 15);
        mDotPadding = array.getDimensionPixelSize(R.styleable.dot_padding, 30);
        int shadowRadius = array.getDimensionPixelSize(R.styleable.dot_shadowSize, 3);
        mMaxDotCount = array.getInteger(R.styleable.dot_count, 4);
        mMaxDotCount = new PreferenceHelper(context).complexPwd() ? 6 : 4;
        array.recycle();

        mDotPaint = new Paint();
        mDotPaint.setColor(dotColor);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setAntiAlias(true);
        mDotPaint.setShadowLayer(shadowRadius, 2, 2, Color.GRAY);
        mEmptyDotPaint = new Paint(mDotPaint);
        mEmptyDotPaint.setStyle(Paint.Style.STROKE);
        mDots = Lists.newArrayList(mMaxDotCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
        Logger.i("onMeasure, w-h-" + mWidth + "-" + mHeight, getClass());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0) mWidth = getWidth();
        if (mHeight == 0) mHeight = getHeight();
        float startX = ((float) mWidth - (mMaxDotCount - 1f) * ((float) mDotPadding + (float) mRadius * 2f)) / 2f;
        float startY = (float) mHeight / 2f;

        for (int i = 0; i < mMaxDotCount; i++) {
            drawStroke(canvas, startX, startY, i);
        }

        for (Dot d : mDots) {
            boolean draw = (d.index < mCurrDotCt)
                    || (d.index == mCurrDotCt && mInputOp == InputMode.Remove);
            if (draw) {
                drawDot(canvas, startX, startY, d);
            }
        }
    }

    private void drawDot(Canvas canvas, float xStart, float yStart, Dot dot) {
        boolean animate = (dot.index == mCurrDotCt - 1 && mInputOp == InputMode.Add)
                || (dot.index == mCurrDotCt && mInputOp == InputMode.Remove) || (mInputOp == InputMode.Clear);
        if (animate) {
            canvas.drawCircle(xStart + dot.index * (mRadius * 2 + mDotPadding), yStart, mRadius * mCurrDotScale, mDotPaint);
        } else {
            canvas.drawCircle(xStart + dot.index * (mRadius * 2 + mDotPadding), yStart, mRadius, mDotPaint);
        }
    }

    private void drawStroke(Canvas canvas, float xStart, float yStart, int index) {
        canvas.drawCircle(xStart + index * (mRadius * 2 + mDotPadding), yStart, mRadius, mEmptyDotPaint);
    }

    /**
     * @return True if successfully added a pin key.
     */
    public synchronized boolean addPin(PinKey key) {
        if (mCurrDotCt == mMaxDotCount) {
            if (mDotListener != null) {
                mDotListener.onOutOfBound();
            }
            return false;
        }
        mInputOp = InputMode.Add;
        Dot dot = new Dot(mCurrDotCt, key);
        mDots.add(dot);
        upDotCount();
        startDotAnimator();
        if (mDotListener != null) {
            mDotListener.onPinKeyAdded(key);
        }
        return true;
    }

    /**
     * @return True if successfully backspaced from dots.
     */
    public synchronized boolean removeLastDigit() {
        if (mCurrDotCt == 0) return false;
        mInputOp = InputMode.Remove;
        downDotCount();
        startDotAnimator();
        return true;
    }

    /**
     * @return True if successfully cleared the dots.
     */
    public synchronized boolean clearDots() {
        mInputOp = InputMode.Clear;
        mCurrDotCt = 0;
        mDots.clear();
        startDotAnimator();
        return true;
    }

    private void startDotAnimator() {
        Logger.i("startDotAnimator..", getClass());
        ValueAnimator v;
        if (mInputOp == InputMode.Add)
            v = ValueAnimator.ofFloat(0f, 1f);
        else {
            v = ValueAnimator.ofFloat(1f, 0f);
        }
        long duration = mInputOp == InputMode.Clear ? 300 : 100;
        v.setDuration(duration);
        v.setInterpolator(new LinearInterpolator());
        v.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                mCurrDotScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        v.start();
    }

    private synchronized void upDotCount() {
        mCurrDotCt++;
        Logger.i("up dot ct to: " + mCurrDotCt, getClass());
    }

    private synchronized void downDotCount() {
        mCurrDotCt--;
        Logger.i("down dot ct to: " + mCurrDotCt, getClass());
    }

    /**
     * Called by the ObjectAnimator.
     */
    public void setColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(getResources().getDrawable(colorId));//FIXME
        }
    }

    /**
     * Called to set display with a animation update.
     *
     * @param displayMode The display mode you want to set.
     */
    public void setDisplayMode(DisplayMode displayMode, final boolean clearDots) {
        switch (displayMode) {
            case Correct:
                break;
            case Wrong:
                ViewAnimatorUtil.animateColorChange(this,
                        R.color.green, R.color.red, ViewAnimatorUtil.DURATION_SHORT, new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                // Do nothing.
                            }

                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (clearDots) clearDots();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                // Do nothing.
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                // Do nothing.
                            }
                        });
                break;
        }
    }

    public String convertDots() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mCurrDotCt; i++) {
            Dot dot = mDots.get(i);
            if (dot.key == PinKey.NULL) break;
            sb.append(dot.key.toString());
        }
        return sb.toString();
    }

    enum InputMode {
        Add,
        Remove,
        Clear,
        None
    }

    public enum DisplayMode {
        Correct,
        Wrong
    }

    class Dot {
        int index;
        PinKey key;

        Dot(int index, PinKey key) {
            this.index = index;
            this.key = key;
        }
    }
}

package dev.nick.app.pinlock;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.nick.app.pinlock.secure.PinKey;
import dev.nick.app.pinlock.secure.VividDotListener;
import dev.nick.app.pinlock.utils.Logger;
import dev.nick.app.pinlock.utils.PreferenceHelper;
import dev.nick.app.pinlock.utils.ViewAnimatorUtil;
import dev.nick.app.pinlock.widget.DigitButton;
import dev.nick.app.pinlock.widget.VividDot;

import static android.content.Context.WINDOW_SERVICE;


public class PinLockStub implements VividDotListener {

    private PreferenceHelper mPrefHelper;

    private RelativeLayout mPinLayout;
    private VividDot mDotArea;
    private TextView mTipView;
    private ImageView mLogo;
    private String mLastInputPwd;

    private Context mContext;

    private LockInfo mInfo;
    private Listener mListener;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.backspace) {
                onBackspaceClicked();

            } else if (id == R.id.okay) {
                onOkayClicked();

            } else {// Check again...
                if (v instanceof DigitButton) {
                    onDigitBtnClicked((DigitButton) v);
                }

            }
        }
    };

    public PinLockStub(Context context, LockInfo info, Listener listener) {
        this.mContext = context;
        this.mInfo = info;
        this.mListener = listener;
        this.mPrefHelper = new PreferenceHelper(mContext);
    }

    private void initView() {
        if (mPinLayout == null) {
            mPinLayout = (RelativeLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.layout_pin_pad, null, false);
            mDotArea = getView(mPinLayout, R.id.dot_area);
            mTipView = getView(mPinLayout, R.id.tip_view);
            mLogo = getView(mPinLayout, R.id.logo);
            mLogo.setImageDrawable(mInfo.icon);
            mDotArea.setListener(this);
            // set mListener for all buttons ugly.
            getView(mPinLayout, R.id.digit_9).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_8).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_7).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_6).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_5).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_4).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_3).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_2).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_1).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.digit_0).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.okay).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.backspace).setOnClickListener(mButtonClickListener);
            getView(mPinLayout, R.id.backspace).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mDotArea.clearDots();
                }
            });
        }
        if (mPrefHelper.isPwdSet()) {
            setTipText(R.string.tips_pin_choose, 0);
        } else {
            setTipText(R.string.tips_enter_pin, 0);
        }
    }

    public String getStoredPwd() {
        return mPrefHelper.getStoredPwd();
    }

    /**
     * Add pin pad to decor.
     *
     * @param animate True if you wanna show an animation during the add.
     */
    private void addPinView(boolean animate) {
        initView();
        if (mPinLayout.getWindowToken() != null) {
            return;
        }
        ViewGroup.LayoutParams params
                = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        final WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        windowManager.addView(mPinLayout, params);
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPinLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onViewAttachedToWindow(View view) {
                    ViewAnimatorUtil.circularShow(mPinLayout, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }

                @Override
                public void onViewDetachedFromWindow(View view) {

                }
            });
        }
    }

    /**
     * Remove pin pad from decor.
     *
     * @param animate True if you wanna show an animation during the remove.
     */
    private synchronized void removePinView(boolean animate) {
        disableOkayBtn();
        final WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewAnimatorUtil.circularHide(mPinLayout, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(mPinLayout);
                    mListener.onDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            windowManager.removeView(mPinLayout);
            mListener.onDismiss();
        }

    }

    private void onBackspaceClicked() {
        mDotArea.removeLastDigit();
    }

    private void onOkayClicked() {
        String input = mDotArea.convertDots();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        Logger.i("onOkayClicked, input = " + input, getClass());
        if (mPrefHelper.isPwdSet()) {
            if (mLastInputPwd == null) {
                mLastInputPwd = input;
                mDotArea.clearDots();
                setTipText(R.string.tips_pin_confirm, 0);
                Logger.i("Record the input: " + input, getClass());
            } else {
                if (input.equals(mLastInputPwd)) {
                    mDotArea.clearDots();
                    mPrefHelper.updatePwd(input);
                    onPinCodeSaved(input);
                    removePinView(true);
                } else {
                    onPinNotMatch(input);
                }
            }
        } else {
            String pwd = mPrefHelper.getStoredPwd();
            if (input.equals(pwd)) {
                mDotArea.clearDots();
                onPinCorrectInternal();
            } else {
                mDotArea.clearDots();
                onPinCodeError(input);
            }
        }
    }

    private void onDigitBtnClicked(DigitButton button) {
        mDotArea.addPin(button.getPinKey());
    }

    @Override
    public void onPinKeyAdded(PinKey added) {
        // EMPTY.
    }

    @Override
    public void onOutOfBound() {
        // EMPTY.
    }

    private void onPinCodeSaved(String pin) {
        Logger.i("onPinCodeSaved: " + pin, getClass());
        mPrefHelper.onPwdSet();
    }

    /**
     * Called when the pin user input matches the right.
     */
    private void onPinCorrectInternal() {
        Logger.i("onPinCorrectInternal: ", getClass());
        removePinView(true);
    }

    private void onPinCorrect() {
        unLock();
    }

    /**
     * Called when the pin user input is not match with the previous when record.
     *
     * @param pin The pin that user give.
     */
    private void onPinNotMatch(String pin) {
        Logger.i("onPinNotMatch: " + pin, getClass());
        setTipText(R.string.tips_pin_no_match, R.string.tips_pin_choose);
        mLastInputPwd = null;
    }

    /**
     * Called when check pwd, and the pwd user input is not correct.
     *
     * @param pin The pin that user give.
     */
    private void onPinCodeError(String pin) {
        mDotArea.setDisplayMode(VividDot.DisplayMode.Wrong, true);
        setTipText(R.string.tips_pin_wrong, R.string.tips_enter_pin);
    }

    /**
     * Update the tips view content.
     *
     * @param text          The text to set as current.
     * @param textToRestore The text to restore when after the animation,
     *                      accept ZERO if you do not want to restore.
     */
    private void setTipText(int text, final int textToRestore) {
        if (textToRestore > 0)
            ViewAnimatorUtil.animateTextChange(mTipView, text, new Runnable() {
                @Override
                public void run() {
                    ViewAnimatorUtil.animateTextChange(mTipView, textToRestore, null);
                }
            });
        else {
            mTipView.setText(text);
        }
    }

    private void enableOkayBtn() {
        getView(mPinLayout, R.id.okay).setEnabled(true);
    }

    private void disableOkayBtn() {
        getView(mPinLayout, R.id.okay).setEnabled(false);
    }

    /**
     * @return True if this activity is the "Launcher Activity" of Vault.
     */
    protected boolean isEntryActivity() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T getView(View root, int id) {
        return (T) root.findViewById(id);
    }

    public void lock() {
        addPinView(false);
        mListener.onShown();
    }

    public void unLock() {
        removePinView(true);
        mListener.onDismiss();
    }

    public interface Listener {
        void onShown();

        void onDismiss();
    }

    public static class LockInfo {

        Drawable icon;
        String title;

        public LockInfo(String title, Drawable icon) {
            this.icon = icon;
            this.title = title;
        }
    }
}
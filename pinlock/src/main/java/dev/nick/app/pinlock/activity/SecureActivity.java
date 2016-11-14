package dev.nick.app.pinlock.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.nick.app.pinlock.R;
import dev.nick.app.pinlock.application.VaultApp;
import dev.nick.app.pinlock.secure.PinKey;
import dev.nick.app.pinlock.secure.SecurityLockManager;
import dev.nick.app.pinlock.secure.VividDotListener;
import dev.nick.app.pinlock.utils.Logger;
import dev.nick.app.pinlock.utils.PreferenceHelper;
import dev.nick.app.pinlock.utils.ViewAnimatorUtil;
import dev.nick.app.pinlock.widget.DigitButton;
import dev.nick.app.pinlock.widget.PinPadLayout;
import dev.nick.app.pinlock.widget.VividDot;


public abstract class SecureActivity extends Activity implements VividDotListener {

    /**
     * Intent key to indicate if we should show the pin pad when activity create.
     */
    public static final String KEY_SHOW_PIN_PAD_WHEN_CREATE = "zhntd.key.pin.show";
    /**
     * The value of the lock time out when user has back to home.
     */
    public static final long PIN_LOCK_TIME_OUT = 5000;
    protected PreferenceHelper mPrefHelper;
    protected RelativeLayout mPinLayout;
    private VaultApp mApp;
    private VividDot mDotArea;
    private TextView mTipView;
    private String mLastInputPwd;

    private Handler mSecureHandler;
    private boolean mActivityVisible;
    private boolean mPendingShowPinPad;
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
    private Runnable mPinPadEnabler = new Runnable() {
        @Override
        public void run() {
            if (!mActivityVisible) {
                mPendingShowPinPad = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityVisible = true;
        onCreating();
        initWhenCreate();
        checkStateWhenCreate(getIntent());
    }

    protected void onCreating() {
        setContentView(getContentViewId());
    }

    public int getContentViewId() {
        return 0;
    }

    @Override
    protected void onResume() {
        Logger.i("Activity: " + toString() + "-onResume", getClass());
        super.onResume();
        mActivityVisible = true;
        mApp.checkState(this);
    }

    @Override
    protected void onPause() {
        Logger.i("Activity: " + toString() + "-onPause", getClass());
        super.onPause();
        mActivityVisible = false;
        if (mApp.getLockManager().isOnTop(this)) {
            Logger.d("Activity: " + this.toString() + "is on top, will trace the time out.", getClass());
            if (!mPrefHelper.isVaultFirstRun())
                mSecureHandler.postDelayed(mPinPadEnabler, PIN_LOCK_TIME_OUT);
        } else {
            Logger.d("Activity: " + this.toString() + "is Not on top, won't start the trace", getClass());
        }
    }

    @Override
    protected void onStop() {
        Logger.i("Activity: " + toString() + "-onStop", getClass());
        super.onStop();
        mActivityVisible = false;
    }

    @Override
    protected void onDestroy() {
        Logger.i("Activity: " + toString() + "-onDestroy", getClass());
        super.onDestroy();
        // pop out from the lock stack.
        mApp.getLockManager().popActivity(this);
        mActivityVisible = false;
        mSecureHandler.removeCallbacksAndMessages(null);
    }

    protected void initWhenCreate() {
        mApp = (VaultApp) getApplication();
        // steal into the lock stack.
        mApp.getLockManager().registerActivity(this);
        mPrefHelper = new PreferenceHelper(this);
        mSecureHandler = new Handler();
        setTitle(getTitle());
    }

    protected void preparePinPad() {
        if (mPinLayout == null) {
            mPinLayout = (RelativeLayout) LayoutInflater.from(this)
                    .inflate(R.layout.layout_pin_pad, (ViewGroup) getWindow().getDecorView(), false);
            mDotArea = getView(mPinLayout, R.id.dot_area);
            mTipView = getView(mPinLayout, R.id.tip_view);
            mDotArea.setListener(this);
            // set listener for all buttons ugly.
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
        if (mPrefHelper.isVaultFirstRun()) {
            setTipText(R.string.tips_pin_choose, 0);
        } else {
            setTipText(R.string.tips_enter_pin, 0);
        }
    }

    /**
     * Add pin pad to decor.
     *
     * @param animate True if you wanna show an animation during the add.
     * @param mode    The mode contains: Record(first run this app or change pwd),
     *                Check(Login this app).
     */
    protected void addPinPad(boolean animate, PinPadLayout.PinPadMode mode) {
        preparePinPad();
        if (mPinLayout.getWindowToken() != null) {
            return;
        }
        ViewGroup.LayoutParams params
                = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(mPinLayout, params);
        if (animate) {
            ViewAnimatorUtil.circularSHow(mPinLayout);
        } else {
            mPinLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remove pin pad from decor.
     *
     * @param animate True if you wanna show an animation during the remove.
     */
    protected synchronized void removePinPad(boolean animate) {
        disableOkayBtn();
        final ViewGroup group = (ViewGroup) getWindow().getDecorView();
        if (animate) {
            ViewAnimatorUtil.circularHide(mPinLayout, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    enableOkayBtn();
                    group.removeView(mPinLayout);
                    onPinCorrect();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            group.removeView(mPinLayout);
        }

    }

    protected void checkStateWhenCreate(Intent state) {
        boolean isFirstRunApp = mPrefHelper.isVaultFirstRun();
        if (isFirstRunApp) {
            addPinPad(false, PinPadLayout.PinPadMode.Record);
            return;
        }
        if (isEntryActivity()) {
            addPinPad(true, PinPadLayout.PinPadMode.Check);
            return;
        }
        boolean showWhenCreate = false;
        if (state != null)
            showWhenCreate = state.getBooleanExtra(KEY_SHOW_PIN_PAD_WHEN_CREATE, false);
        if (showWhenCreate) {
            addPinPad(true, PinPadLayout.PinPadMode.Check);
        }
    }

    /**
     * Perform a check when activity resume to check if the lock has timeout
     * and if we should show the pin pad.
     */
    public void checkStateWhenResume(SecurityLockManager manager) {
        if (manager == null) throw new IllegalArgumentException("Invalid args!");
        if (!mPrefHelper.isVaultFirstRun()) {
            if (mPendingShowPinPad)
                addPinPad(true, PinPadLayout.PinPadMode.Check);
            else {
                mSecureHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    protected void onBackspaceClicked() {
        mDotArea.removeLastDigit();
    }

    protected void onOkayClicked() {
        String input = mDotArea.convertDots();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        Logger.i("onOkayClicked, input = " + input, getClass());
        if (mPrefHelper.isVaultFirstRun()) {
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
                    removePinPad(true);
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

    protected void onDigitBtnClicked(DigitButton button) {
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

    protected void onPinCodeSaved(String pin) {
        Logger.i("onPinCodeSaved: " + pin, getClass());
        mPrefHelper.markHasRun();
    }

    /**
     * Called when the pin user input matches the right.
     */
    private void onPinCorrectInternal() {
        Logger.i("onPinCorrectInternal: ", getClass());
        removePinPad(true);
    }

    protected void onPinCorrect() {
        // None
    }

    /**
     * Called when the pin user input is not match with the previous when record.
     *
     * @param pin The pin that user give.
     */
    protected void onPinNotMatch(String pin) {
        Logger.i("onPinNotMatch: " + pin, getClass());
        setTipText(R.string.tips_pin_no_match, R.string.tips_pin_choose);
        mLastInputPwd = null;
    }

    /**
     * Called when check pwd, and the pwd user input is not correct.
     *
     * @param pin The pin that user give.
     */
    protected void onPinCodeError(String pin) {
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
    protected void setTipText(int text, final int textToRestore) {
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

    protected void enableOkayBtn() {
        getView(mPinLayout, R.id.okay).setEnabled(true);
    }

    protected void disableOkayBtn() {
        getView(mPinLayout, R.id.okay).setEnabled(false);
    }

    /**
     * @return True if this activity is the "Launcher Activity" of Vault.
     */
    protected boolean isEntryActivity() {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(View root, int id) {
        return (T) root.findViewById(id);
    }

}
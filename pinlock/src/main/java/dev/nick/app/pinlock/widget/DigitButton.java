package dev.nick.app.pinlock.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import dev.nick.app.pinlock.R;
import dev.nick.app.pinlock.secure.PinKey;
import dev.nick.app.pinlock.secure.SecurityUtils;
import dev.nick.app.pinlock.utils.Logger;

public class DigitButton extends Button {

    private PinKey mStandFor = PinKey.ZERO;

    private DigitButton(Context context) {
        super(context);
    }

    public DigitButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DigitButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DigitButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.pin);
        String keyStr = array.getString(R.styleable.pin_key);
        if (keyStr != null)
            mStandFor = SecurityUtils.text2PinKey(keyStr);
        array.recycle();

        setSoundEffectsEnabled(true);

        Logger.d("init: key = " + mStandFor.toString(), getClass());
    }

    public PinKey getPinKey() {
        return mStandFor;
    }
}

package dev.nick.app.pinlock.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import dev.nick.app.pinlock.R;
import dev.nick.app.pinlock.utils.ViewAnimatorUtil;

public class VaultEntryActivity extends SecureActivity {

    @Override
    public int getContentViewId() {
        return R.layout.activity_empty;
    }

    @Override
    protected boolean isEntryActivity() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getView(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(VaultEntryActivity.this, EmptyActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPinCodeError(String pin) {
        super.onPinCodeError(pin);
    }

    @Override
    protected void onPinCorrect() {
        super.onPinCorrect();
        initViews();
    }

    @Override
    protected void onPinCodeSaved(String pin) {
        super.onPinCodeSaved(pin);
        initViews();
    }

    private void initViews() {
        Toolbar toolbar = getView(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.purple));
        getWindow().setStatusBarColor(getResources().getColor(R.color.purple));
        ViewAnimatorUtil.circularSHow(toolbar);
    }
}

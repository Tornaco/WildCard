package dev.nick.app.pinlock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dev.nick.app.pinlock.utils.PreferenceHelper;

public class PwdReSetter extends AppCompatActivity {

    PreferenceHelper mHelper;

    EditText questionText;
    EditText answerText;
    Button completeBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new PreferenceHelper(this);
        setContentView(R.layout.activity_pwd_resetter);

        questionText = (EditText) findViewById(R.id.input_q);
        answerText = (EditText) findViewById(R.id.input_a);
        completeBtn = (Button) findViewById(R.id.btn_complete);

        questionText.setText(mHelper.getSecQuestion());

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResetPwd();
            }
        });
    }

    protected void onResetPwd() {
        if (validate()) {

            String q = questionText.getText().toString();
            String a = answerText.getText().toString();

            if (!mHelper.getSecAnswer().equals(a)) {
                Snackbar.make(findViewById(R.id.parent), R.string.incorrect_a, Snackbar.LENGTH_SHORT).show();
                return;
            }

            mHelper.setQuestion(q);
            mHelper.setAnswer(a);

            mHelper.clearPwd();

            PinLockStub.LockSettings info = new PinLockStub.LockSettings(
                    ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
                    , null, ContextCompat.getColor(getApplicationContext(), R.color.primary),
                    false, false);

            info.setShowHelp(false);

            PinLockStub stub = new PinLockStub(this, info, new PinLockStub.Listener() {
                @Override
                public void onShown(PinLockStub.LockSettings settings) {
                    // None
                }

                @Override
                public void onDismiss(PinLockStub.LockSettings lockInfo) {
                    onPwdSet();
                }
            });
            stub.lock();
        }
    }

    public void onPwdSet() {
        finish();
    }

    public boolean validate() {
        boolean valid = true;

        String email = questionText.getText().toString();
        String password = answerText.getText().toString();

        if (email.isEmpty()) {
            questionText.setError(getString(R.string.q_err_invalid));
            valid = false;
        } else {
            questionText.setError(null);
        }

        if (password.isEmpty()) {
            answerText.setError(getString(R.string.a_err_invalid));
            valid = false;
        } else {
            answerText.setError(null);
        }

        return valid;
    }
}

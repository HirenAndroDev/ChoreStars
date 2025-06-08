package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chores.app.kids.chores_app_for_kids.R;

public class InviteCodeDialog extends Dialog {
    private EditText etInviteCode;
    private Button btnJoin;
    private Button btnCancel;
    private TextView tvTitle;

    private OnInviteCodeEnteredListener listener;

    public interface OnInviteCodeEnteredListener {
        void onInviteCodeEntered(String inviteCode);
    }

    public InviteCodeDialog(@NonNull Context context, OnInviteCodeEnteredListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_invite_code);

        initViews();
        setupClickListeners();
        setupTextWatcher();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        etInviteCode = findViewById(R.id.et_invite_code);
        btnJoin = findViewById(R.id.btn_join);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupClickListeners() {
        btnJoin.setOnClickListener(v -> {
            String inviteCode = etInviteCode.getText().toString().trim();
            if (validateInviteCode(inviteCode)) {
                if (listener != null) {
                    listener.onInviteCodeEntered(inviteCode);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupTextWatcher() {
        etInviteCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnJoin.setEnabled(s.length() == 6);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInviteCode(String inviteCode) {
        if (inviteCode.isEmpty()) {
            etInviteCode.setError("Invite code is required");
            return false;
        }

        if (inviteCode.length() != 6) {
            etInviteCode.setError("Invite code must be 6 digits");
            return false;
        }

        return true;
    }
}
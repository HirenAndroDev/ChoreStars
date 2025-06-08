package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chores.app.kids.chores_app_for_kids.R;

public class ConfirmationDialog extends Dialog {
    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;
    private OnConfirmationListener listener;

    private TextView tvTitle;
    private TextView tvMessage;
    private Button btnPositive;
    private Button btnNegative;

    public interface OnConfirmationListener {
        void onConfirm();
        void onCancel();
    }

    public ConfirmationDialog(@NonNull Context context, String title, String message,
                              String positiveButtonText, String negativeButtonText,
                              OnConfirmationListener listener) {
        super(context);
        this.title = title;
        this.message = message;
        this.positiveButtonText = positiveButtonText;
        this.negativeButtonText = negativeButtonText;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirmation);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvMessage = findViewById(R.id.tv_message);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
    }

    private void setupData() {
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveButtonText);
        btnNegative.setText(negativeButtonText);
    }

    private void setupClickListeners() {
        btnPositive.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm();
            }
            dismiss();
        });

        btnNegative.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });
    }
}
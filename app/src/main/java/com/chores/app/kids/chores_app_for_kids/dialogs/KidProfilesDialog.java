package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.MainActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.KidProfileDialogAdapter;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.KidProfile;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.KidProfileManager;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;

import java.util.ArrayList;
import java.util.List;

public class KidProfilesDialog extends Dialog {

    private static final String TAG = "KidProfilesDialog";

    private Context context;
    private List<ChildProfile> kidProfiles;
    private String selectedKidId;
    private OnKidSelectedListener onKidSelectedListener;

    private RecyclerView recyclerKidProfiles;
    private KidProfileDialogAdapter adapter;
    private LinearLayout layoutAddKidProfile;
    private TextView tvLogOut;
    private ImageView ivCloseDialog;

    // Add Kid Profile Dialog variables
    private Dialog addKidDialog;
    private EditText[] digitInputs;
    private Button btnJoin;
    private ImageView ivBackArrow;

    public interface OnKidSelectedListener {
        void onKidSelected(ChildProfile selectedKid);
    }

    public KidProfilesDialog(@NonNull Context context, List<ChildProfile> kidProfiles, String selectedKidId) {
        super(context);
        this.context = context;
        this.kidProfiles = kidProfiles;
        this.selectedKidId = selectedKidId;
        Log.d(TAG, "Dialog created with " + kidProfiles.size() + " profiles");
    }

    public void setOnKidSelectedListener(OnKidSelectedListener listener) {
        this.onKidSelectedListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_kid_profiles);

        // Set dialog size
        if (getWindow() != null) {
            android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerKidProfiles = findViewById(R.id.recycler_kid_profiles);
        layoutAddKidProfile = findViewById(R.id.layout_add_kid_profile);
        tvLogOut = findViewById(R.id.tv_log_out);
        ivCloseDialog = findViewById(R.id.iv_close_dialog);
    }

    private void setupRecyclerView() {
        adapter = new KidProfileDialogAdapter(kidProfiles, context, selectedKidId);
        adapter.setOnKidProfileClickListener(kidProfile -> {
            Log.d(TAG, "Kid profile selected: " + kidProfile.getName());
            if (onKidSelectedListener != null) {
                onKidSelectedListener.onKidSelected(kidProfile);
            }
            dismiss();
        });

        recyclerKidProfiles.setLayoutManager(new LinearLayoutManager(context));
        recyclerKidProfiles.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ivCloseDialog.setOnClickListener(v -> dismiss());

        layoutAddKidProfile.setOnClickListener(v -> {
            SoundHelper.playClickSound(context);
            showAddKidProfileDialog();
        });

        tvLogOut.setOnClickListener(v -> {
            SoundHelper.playClickSound(context);
            showLogoutConfirmation();
        });
    }

    private void showAddKidProfileDialog() {
        addKidDialog = new Dialog(context);
        addKidDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addKidDialog.setContentView(R.layout.dialog_add_kid_profile);

        // Set dialog size for better visibility
        if (addKidDialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = addKidDialog.getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            addKidDialog.getWindow().setAttributes(params);
            addKidDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initializeAddKidDialogViews();
        setupAddKidDialogListeners();

        addKidDialog.show();
    }

    private void initializeAddKidDialogViews() {
        digitInputs = new EditText[6];
        digitInputs[0] = addKidDialog.findViewById(R.id.et_digit_1);
        digitInputs[1] = addKidDialog.findViewById(R.id.et_digit_2);
        digitInputs[2] = addKidDialog.findViewById(R.id.et_digit_3);
        digitInputs[3] = addKidDialog.findViewById(R.id.et_digit_4);
        digitInputs[4] = addKidDialog.findViewById(R.id.et_digit_5);
        digitInputs[5] = addKidDialog.findViewById(R.id.et_digit_6);

        btnJoin = addKidDialog.findViewById(R.id.btn_join);
        ivBackArrow = addKidDialog.findViewById(R.id.iv_back_arrow);

        // Focus on first input and show keyboard
        digitInputs[0].requestFocus();
        if (addKidDialog.getWindow() != null) {
            addKidDialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private void setupAddKidDialogListeners() {
        // Setup digit input navigation
        for (int i = 0; i < digitInputs.length; i++) {
            final int currentIndex = i;
            digitInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < digitInputs.length - 1) {
                        digitInputs[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            digitInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && digitInputs[currentIndex].getText().toString().isEmpty() && currentIndex > 0) {
                    digitInputs[currentIndex - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }

        ivBackArrow.setOnClickListener(v -> {
            if (addKidDialog != null) {
                addKidDialog.dismiss();
            }
        });

        btnJoin.setOnClickListener(v -> handleJoinKid());
    }

    private void handleJoinKid() {
        String inviteCode = getInviteCodeFromInputs();

        if (inviteCode.length() != 6) {
            Toast.makeText(context, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            SoundHelper.playErrorSound(context);
            return;
        }

        // Show loading state
        btnJoin.setEnabled(false);
        btnJoin.setText("Joining...");

        // Join family with child code
        AuthHelper.joinFamilyWithChildCode(inviteCode, context, task -> {
            btnJoin.setEnabled(true);
            btnJoin.setText("JOIN");

            if (task.isSuccessful()) {
                // Success - play sound and close dialogs
                SoundHelper.playSuccessSound(context);
                Toast.makeText(context, "Successfully added kid profile!", Toast.LENGTH_SHORT).show();

                if (addKidDialog != null) {
                    addKidDialog.dismiss();
                }
                dismiss();

                // Refresh the fragment/activity
                if (onKidSelectedListener != null) {
                    // Create a temp child profile for the new kid
                    ChildProfile newKid = new ChildProfile();
                    newKid.setChildId(AuthHelper.getChildId(context));
                    newKid.setName(AuthHelper.getUserName(context));
                    newKid.setFamilyId(AuthHelper.getFamilyId(context));
                    onKidSelectedListener.onKidSelected(newKid);
                }

                // Refresh the current dialog with updated kid profiles
                refreshKidProfiles();
            } else {
                // Show error message
                String errorMessage = getErrorMessage(task.getException());
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                SoundHelper.playErrorSound(context);
                shakeDigitInputs();
            }
        });
    }

    private String getInviteCodeFromInputs() {
        StringBuilder code = new StringBuilder();
        for (EditText digitInput : digitInputs) {
            code.append(digitInput.getText().toString());
        }
        return code.toString();
    }

    private void shakeDigitInputs() {
        for (EditText digitInput : digitInputs) {
            android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(
                    digitInput, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
            shake.setDuration(600);
            shake.start();
        }
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Invalid invite code. Please check with your parent.";
        }

        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("expired")) {
                return "This invite code has expired. Ask your parent for a new one!";
            } else if (message.contains("not found")) {
                return "Invite code not found. Double-check the numbers!";
            }
        }

        return "Something went wrong. Please try again!";
    }

    private void showLogoutConfirmation() {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? You'll need to enter your invite code again next time.")
                .setPositiveButton("Yes, Log Out", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "Performing logout");

        // Clear all kid profile data
        AuthHelper.clearAllKidProfiles(context);

        // Play sound
        SoundHelper.playClickSound(context);

        // Close dialog
        dismiss();

        // Navigate to MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Close current activity if it's an activity context
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }

    private void refreshKidProfiles() {
        // Get updated kid profiles from AuthHelper
        List<KidProfile> savedKidProfiles = AuthHelper.getSavedKidProfiles(context);
        List<ChildProfile> updatedChildProfiles = new ArrayList<>();

        // Convert KidProfile to ChildProfile
        for (KidProfile kidProfile : savedKidProfiles) {
            ChildProfile childProfile = new ChildProfile();
            childProfile.setChildId(kidProfile.getKidId());
            childProfile.setName(kidProfile.getName());
            childProfile.setFamilyId(kidProfile.getFamilyId());
            childProfile.setProfileImageUrl(kidProfile.getProfileImageUrl());
            childProfile.setStarBalance(kidProfile.getStarBalance());
            updatedChildProfiles.add(childProfile);
        }

        // Update the dialog's profiles and adapter
        kidProfiles = updatedChildProfiles;
        adapter = new KidProfileDialogAdapter(kidProfiles, context, selectedKidId);
        adapter.setOnKidProfileClickListener(kidProfile -> {
            Log.d(TAG, "Kid profile selected: " + kidProfile.getName());
            if (onKidSelectedListener != null) {
                onKidSelectedListener.onKidSelected(kidProfile);
            }
            dismiss();
        });
        recyclerKidProfiles.setAdapter(adapter);
    }
}

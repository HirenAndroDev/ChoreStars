package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.AddKidProfileActivity;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class KidProfilesParentDialog extends Dialog {

    private Context context;
    private List<ChildProfile> kidProfiles;
    private ChildProfile selectedKid;
    private OnKidSelectedListener listener;
    private LinearLayout layoutKidProfilesContainer;

    public interface OnKidSelectedListener {
        void onKidSelected(ChildProfile kidProfile);
    }

    public KidProfilesParentDialog(@NonNull Context context, List<ChildProfile> kidProfiles, ChildProfile selectedKid) {
        super(context);
        this.context = context;
        this.kidProfiles = kidProfiles;
        this.selectedKid = selectedKid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_kid_profiles_parent);

        setupViews();
        populateKidProfiles();
    }

    private void setupViews() {
        ImageView ivClose = findViewById(R.id.iv_close_dialog);
        LinearLayout layoutAddKid = findViewById(R.id.layout_add_kid_profile);
        layoutKidProfilesContainer = findViewById(R.id.layout_kid_profiles_container);

        ivClose.setOnClickListener(v -> dismiss());
        layoutAddKid.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddKidProfileActivity.class);
            context.startActivity(intent);
            dismiss();
        });
    }

    private void populateKidProfiles() {
        layoutKidProfilesContainer.removeAllViews();

        for (ChildProfile kid : kidProfiles) {
            View kidView = LayoutInflater.from(context).inflate(R.layout.item_kid_profile_parent, null);

            CircleImageView ivKidProfile = kidView.findViewById(R.id.iv_kid_profile);
            TextView tvKidName = kidView.findViewById(R.id.tv_kid_name);
            TextView tvTasksDue = kidView.findViewById(R.id.tv_tasks_due);
            TextView tvKidStarBalance = kidView.findViewById(R.id.tv_kid_star_balance);

            // Set kid data
            tvKidName.setText(kid.getName());
            tvKidStarBalance.setText(String.valueOf(kid.getStarBalance()));

            // Set tasks due (you can implement actual logic here)
            int tasksDue = getTasksDueForKid(kid);
            if (tasksDue == 0) {
                tvTasksDue.setText("No tasks due today");
            } else if (tasksDue == 1) {
                tvTasksDue.setText("1 Task Due Today");
            } else {
                tvTasksDue.setText(tasksDue + " Tasks Due Today");
            }

            // Load profile image
            if (kid.getProfileImageUrl() != null && !kid.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(kid.getProfileImageUrl())
                        .circleCrop()
                        .into(ivKidProfile);
            } else {
                ivKidProfile.setImageResource(R.drawable.default_avatar);
            }

            // Set selected state
            boolean isSelected = selectedKid != null && selectedKid.getChildId().equals(kid.getChildId());
            if (isSelected) {
                kidView.setBackgroundResource(R.drawable.bg_kid_profile_item_parent_selected);
            } else {
                kidView.setBackgroundResource(R.drawable.bg_kid_profile_item_parent);
            }

            // Set click listener
            kidView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKidSelected(kid);
                }
                dismiss();
            });

            // Add to container
            layoutKidProfilesContainer.addView(kidView);
        }
    }

    private int getTasksDueForKid(ChildProfile kid) {
        // Placeholder - implement actual logic to count tasks due today for this kid
        // You can use FirebaseHelper to get actual task count
        return 1; // Temporary placeholder
    }

    public void setOnKidSelectedListener(OnKidSelectedListener listener) {
        this.listener = listener;
    }
}
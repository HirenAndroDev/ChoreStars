package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class KidProfileDialogAdapter extends RecyclerView.Adapter<KidProfileDialogAdapter.KidProfileViewHolder> {

    private static final String TAG = "KidProfileDialogAdapter";
    private List<ChildProfile> kidProfiles;
    private Context context;
    private OnKidProfileClickListener listener;
    private String selectedKidId;

    public interface OnKidProfileClickListener {
        void onKidProfileClick(ChildProfile kidProfile);
    }

    public KidProfileDialogAdapter(List<ChildProfile> kidProfiles, Context context, String selectedKidId) {
        this.kidProfiles = kidProfiles;
        this.context = context;
        this.selectedKidId = selectedKidId;
        Log.d(TAG, "Adapter created with " + kidProfiles.size() + " profiles, selected: " + selectedKidId);
    }

    public void setOnKidProfileClickListener(OnKidProfileClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedKidId(String selectedKidId) {
        this.selectedKidId = selectedKidId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KidProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kid_profile_dialog, parent, false);
        return new KidProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidProfileViewHolder holder, int position) {
        ChildProfile kidProfile = kidProfiles.get(position);
        Log.d(TAG, "Binding profile: " + kidProfile.getName() + " (ID: " + kidProfile.getChildId() + ")");

        holder.tvKidName.setText(kidProfile.getName());

        // Load profile image
        if (kidProfile.getProfileImageUrl() != null && !kidProfile.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(kidProfile.getProfileImageUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(holder.ivKidAvatar);
        } else {
            holder.ivKidAvatar.setImageResource(R.drawable.default_avatar);
        }

        // Show selection indicator
        boolean isSelected = kidProfile.getChildId().equals(selectedKidId);
        holder.ivSelectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

        Log.d(TAG, "Profile " + kidProfile.getName() + " selected: " + isSelected);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "Profile clicked: " + kidProfile.getName());
                listener.onKidProfileClick(kidProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return kidProfiles.size();
    }

    static class KidProfileViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivKidAvatar;
        TextView tvKidName;
        ImageView ivSelectedIndicator;

        public KidProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivKidAvatar = itemView.findViewById(R.id.iv_kid_avatar);
            tvKidName = itemView.findViewById(R.id.tv_kid_name);
            ivSelectedIndicator = itemView.findViewById(R.id.iv_selected_indicator);
        }
    }
}

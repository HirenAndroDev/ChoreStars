package com.chores.app.kids.chores_app_for_kids.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;
import java.util.ArrayList;
import java.util.List;

public class KidSelectionAdapter extends RecyclerView.Adapter<KidSelectionAdapter.KidSelectionViewHolder> {

    private List<User> kidList;
    private List<String> selectedKidIds;
    private Context context;
    private OnKidSelectionChangedListener listener;

    public interface OnKidSelectionChangedListener {
        void onKidSelectionChanged(List<String> selectedKidIds);
    }

    public KidSelectionAdapter(List<User> kidList, Context context, OnKidSelectionChangedListener listener) {
        this.kidList = kidList != null ? kidList : new ArrayList<>();
        this.context = context;
        this.listener = listener;
        this.selectedKidIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public KidSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kid_selection, parent, false);
        return new KidSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidSelectionViewHolder holder, int position) {
        User kid = kidList.get(position);

        // Set kid name
        holder.tvKidName.setText(kid.getName());

        // Set star balance info
        if (kid.getStarBalance() > 0) {
            holder.tvKidStarBalance.setText(String.format("%d stars earned", kid.getStarBalance()));
            holder.tvKidStarBalance.setVisibility(View.VISIBLE);
        } else {
            holder.tvKidStarBalance.setVisibility(View.GONE);
        }

        // Set avatar - you can customize this based on kid's profile image or use default
        if (kid.getProfileImageUrl() != null && !kid.getProfileImageUrl().isEmpty()) {
            // Load profile image using Glide or similar
            // Glide.with(context).load(kid.getProfileImageUrl()).into(holder.ivKidAvatar);
            holder.ivKidAvatar.setImageResource(R.drawable.ic_child);
        } else {
            // Use default avatar with different colors for variety
            holder.ivKidAvatar.setImageResource(R.drawable.ic_child);
            int[] avatarColors = {
                    R.color.success_green,
                    R.color.accent_blue,
                    R.color.accent_purple,
                    R.color.accent_orange,
                    R.color.primary_green
            };
            int colorIndex = position % avatarColors.length;
            holder.ivKidAvatar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(context.getResources().getColor(avatarColors[colorIndex]))
            );
        }

        // Set checkbox state
        boolean isSelected = selectedKidIds.contains(kid.getUserId());
        holder.cbKidSelected.setChecked(isSelected);

        // Handle checkbox changes
        holder.cbKidSelected.setOnCheckedChangeListener(null); // Clear previous listener
        holder.cbKidSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedKidIds.contains(kid.getUserId())) {
                    selectedKidIds.add(kid.getUserId());
                }
            } else {
                selectedKidIds.remove(kid.getUserId());
            }

            // Notify listener of selection change
            if (listener != null) {
                listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
            }

            // Update visual feedback
            updateSelectionVisualFeedback(holder, isChecked);
        });

        // Handle item click to toggle selection
        holder.itemView.setOnClickListener(v -> {
            holder.cbKidSelected.setChecked(!holder.cbKidSelected.isChecked());
        });

        // Set initial visual feedback
        updateSelectionVisualFeedback(holder, isSelected);
    }

    private void updateSelectionVisualFeedback(KidSelectionViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
        } else {
            holder.itemView.setAlpha(0.7f);
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return kidList.size();
    }

    // Public methods for external control

    public void setSelectedKids(List<String> selectedIds) {
        this.selectedKidIds.clear();
        if (selectedIds != null) {
            this.selectedKidIds.addAll(selectedIds);
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedKidIds() {
        return new ArrayList<>(selectedKidIds);
    }

    public void selectAll() {
        selectedKidIds.clear();
        for (User kid : kidList) {
            selectedKidIds.add(kid.getUserId());
        }
        notifyDataSetChanged();

        if (listener != null) {
            listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
        }
    }

    public void clearSelection() {
        selectedKidIds.clear();
        notifyDataSetChanged();

        if (listener != null) {
            listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
        }
    }

    public boolean hasSelection() {
        return !selectedKidIds.isEmpty();
    }

    public int getSelectionCount() {
        return selectedKidIds.size();
    }

    public void updateKidList(List<User> newKidList) {
        this.kidList.clear();
        if (newKidList != null) {
            this.kidList.addAll(newKidList);
        }

        // Remove any selected IDs that are no longer in the list
        List<String> validIds = new ArrayList<>();
        for (User kid : this.kidList) {
            if (selectedKidIds.contains(kid.getUserId())) {
                validIds.add(kid.getUserId());
            }
        }
        this.selectedKidIds = validIds;

        notifyDataSetChanged();

        if (listener != null) {
            listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
        }
    }

    static class KidSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbKidSelected;
        ImageView ivKidAvatar;
        TextView tvKidName;
        TextView tvKidStarBalance;

        public KidSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cbKidSelected = itemView.findViewById(R.id.cb_kid_selected);
            ivKidAvatar = itemView.findViewById(R.id.iv_kid_avatar);
            tvKidName = itemView.findViewById(R.id.tv_kid_name);
            tvKidStarBalance = itemView.findViewById(R.id.tv_kid_star_balance);
        }
    }
}

package com.chores.app.kids.chores_app_for_kids.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import com.chores.app.kids.chores_app_for_kids.utils.SoundHelper;
import java.util.ArrayList;
import java.util.List;

public class PrebuiltRewardAdapter extends RecyclerView.Adapter<PrebuiltRewardAdapter.PrebuiltRewardViewHolder> {

    private List<Reward> rewardList;
    private Context context;
    private OnRewardSelectedListener listener;
    private int selectedPosition = -1;
    private Reward selectedReward = null;

    public interface OnRewardSelectedListener {
        void onRewardSelected(Reward reward);
        void onRewardDeselected();
    }

    public PrebuiltRewardAdapter(List<Reward> rewardList, Context context, OnRewardSelectedListener listener) {
        this.rewardList = rewardList != null ? rewardList : new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrebuiltRewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prebuilt_reward, parent, false);
        return new PrebuiltRewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrebuiltRewardViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Reward reward = rewardList.get(position);

        // Set reward details
        holder.tvPrebuiltName.setText(reward.getName());
        holder.tvPrebuiltCost.setText(String.format("%d ⭐", reward.getStarCost()));

        // Set reward icon
        setRewardIcon(holder, reward);

        // Handle selection state
        boolean isSelected = position == selectedPosition;
        updateSelectionState(holder, isSelected);

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            SoundHelper.playClickSound(context);

            int previousPosition = selectedPosition;

            if (selectedPosition == position) {
                // Deselect current selection
                selectedPosition = -1;
                selectedReward = null;
                if (listener != null) {
                    listener.onRewardDeselected();
                }
            } else {
                // Select new reward
                selectedPosition = position;
                selectedReward = reward;
                if (listener != null) {
                    listener.onRewardSelected(reward);
                }
            }

            // Update UI
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition != -1 ? selectedPosition : position);
        });

        // Add category-based visual styling
        applyCategoryStyle(holder, reward);
    }

    private void setRewardIcon(PrebuiltRewardViewHolder holder, Reward reward) {
        String iconName = reward.getIconName();
        if (iconName != null && !iconName.isEmpty()) {
            int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            if (iconResId != 0) {
                holder.ivPrebuiltIcon.setImageResource(iconResId);
            } else {
                holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_reward_default);
            }
        } else {
            // Set default icon based on reward name/category
            setDefaultIconByName(holder, reward.getName());
        }
    }

    private void setDefaultIconByName(PrebuiltRewardViewHolder holder, String rewardName) {
        String name = rewardName.toLowerCase();

        if (name.contains("ice cream") || name.contains("treat")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("screen") || name.contains("tv") || name.contains("video")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("dinner") || name.contains("food") || name.contains("meal")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("bedtime") || name.contains("sleep") || name.contains("stay up")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("movie") || name.contains("film")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("park") || name.contains("outside") || name.contains("playground")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("friend") || name.contains("playdate")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("toy") || name.contains("game")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else if (name.contains("allowance") || name.contains("money")) {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_ice_cream);
        } else {
            holder.ivPrebuiltIcon.setImageResource(R.drawable.ic_reward_default);
        }
    }

    private void updateSelectionState(PrebuiltRewardViewHolder holder, boolean isSelected) {
        if (isSelected) {
            // Selected state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_green));
            holder.tvPrebuiltName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.tvPrebuiltCost.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.ivPrebuiltIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
            holder.viewSelectionIndicator.setVisibility(View.VISIBLE);
            holder.cardView.setCardElevation(context.getResources().getDimension(R.dimen.card_elevation_high));
        } else {
            // Normal state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.tvPrebuiltName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.tvPrebuiltCost.setTextColor(ContextCompat.getColor(context, R.color.star_yellow));
            holder.ivPrebuiltIcon.clearColorFilter();
            holder.viewSelectionIndicator.setVisibility(View.INVISIBLE);
            holder.cardView.setCardElevation(context.getResources().getDimension(R.dimen.card_elevation));
        }
    }

    private void applyCategoryStyle(PrebuiltRewardViewHolder holder, Reward reward) {
        // Apply subtle background tint based on reward category/type
        String name = reward.getName().toLowerCase();
        int backgroundTint = android.R.color.white;

        if (name.contains("ice cream") || name.contains("treat") || name.contains("snack")) {
            // Food treats - light orange tint
            backgroundTint = R.color.treat_background;
        } else if (name.contains("screen") || name.contains("movie") || name.contains("tv")) {
            // Entertainment - light blue tint
            backgroundTint = R.color.entertainment_background;
        } else if (name.contains("friend") || name.contains("park") || name.contains("outside")) {
            // Activities - light green tint
            backgroundTint = R.color.activity_background;
        } else if (name.contains("toy") || name.contains("allowance")) {
            // Special rewards - light purple tint
            backgroundTint = R.color.special_background;
        }

        if (selectedPosition != holder.getAdapterPosition()) {
            // Only apply category tint when not selected
            try {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, backgroundTint));
            } catch (Exception e) {
                // Fallback to white if color doesn't exist
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    // Public methods for external control

    public Reward getSelectedReward() {
        return selectedReward;
    }

    public void clearSelection() {
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        selectedReward = null;

        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }

        if (listener != null) {
            listener.onRewardDeselected();
        }
    }

    public void setSelectedReward(Reward reward) {
        if (reward == null) {
            clearSelection();
            return;
        }

        for (int i = 0; i < rewardList.size(); i++) {
            if (rewardList.get(i).getName().equals(reward.getName())) {
                int previousPosition = selectedPosition;
                selectedPosition = i;
                selectedReward = reward;

                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onRewardSelected(reward);
                }
                break;
            }
        }
    }

    public void updateRewardList(List<Reward> newRewardList) {
        this.rewardList.clear();
        if (newRewardList != null) {
            this.rewardList.addAll(newRewardList);
        }

        // Clear selection if the selected reward is no longer available
        if (selectedReward != null) {
            boolean found = false;
            for (Reward reward : this.rewardList) {
                if (reward.getName().equals(selectedReward.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                clearSelection();
            }
        }

        notifyDataSetChanged();
    }

    public void updateStarCost(int newStarCost) {
        if (selectedReward != null) {
            selectedReward.setStarCost(newStarCost);
            if (selectedPosition != -1) {
                notifyItemChanged(selectedPosition);
            }
        }
    }

    public boolean hasSelection() {
        return selectedReward != null;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public List<Reward> getRewardList() {
        return new ArrayList<>(rewardList);
    }

    public void sortRewardsByCategory() {
        rewardList.sort((r1, r2) -> {
            String cat1 = getRewardCategory(r1.getName());
            String cat2 = getRewardCategory(r2.getName());

            if (cat1.equals(cat2)) {
                // Same category, sort by star cost
                return Integer.compare(r1.getStarCost(), r2.getStarCost());
            } else {
                // Different categories, sort alphabetically
                return cat1.compareTo(cat2);
            }
        });
        notifyDataSetChanged();
    }

    public void sortRewardsByCost() {
        rewardList.sort((r1, r2) -> Integer.compare(r1.getStarCost(), r2.getStarCost()));
        notifyDataSetChanged();
    }

    private String getRewardCategory(String rewardName) {
        String name = rewardName.toLowerCase();

        if (name.contains("ice cream") || name.contains("treat") || name.contains("snack")) {
            return "Treats";
        } else if (name.contains("screen") || name.contains("movie") || name.contains("tv")) {
            return "Entertainment";
        } else if (name.contains("friend") || name.contains("park") || name.contains("outside")) {
            return "Activities";
        } else if (name.contains("toy") || name.contains("allowance")) {
            return "Special";
        } else {
            return "Other";
        }
    }

    public List<Reward> getRewardsByCategory(String category) {
        List<Reward> categoryRewards = new ArrayList<>();
        for (Reward reward : rewardList) {
            if (getRewardCategory(reward.getName()).equals(category)) {
                categoryRewards.add(reward);
            }
        }
        return categoryRewards;
    }

    public String[] getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        for (Reward reward : rewardList) {
            String category = getRewardCategory(reward.getName());
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        return categories.toArray(new String[0]);
    }

    static class PrebuiltRewardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivPrebuiltIcon;
        TextView tvPrebuiltName;
        TextView tvPrebuiltCost;
        View viewSelectionIndicator;

        public PrebuiltRewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_prebuilt_reward);
            ivPrebuiltIcon = itemView.findViewById(R.id.iv_prebuilt_icon);
            tvPrebuiltName = itemView.findViewById(R.id.tv_prebuilt_name);
            tvPrebuiltCost = itemView.findViewById(R.id.tv_prebuilt_cost);
            viewSelectionIndicator = itemView.findViewById(R.id.view_selection_indicator);
        }
    }
}

package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;

import de.hdodenhof.circleimageview.CircleImageView;
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
        Log.d("KidSelectionAdapter", "onBindViewHolder called for position: " + position);

        User kid = kidList.get(position);
        Log.d("KidSelectionAdapter", "Binding kid: " + kid.getName() + " at position " + position);

        // Set kid name
        holder.tvKidName.setText(kid.getName());

        // Load profile image
        if (kid.getProfileImageUrl() != null && !kid.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(kid.getProfileImageUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(holder.ivKidAvatar);
        } else {
            holder.ivKidAvatar.setImageResource(R.drawable.default_avatar);
        }

        // Set selection state
        boolean isSelected = selectedKidIds.contains(kid.getUserId());
        holder.cbKidSelected.setChecked(isSelected);
        holder.ivSelectionCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Update border color based on selection
        if (isSelected) {
            holder.ivKidAvatar.setBorderColor(context.getResources().getColor(R.color.success_green));
            holder.ivKidAvatar.setBorderWidth(6);
        } else {
            holder.ivKidAvatar.setBorderColor(context.getResources().getColor(R.color.light_gray));
            holder.ivKidAvatar.setBorderWidth(3);
        }

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

            // Update visual feedback
            holder.ivSelectionCheck.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                holder.ivKidAvatar.setBorderColor(context.getResources().getColor(R.color.success_green));
                holder.ivKidAvatar.setBorderWidth(6);
            } else {
                holder.ivKidAvatar.setBorderColor(context.getResources().getColor(R.color.light_gray));
                holder.ivKidAvatar.setBorderWidth(3);
            }

            // Notify listener of selection change
            if (listener != null) {
                listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
            }
        });

        // Handle item click to toggle selection
        holder.itemView.setOnClickListener(v -> {
            holder.cbKidSelected.setChecked(!holder.cbKidSelected.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return kidList.size();
    }

    // Public methods for external control
    public void setSelectedKids(List<String> selectedIds) {
        Log.d("KidSelectionAdapter", "setSelectedKids called with " + (selectedIds != null ? selectedIds.size() : 0) + " kids");

        this.selectedKidIds.clear();
        if (selectedIds != null) {
            this.selectedKidIds.addAll(selectedIds);
            for (String kidId : selectedIds) {
                Log.d("KidSelectionAdapter", "Setting kid as selected: " + kidId);
            }
        }
        notifyDataSetChanged();

        // IMPORTANT: Trigger the callback to update the activity's selectedKids list
        if (listener != null) {
            Log.d("KidSelectionAdapter", "Triggering onKidSelectionChanged callback");
            listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
        } else {
            Log.w("KidSelectionAdapter", "Listener is null, cannot trigger callback!");
        }
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

    public void updateKidList(List<User> newKidList) {
        Log.d("KidSelectionAdapter", "updateKidList called with " + (newKidList != null ? newKidList.size() : 0) + " kids");

        this.kidList.clear();
        if (newKidList != null) {
            this.kidList.addAll(newKidList);
            for (User kid : newKidList) {
                Log.d("KidSelectionAdapter", "Kid added: " + kid.getName() + " (ID: " + kid.getUserId() + ")");
            }
        }

        // Remove any selected IDs that are no longer in the list
        List<String> validIds = new ArrayList<>();
        for (User kid : this.kidList) {
            if (selectedKidIds.contains(kid.getUserId())) {
                validIds.add(kid.getUserId());
            }
        }
        this.selectedKidIds = validIds;

        Log.d("KidSelectionAdapter", "Calling notifyDataSetChanged, kidList size: " + this.kidList.size());
        notifyDataSetChanged();

        if (listener != null) {
            listener.onKidSelectionChanged(new ArrayList<>(selectedKidIds));
        }
    }

    static class KidSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbKidSelected;
        CircleImageView ivKidAvatar;
        TextView tvKidName;
        ImageView ivSelectionCheck;

        public KidSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cbKidSelected = itemView.findViewById(R.id.cb_kid_selected);
            ivKidAvatar = itemView.findViewById(R.id.iv_kid_avatar);
            tvKidName = itemView.findViewById(R.id.tv_kid_name);
            ivSelectionCheck = itemView.findViewById(R.id.iv_selection_check);
        }
    }
}

package com.chores.app.kids.chores_app_for_kids.adapters;

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
import com.chores.app.kids.chores_app_for_kids.models.Kid;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class KidSelectionAdapter extends RecyclerView.Adapter<KidSelectionAdapter.KidViewHolder> {
    private List<Kid> kids;
    private List<String> selectedKidIds;
    private OnKidSelectionListener listener;

    public interface OnKidSelectionListener {
        void onKidSelectionChanged(String kidId, boolean isSelected);
    }

    public KidSelectionAdapter(List<Kid> kids, List<String> selectedKidIds, OnKidSelectionListener listener) {
        this.kids = kids;
        this.selectedKidIds = selectedKidIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kid_selection, parent, false);
        return new KidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidViewHolder holder, int position) {
        Kid kid = kids.get(position);
        holder.bind(kid);
    }

    @Override
    public int getItemCount() {
        return kids.size();
    }

    public void updateSelectedKids(List<String> newSelectedKidIds) {
        selectedKidIds.clear();
        selectedKidIds.addAll(newSelectedKidIds);
        notifyDataSetChanged();
    }

    class KidViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivKidAvatar;
        private TextView tvKidName;
        private TextView tvStarBalance;
        private CheckBox checkBoxSelected;

        public KidViewHolder(@NonNull View itemView) {
            super(itemView);
            ivKidAvatar = itemView.findViewById(R.id.iv_kid_avatar);
            tvKidName = itemView.findViewById(R.id.tv_kid_name);
            tvStarBalance = itemView.findViewById(R.id.tv_star_balance);
            checkBoxSelected = itemView.findViewById(R.id.checkbox_selected);

            itemView.setOnClickListener(v -> {
                checkBoxSelected.toggle();
            });

            checkBoxSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onKidSelectionChanged(kids.get(position).getKidId(), isChecked);
                    }
                }
            });
        }

        public void bind(Kid kid) {
            tvKidName.setText(kid.getName());
            tvStarBalance.setText(kid.getStarBalance() + " ⭐");

            boolean isSelected = selectedKidIds.contains(kid.getKidId());
            checkBoxSelected.setOnCheckedChangeListener(null);
            checkBoxSelected.setChecked(isSelected);
            checkBoxSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onKidSelectionChanged(kid.getKidId(), isChecked);
                }
            });

            if (kid.getProfileImage() != null && !kid.getProfileImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(kid.getProfileImage())
                        .placeholder(R.drawable.ic_default_kid_avatar)
                        .into(ivKidAvatar);
            } else {
                ivKidAvatar.setImageResource(R.drawable.ic_default_kid_avatar);
            }
        }
    }
}
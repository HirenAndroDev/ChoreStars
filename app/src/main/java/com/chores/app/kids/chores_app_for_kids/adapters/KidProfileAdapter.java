package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.Kid;

import java.util.List;

public class KidProfileAdapter extends RecyclerView.Adapter<KidProfileAdapter.KidViewHolder> {
    private List<Kid> kids;
    private OnKidClickListener listener;

    public interface OnKidClickListener {
        void onKidClick(Kid kid);
        void onKidEdit(Kid kid);
    }

    public KidProfileAdapter(List<Kid> kids, OnKidClickListener listener) {
        this.kids = kids;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kid_profile, parent, false);
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

    public void updateKids(List<Kid> newKids) {
        this.kids.clear();
        this.kids.addAll(newKids);
        notifyDataSetChanged();
    }

    class KidViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfileImage;
        private TextView tvKidName;
        private TextView tvStarBalance;
        private ImageView btnEdit;

        public KidViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvKidName = itemView.findViewById(R.id.tv_kid_name);
            tvStarBalance = itemView.findViewById(R.id.tv_star_balance);
            btnEdit = itemView.findViewById(R.id.btn_edit);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onKidClick(kids.get(position));
                    }
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onKidEdit(kids.get(position));
                    }
                }
            });
        }

        public void bind(Kid kid) {
            tvKidName.setText(kid.getName());
            tvStarBalance.setText(kid.getStarBalance() + " ⭐");

            if (kid.getProfileImage() != null && !kid.getProfileImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(kid.getProfileImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_kid_avatar)
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_default_kid_avatar);
            }
        }
    }
}

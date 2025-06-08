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
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;

import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.MemberViewHolder> {
    private List<User> members;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(User member);
    }

    public FamilyMemberAdapter(List<User> members, OnMemberClickListener listener) {
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<User> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        notifyDataSetChanged();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfileImage;
        private TextView tvMemberName;
        private TextView tvMemberEmail;
        private TextView tvMemberRole;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberEmail = itemView.findViewById(R.id.tv_member_email);
            tvMemberRole = itemView.findViewById(R.id.tv_member_role);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMemberClick(members.get(position));
                    }
                }
            });
        }

        public void bind(User member) {
            tvMemberName.setText(member.getName());
            tvMemberEmail.setText(member.getEmail());

            // Set role text
            if (Constants.ROLE_PARENT.equals(member.getRole())) {
                tvMemberRole.setText("Owner");
                tvMemberRole.setVisibility(View.VISIBLE);
            } else {
                tvMemberRole.setVisibility(View.GONE);
            }

            if (member.getProfileImage() != null && !member.getProfileImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(member.getProfileImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }
}

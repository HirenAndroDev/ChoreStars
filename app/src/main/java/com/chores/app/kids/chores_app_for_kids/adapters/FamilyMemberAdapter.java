package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    private List<User> memberList;
    private Context context;
    private OnMemberActionListener listener;
    private String currentUserId;

    public interface OnMemberActionListener {
        void onViewMemberDetails(User member);
        void onEditMember(User member);
        void onRemoveMember(User member);
        void onViewMemberStats(User member);
        void onManagePermissions(User member);

        void onGenerateInviteCode(User member);
    }

    public FamilyMemberAdapter(List<User> memberList, Context context) {
        this.memberList = memberList != null ? memberList : new ArrayList<>();
        this.context = context;
        this.currentUserId = AuthHelper.getCurrentUserId();
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_member, parent, false);
        return new FamilyMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberViewHolder holder, int position) {
        User member = memberList.get(position);

        Log.d("FamilyMemberAdapter", "onBindViewHolder: position=" + position + ", member=" + member.getName() + " (" + member.getRole() + ")");

        // Set member name
        holder.tvMemberName.setText(member.getName());

        // Set role with appropriate styling
        setupRoleDisplay(holder, member);

        // Set profile image
        setupProfileImage(holder, member, position);

        // Show star balance for children
        setupStarBalance(holder, member);

        // Set up action menu
        setupActionMenu(holder, member);

        // Highlight current user
        if (member.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_yellow));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        // Handle invite code display and generation for children only
        if ("child".equals(member.getRole())) {
            // For children, show invite code section
            holder.tvInviteCode.setVisibility(View.VISIBLE);
            holder.tvExpiresDate.setVisibility(View.VISIBLE);
            holder.btnGenerateCode.setVisibility(View.VISIBLE);

            // Load invite code data for this child
            loadInviteCodeForChild(holder, member);
        } else {
            // For parents, hide invite code section
            holder.tvInviteCode.setVisibility(View.GONE);
            holder.tvExpiresDate.setVisibility(View.GONE);
            holder.btnGenerateCode.setVisibility(View.GONE);
        }
    }

    private void loadInviteCodeForChild(FamilyMemberViewHolder holder, User member) {
        // This is a placeholder implementation. You would need to implement actual
        // Firebase loading logic here to get the invite code data for the child.
        // Example:
        // FirebaseHelper.getInviteCodeForChild(member.getUserId(), (inviteCode, expires) -> {
        //     holder.tvInviteCode.setText(inviteCode != null ? inviteCode : "No code generated");
        //     holder.tvExpiresDate.setText(expires != null ? "Expires: " + formatDate(expires) : "Never");
        // });

        // For now, just display the code from the model (if available)
        holder.tvInviteCode.setText(member.getInviteCode() != null ? member.getInviteCode() : "No code generated");
        holder.tvExpiresDate.setText(member.getInviteCodeExpires() != null ? "Expires: " + formatDate(member.getInviteCodeExpires()) : "Never");
        holder.btnGenerateCode.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenerateInviteCode(member);
            }
        });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(new java.util.Date(timestamp));
    }

    private void setupRoleDisplay(FamilyMemberViewHolder holder, User member) {
        String role = member.getRole();
        if ("parent".equals(role)) {
            holder.tvMemberRole.setText("Parent");
            holder.tvMemberRole.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_green)
            );
            holder.tvMemberRole.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.tvMemberRole.setText("Child");
            holder.tvMemberRole.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.success_green)
            );
            holder.tvMemberRole.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    private void setupProfileImage(FamilyMemberViewHolder holder, User member, int position) {
        // Clear any previous styling
        holder.ivProfileImage.setPadding(0, 0, 0, 0);
        holder.ivProfileImage.setBackgroundTintList(null);

        if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty()) {
            Log.d("FamilyMemberAdapter", "Loading profile image for " + member.getName() + ": " + member.getProfileImageUrl());

            Glide.with(context)
                    .load(member.getProfileImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.circle_background)
                            .error(R.drawable.circle_background)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(48, 48)
                            .circleCrop()) // This will make the image circular
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e("FamilyMemberAdapter", "Failed to load image for " + member.getName(), e);
                            // Set default avatar on failure
                            holder.ivProfileImage.post(() -> setDefaultAvatar(holder, member, position));
                            return true; // Return true to prevent Glide from setting error drawable
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("FamilyMemberAdapter", "Successfully loaded image for " + member.getName());
                            return false; // Let Glide handle the image setting
                        }
                    })
                    .into(holder.ivProfileImage);
        } else {
            Log.d("FamilyMemberAdapter", "Using default avatar for " + member.getName());
            setDefaultAvatar(holder, member, position);
        }
    }
    private android.graphics.drawable.Drawable getDefaultAvatarDrawable(User member, int position) {
        if ("parent".equals(member.getRole())) {
            return ContextCompat.getDrawable(context, R.drawable.ic_parent);
        } else {
            return ContextCompat.getDrawable(context, R.drawable.ic_child);
        }
    }

    private void setDefaultAvatar(FamilyMemberViewHolder holder, User member, int position) {
        // Clear any existing image first
        holder.ivProfileImage.setImageDrawable(null);

        // Set padding for icon display
        holder.ivProfileImage.setPadding(12, 12, 12, 12);

        // Use different icons and colors for parents vs children
        if ("parent".equals(member.getRole())) {
            holder.ivProfileImage.setImageResource(R.drawable.ic_parent);
            holder.ivProfileImage.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_green)
            );
        } else {
            holder.ivProfileImage.setImageResource(R.drawable.ic_child);

            // Use different colors for children for visual variety
            int[] childColors = {
                    R.color.success_green,
                    R.color.accent_blue,
                    R.color.accent_purple,
                    R.color.accent_orange,
                    R.color.star_yellow
            };
            int colorIndex = position % childColors.length;
            holder.ivProfileImage.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, childColors[colorIndex])
            );
        }
    }
    private void setupStarBalance(FamilyMemberViewHolder holder, User member) {
        if ("child".equals(member.getRole())) {
            holder.tvStarBalance.setVisibility(View.VISIBLE);
            holder.tvStarBalance.setText(String.format("%d ⭐", member.getStarBalance()));
        } else {
            holder.tvStarBalance.setVisibility(View.GONE);
        }
    }

    private void setupActionMenu(FamilyMemberViewHolder holder, User member) {
        holder.btnMemberOptions.setOnClickListener(v -> showActionMenu(v, member));
    }

    private void showActionMenu(View anchor, User member) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.member_actions_menu, popup.getMenu());

        // Customize menu based on member type and permissions
        customizeMenuForMember(popup, member);

        popup.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;

            int itemId = item.getItemId();
            if (itemId == R.id.action_view_details) {
                listener.onViewMemberDetails(member);
                return true;
            } else if (itemId == R.id.action_edit_member) {
                listener.onEditMember(member);
                return true;
            } else if (itemId == R.id.action_view_stats) {
                listener.onViewMemberStats(member);
                return true;
            } else if (itemId == R.id.action_manage_permissions) {
                listener.onManagePermissions(member);
                return true;
            } else if (itemId == R.id.action_remove_member) {
                listener.onRemoveMember(member);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void customizeMenuForMember(PopupMenu popup, User member) {
        // Hide stats for parents (they don't earn stars)
        if ("parent".equals(member.getRole())) {
            popup.getMenu().findItem(R.id.action_view_stats).setVisible(false);
        }

        // Don't allow removing yourself
        if (member.getUserId().equals(currentUserId)) {
            popup.getMenu().findItem(R.id.action_remove_member).setVisible(false);
            popup.getMenu().findItem(R.id.action_manage_permissions).setVisible(false);
        }

        // Only show manage permissions for other parents
        if (!"parent".equals(member.getRole()) || member.getUserId().equals(currentUserId)) {
            popup.getMenu().findItem(R.id.action_manage_permissions).setVisible(false);
        }
    }

    @Override
    public int getItemCount() {
        int count = memberList.size();
        Log.d("FamilyMemberAdapter", "getItemCount: " + count);
        return count;
    }

    // Public methods for data management

    public void updateMemberList(List<User> newMemberList) {
        this.memberList.clear();
        if (newMemberList != null) {
            this.memberList.addAll(newMemberList);
        }
        notifyDataSetChanged();
    }

    public void addMember(User member) {
        if (member != null && !memberList.contains(member)) {
            memberList.add(member);
            notifyItemInserted(memberList.size() - 1);
        }
    }

    public void removeMember(String userId) {
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).getUserId().equals(userId)) {
                memberList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateMember(User updatedMember) {
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).getUserId().equals(updatedMember.getUserId())) {
                memberList.set(i, updatedMember);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public User getMemberAt(int position) {
        if (position >= 0 && position < memberList.size()) {
            return memberList.get(position);
        }
        return null;
    }

    public List<User> getChildren() {
        List<User> children = new ArrayList<>();
        for (User member : memberList) {
            if ("child".equals(member.getRole())) {
                children.add(member);
            }
        }
        return children;
    }

    public List<User> getParents() {
        List<User> parents = new ArrayList<>();
        for (User member : memberList) {
            if ("parent".equals(member.getRole())) {
                parents.add(member);
            }
        }
        return parents;
    }

    public int getTotalStars() {
        int total = 0;
        for (User member : memberList) {
            if ("child".equals(member.getRole())) {
                total += member.getStarBalance();
            }
        }
        return total;
    }

    public boolean isEmpty() {
        return memberList.isEmpty();
    }

    public void sortMembers() {
        // Sort by role (parents first) then by name
        memberList.sort((m1, m2) -> {
            // Parents first
            if ("parent".equals(m1.getRole()) && "child".equals(m2.getRole())) {
                return -1;
            } else if ("child".equals(m1.getRole()) && "parent".equals(m2.getRole())) {
                return 1;
            } else {
                // Same role, sort by name
                return m1.getName().compareToIgnoreCase(m2.getName());
            }
        });

        notifyDataSetChanged();
    }

    static class FamilyMemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImage;
        TextView tvMemberName;
        TextView tvMemberRole;
        TextView tvStarBalance;
        TextView tvInviteCode;
        TextView tvExpiresDate;
        Button btnGenerateCode;
        ImageButton btnMemberOptions;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberRole = itemView.findViewById(R.id.tv_member_role);
            tvStarBalance = itemView.findViewById(R.id.tv_star_balance);
            tvInviteCode = itemView.findViewById(R.id.tv_Invite_code);
            tvExpiresDate = itemView.findViewById(R.id.tv_Expires_date);
            btnGenerateCode = itemView.findViewById(R.id.btn_generate_code);
            btnMemberOptions = itemView.findViewById(R.id.btn_member_options);
        }
    }
}

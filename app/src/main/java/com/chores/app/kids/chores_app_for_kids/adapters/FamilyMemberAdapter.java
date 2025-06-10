package com.chores.app.kids.chores_app_for_kids.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
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

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewMemberDetails(member);
            }
        });

        // Highlight current user
        if (member.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_yellow));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
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
        if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty()) {
            // Load actual profile image using Glide or similar
            // Glide.with(context).load(member.getProfileImageUrl()).into(holder.ivProfileImage);
            setDefaultAvatar(holder, member, position);
        } else {
            setDefaultAvatar(holder, member, position);
        }
    }

    private void setDefaultAvatar(FamilyMemberViewHolder holder, User member, int position) {
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
        return memberList.size();
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
        ImageButton btnMemberOptions;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberRole = itemView.findViewById(R.id.tv_member_role);
            tvStarBalance = itemView.findViewById(R.id.tv_star_balance);
            btnMemberOptions = itemView.findViewById(R.id.btn_member_options);
        }
    }
}

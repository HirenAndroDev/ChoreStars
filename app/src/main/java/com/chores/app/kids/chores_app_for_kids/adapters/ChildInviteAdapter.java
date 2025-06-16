package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChildInviteAdapter extends RecyclerView.Adapter<ChildInviteAdapter.ChildViewHolder> {

    private List<ChildProfile> childProfiles;
    private Context context;
    private OnChildActionListener listener;

    public interface OnChildActionListener {
        void onGenerateInviteCode(ChildProfile child);

        void onShareInviteCode(ChildProfile child);

        void onDeleteChild(ChildProfile child);
    }

    public ChildInviteAdapter(List<ChildProfile> childProfiles, Context context, OnChildActionListener listener) {
        this.childProfiles = childProfiles;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_invite, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildProfile child = childProfiles.get(position);

        holder.tvChildName.setText(child.getName());
        holder.tvStarBalance.setText(String.format("%d ⭐", child.getStarBalance()));

        if (child.getInviteCode() != null && !child.getInviteCode().isEmpty()) {
            if (child.isInviteCodeValid()) {
                holder.tvInviteCode.setText(child.getInviteCode());
                holder.tvInviteCode.setVisibility(View.VISIBLE);
                holder.tvExpiry.setText(formatExpiry(child.getInviteCodeExpiry()));
                holder.tvExpiry.setVisibility(View.VISIBLE);
                holder.btnShare.setVisibility(View.VISIBLE);
                holder.btnGenerate.setText("New Code");
            } else {
                holder.tvInviteCode.setText("Code Expired");
                holder.tvInviteCode.setVisibility(View.VISIBLE);
                holder.tvExpiry.setVisibility(View.GONE);
                holder.btnShare.setVisibility(View.GONE);
                holder.btnGenerate.setText("Generate Code");
            }
        } else {
            holder.tvInviteCode.setVisibility(View.GONE);
            holder.tvExpiry.setVisibility(View.GONE);
            holder.btnShare.setVisibility(View.GONE);
            holder.btnGenerate.setText("Generate Code");
        }

        // Set click listeners
        holder.btnGenerate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenerateInviteCode(child);
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareInviteCode(child);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteChild(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childProfiles.size();
    }

    private String formatExpiry(long expiryTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return "Expires: " + sdf.format(new Date(expiryTime));
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildName;
        TextView tvStarBalance;
        TextView tvInviteCode;
        TextView tvExpiry;
        Button btnGenerate;
        Button btnShare;
        Button btnDelete;
        ImageView ivChildAvatar;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tv_child_name);
            tvStarBalance = itemView.findViewById(R.id.tv_star_balance);
            tvInviteCode = itemView.findViewById(R.id.tv_invite_code);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            btnGenerate = itemView.findViewById(R.id.btn_generate);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivChildAvatar = itemView.findViewById(R.id.iv_child_avatar);
        }
    }
}
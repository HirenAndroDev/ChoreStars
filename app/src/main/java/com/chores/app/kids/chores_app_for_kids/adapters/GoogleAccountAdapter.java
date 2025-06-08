package com.chores.app.kids.chores_app_for_kids.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.chores.app.kids.chores_app_for_kids.R;

import java.util.List;

public class GoogleAccountAdapter extends RecyclerView.Adapter<GoogleAccountAdapter.AccountViewHolder> {
    private List<GoogleSignInAccount> accounts;
    private OnAccountClickListener listener;

    public interface OnAccountClickListener {
        void onAccountClick(GoogleSignInAccount account);
    }

    public GoogleAccountAdapter(List<GoogleSignInAccount> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_google_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        GoogleSignInAccount account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfileImage;
        private TextView tvName;
        private TextView tvEmail;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAccountClick(accounts.get(position));
                    }
                }
            });
        }

        public void bind(GoogleSignInAccount account) {
            tvName.setText(account.getDisplayName());
            tvEmail.setText(account.getEmail());

            if (account.getPhotoUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(account.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }
}
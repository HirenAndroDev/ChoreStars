package com.chores.app.kids.chores_app_for_kids.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import java.util.List;

public class IconSelectionAdapter extends RecyclerView.Adapter<IconSelectionAdapter.IconViewHolder> {

    private List<String> iconList;
    private Context context;
    private OnIconSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnIconSelectedListener {
        void onIconSelected(String iconName);
    }

    public IconSelectionAdapter(List<String> iconList, Context context, OnIconSelectedListener listener) {
        this.iconList = iconList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon_selection, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String iconName = iconList.get(position);

        // Set icon
        int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivIcon.setImageResource(iconResId);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_default);
        }

        // Highlight selected icon
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.primary_green));
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.cardView.setCardElevation(4f);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;

            // Notify changes
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            // Callback
            if (listener != null) {
                listener.onIconSelected(iconName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_icon);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}

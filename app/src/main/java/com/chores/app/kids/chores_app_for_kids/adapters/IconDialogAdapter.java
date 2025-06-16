package com.chores.app.kids.chores_app_for_kids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;

import java.util.List;

public class IconDialogAdapter extends RecyclerView.Adapter<IconDialogAdapter.IconViewHolder> {

    private List<TaskIcon> iconList;
    private Context context;
    private OnIconSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnIconSelectedListener {
        void onIconSelected(TaskIcon icon);
    }

    public IconDialogAdapter(List<TaskIcon> iconList, Context context, OnIconSelectedListener listener) {
        this.iconList = iconList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon_dialog, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        TaskIcon icon = iconList.get(position);

        // Load icon image - check both iconUrl and drawableName
        if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
            // Load from URL
            Glide.with(context)
                    .load(icon.getIconUrl())
                    .placeholder(R.drawable.ic_brush_teeth)
                    .error(R.drawable.ic_brush_teeth)
                    .into(holder.iconImage);
        } else if (icon.getDrawableName() != null && !icon.getDrawableName().isEmpty()) {
            // Load from drawable resource
            int drawableResId = context.getResources().getIdentifier(icon.getDrawableName(), "drawable", context.getPackageName());
            if (drawableResId != 0) {
                holder.iconImage.setImageResource(drawableResId);
            } else {
                holder.iconImage.setImageResource(R.drawable.ic_brush_teeth);
            }
        } else {
            // Default icon
            holder.iconImage.setImageResource(R.drawable.ic_brush_teeth);
        }

        // Handle selection state
        holder.iconImage.setSelected(position == selectedPosition);
        holder.cardView.setCardBackgroundColor(
                position == selectedPosition ?
                        context.getColor(R.color.primary_green) :
                        context.getColor(android.R.color.white)
        );

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onIconSelected(icon);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        androidx.cardview.widget.CardView cardView;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iv_dialog_icon);
            cardView = itemView.findViewById(R.id.card_dialog_icon);
        }
    }
}

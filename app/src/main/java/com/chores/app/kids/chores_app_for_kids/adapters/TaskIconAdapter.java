package com.chores.app.kids.chores_app_for_kids.adapters;

import android.annotation.SuppressLint;
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

public class TaskIconAdapter extends RecyclerView.Adapter<TaskIconAdapter.IconViewHolder> {

    private List<TaskIcon> iconList;
    private Context context;
    private OnIconSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnIconSelectedListener {
        void onIconSelected(TaskIcon icon);
    }

    public TaskIconAdapter(List<TaskIcon> iconList, Context context, OnIconSelectedListener listener) {
        this.iconList = iconList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_icon, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TaskIcon icon = iconList.get(position);

        // Load icon image
        if (icon.getIconUrl() != null && !icon.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(icon.getIconUrl())
                    .placeholder(R.drawable.ic_task_default)
                    .into(holder.iconImage);
        } else {
            holder.iconImage.setImageResource(R.drawable.ic_task_default);
        }

        // Handle selection state
        holder.iconImage.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(previousSelected);
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

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iv_icon);
        }
    }
}
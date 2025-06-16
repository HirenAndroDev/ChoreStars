package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.PresetSelectionAdapter;
import com.chores.app.kids.chores_app_for_kids.models.TaskPreset;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PresetSelectionDialog extends DialogFragment {

    private RecyclerView recyclerViewPresets;
    private ImageView btnCloseDialog;
    private PresetSelectionAdapter presetAdapter;
    private List<TaskPreset> availablePresets;
    private OnPresetSelectedListener listener;

    public interface OnPresetSelectedListener {
        void onPresetSelected(TaskPreset preset);
    }

    public static PresetSelectionDialog newInstance() {
        return new PresetSelectionDialog();
    }

    public void setOnPresetSelectedListener(OnPresetSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_preset_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadTaskPresets();
    }

    private void initializeViews(View view) {
        recyclerViewPresets = view.findViewById(R.id.recycler_view_presets);
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        availablePresets = new ArrayList<>();
    }

    private void setupRecyclerView() {
        presetAdapter = new PresetSelectionAdapter(availablePresets, getContext(), new PresetSelectionAdapter.OnPresetSelectedListener() {
            @Override
            public void onPresetSelected(TaskPreset preset) {
                if (listener != null) {
                    listener.onPresetSelected(preset);
                }
                dismiss();
            }
        });

        recyclerViewPresets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPresets.setAdapter(presetAdapter);
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());
    }

    private void loadTaskPresets() {
        FirebaseHelper.getTaskPresets(new FirebaseHelper.TaskPresetsCallback() {
            @Override
            public void onPresetsLoaded(List<TaskPreset> presets) {
                availablePresets.clear();
                availablePresets.addAll(presets);
                presetAdapter.notifyDataSetChanged();

                if (presets.isEmpty()) {
                    Toast.makeText(getContext(), "No presets available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load presets: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
    }
}
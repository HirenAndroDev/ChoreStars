package com.chores.app.kids.chores_app_for_kids.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.IconDialogAdapter;
import com.chores.app.kids.chores_app_for_kids.models.TaskIcon;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class IconSelectionDialog extends DialogFragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAMERA_CAPTURE = 200;
    private static final int REQUEST_GALLERY_PICK = 300;

    private RecyclerView recyclerViewIcons;
    private ImageView ivPreviewIcon, btnCloseDialog;
    private CardView cardCamera, cardGallery;
    private IconDialogAdapter iconAdapter;
    private List<TaskIcon> availableIcons;
    private TaskIcon selectedIcon;
    private OnIconSelectedListener listener;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    public interface OnIconSelectedListener {
        void onIconSelected(TaskIcon icon);
    }

    public static IconSelectionDialog newInstance() {
        return new IconSelectionDialog();
    }

    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_icon_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadTaskIcons();
    }

    private void initializeViews(View view) {
        recyclerViewIcons = view.findViewById(R.id.recycler_view_icons);
        ivPreviewIcon = view.findViewById(R.id.iv_preview_icon);
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        cardCamera = view.findViewById(R.id.card_camera);
        cardGallery = view.findViewById(R.id.card_gallery);

        availableIcons = new ArrayList<>();
    }

    private void setupRecyclerView() {
        iconAdapter = new IconDialogAdapter(availableIcons, getContext(), new IconDialogAdapter.OnIconSelectedListener() {
            @Override
            public void onIconSelected(TaskIcon icon) {
                selectedIcon = icon;
                updatePreviewIcon();

                // Auto-close dialog and return selected icon
                if (listener != null) {
                    listener.onIconSelected(icon);
                }
                dismiss();
            }
        });

        recyclerViewIcons.setLayoutManager(new GridLayoutManager(getContext(), 5));
        recyclerViewIcons.setAdapter(iconAdapter);
    }

    private void setupClickListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());

        cardCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        cardGallery.setOnClickListener(v -> openGallery());
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);
        } else {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_GALLERY_PICK);
        } else {
            Toast.makeText(getContext(), "Gallery not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        if (bitmap != null) {
                            uploadImageToFirebase(bitmap, "camera");
                        }
                    }
                    break;

                case REQUEST_GALLERY_PICK:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        uploadImageToFirebase(imageUri, "gallery");
                    }
                    break;
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap, String source) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        // Create unique filename
        String filename = "custom_icons/" + AuthHelper.getCurrentUserId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        // Show loading
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

        // Upload file
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Create custom TaskIcon
                        TaskIcon customIcon = new TaskIcon();
                        customIcon.setName("Custom Icon");
                        customIcon.setIconUrl(uri.toString());
                        customIcon.setCategory("custom");
                        customIcon.setDefault(false);
                        customIcon.setCreatedTimestamp(System.currentTimeMillis());

                        // Save to Firebase and add to list
                        FirebaseHelper.addTaskIcon(customIcon, task -> {
                            if (task.isSuccessful()) {
                                customIcon.setId(task.getResult().getId());
                                availableIcons.add(0, customIcon); // Add at beginning
                                iconAdapter.notifyItemInserted(0);

                                // Auto-select the new icon
                                selectedIcon = customIcon;
                                updatePreviewIcon();

                                Toast.makeText(getContext(), "Icon uploaded successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to save icon", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageToFirebase(Uri imageUri, String source) {
        // Create unique filename
        String filename = "custom_icons/" + AuthHelper.getCurrentUserId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        // Show loading
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

        // Upload file
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Create custom TaskIcon
                        TaskIcon customIcon = new TaskIcon();
                        customIcon.setName("Custom Icon");
                        customIcon.setIconUrl(uri.toString());
                        customIcon.setCategory("custom");
                        customIcon.setDefault(false);
                        customIcon.setCreatedTimestamp(System.currentTimeMillis());

                        // Save to Firebase and add to list
                        FirebaseHelper.addTaskIcon(customIcon, task -> {
                            if (task.isSuccessful()) {
                                customIcon.setId(task.getResult().getId());
                                availableIcons.add(0, customIcon); // Add at beginning
                                iconAdapter.notifyItemInserted(0);

                                // Auto-select the new icon
                                selectedIcon = customIcon;
                                updatePreviewIcon();

                                Toast.makeText(getContext(), "Icon uploaded successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to save icon", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadTaskIcons() {
        // Show loading indicator
        Toast.makeText(getContext(), "Loading icons...", Toast.LENGTH_SHORT).show();

        // Load icons from Firestore
        FirebaseHelper.getTaskIcons(new FirebaseHelper.TaskIconsCallback() {
            @Override
            public void onIconsLoaded(List<TaskIcon> icons) {
                android.util.Log.d("IconSelectionDialog", "Loaded " + icons.size() + " icons from Firestore");

                availableIcons.clear();
                availableIcons.addAll(icons);

                if (icons.isEmpty()) {
                    // Add default icons locally if no icons from Firebase
                    addDefaultIcons();
                    Toast.makeText(getContext(), "Using default icons", Toast.LENGTH_SHORT).show();
                } else {
                    iconAdapter.notifyDataSetChanged();
                    // Set first icon as preview if available
                    if (!icons.isEmpty()) {
                        selectedIcon = icons.get(0);
                        updatePreviewIcon();
                    }
                    Toast.makeText(getContext(), "Icons loaded successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("IconSelectionDialog", "Failed to load icons: " + error);
                Toast.makeText(getContext(), "Failed to load icons: " + error, Toast.LENGTH_SHORT).show();

                // Add default icons as fallback
                addDefaultIcons();
            }
        });
    }

    private void addDefaultIcons() {
        // Create default icons with drawable resources
        List<TaskIcon> defaultIcons = new ArrayList<>();

        // Add default icons (you can expand this list)
        defaultIcons.add(createDefaultIcon("Brush Teeth", "personal_care", "ic_brush_teeth"));
        defaultIcons.add(createDefaultIcon("Clean Room", "chores", "ic_clean_room"));
        defaultIcons.add(createDefaultIcon("Do Homework", "education", "ic_homework"));
        defaultIcons.add(createDefaultIcon("Feed Pet", "pets", "ic_pet"));
        defaultIcons.add(createDefaultIcon("Take Shower", "personal_care", "ic_shower"));
        defaultIcons.add(createDefaultIcon("Make Bed", "chores", "ic_bed"));
        defaultIcons.add(createDefaultIcon("Wash Dishes", "chores", "ic_dishes"));
        defaultIcons.add(createDefaultIcon("Exercise", "health", "ic_exercise"));

        availableIcons.addAll(defaultIcons);
        iconAdapter.notifyDataSetChanged();

        if (!defaultIcons.isEmpty()) {
            selectedIcon = defaultIcons.get(0);
            updatePreviewIcon();
        }
    }

    private TaskIcon createDefaultIcon(String name, String category, String drawableName) {
        TaskIcon icon = new TaskIcon();
        icon.setName(name);
        icon.setIconUrl(""); // Empty URL for drawable resources
        icon.setCategory(category);
        icon.setDefault(true);
        icon.setCreatedTimestamp(System.currentTimeMillis());
        return icon;
    }

    private void updatePreviewIcon() {
        if (selectedIcon != null && ivPreviewIcon != null) {
            if (selectedIcon.getIconUrl() != null && !selectedIcon.getIconUrl().isEmpty()) {
                Glide.with(this)
                        .load(selectedIcon.getIconUrl())
                        .placeholder(R.drawable.ic_brush_teeth)
                        .error(R.drawable.ic_brush_teeth)
                        .into(ivPreviewIcon);
            } else {
                // Use default drawable
                ivPreviewIcon.setImageResource(R.drawable.ic_brush_teeth);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
    }
}
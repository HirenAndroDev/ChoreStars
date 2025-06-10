package com.chores.app.kids.chores_app_for_kids.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.chores.app.kids.chores_app_for_kids.utils.ImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddKidProfileActivity extends AppCompatActivity {

    private static final String TAG = "AddKidProfileActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText etKidName;
    private ImageView ivKidProfile;
    private Button btnTakePhoto;
    private Button btnChooseGallery;
    private Button btnSaveKid;
    private Button btnCancel;

    private String familyId;
    private Bitmap selectedImageBitmap;
    private boolean hasSelectedImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_kid_profile);

        // Get family ID from intent
        familyId = getIntent().getStringExtra("familyId");
        if (familyId == null || familyId.isEmpty()) {
            Toast.makeText(this, "Error: Family not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        setDefaultProfileImage();
    }

    private void initializeViews() {
        etKidName = findViewById(R.id.et_kid_name);
        ivKidProfile = findViewById(R.id.iv_kid_profile);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnChooseGallery = findViewById(R.id.btn_choose_gallery);
        btnSaveKid = findViewById(R.id.btn_save_kid);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setDefaultProfileImage() {
        // Set default child icon
        ivKidProfile.setImageResource(R.drawable.ic_child);
        hasSelectedImage = false;
    }

    private void setupClickListeners() {
        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnChooseGallery.setOnClickListener(v -> openGallery());
        btnSaveKid.setOnClickListener(v -> saveKidProfile());
        btnCancel.setOnClickListener(v -> finish());

        // Add click listener to profile image to show options
        ivKidProfile.setOnClickListener(v -> showImageOptions());
    }

    private void showImageOptions() {
        // Create simple dialog for image selection
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Profile Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery", "Remove Photo"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    openCamera();
                                    break;
                                case 1:
                                    openGallery();
                                    break;
                                case 2:
                                    removePhoto();
                                    break;
                            }
                        })
                .show();
    }

    private void removePhoto() {
        setDefaultProfileImage();
        selectedImageBitmap = null;
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            } else {
                Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission required to take photos", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Storage permission required to select photos", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap capturedBitmap = (Bitmap) extras.get("data");
                        if (capturedBitmap != null) {
                            processSelectedImage(capturedBitmap);
                        }
                    }
                    break;
                case REQUEST_IMAGE_GALLERY:
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = ImageHelper.getBitmapFromUri(this, selectedImageUri);
                            if (bitmap != null) {
                                processSelectedImage(bitmap);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing selected image", e);
                            Toast.makeText(this, "Error processing selected image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }

    private void processSelectedImage(Bitmap bitmap) {
        try {
            // Compress and resize the image
            selectedImageBitmap = ImageHelper.compressBitmap(bitmap);

            // Create circular bitmap for display
            Bitmap circularBitmap = ImageHelper.createCircularBitmap(selectedImageBitmap);

            // Display the processed image
            ivKidProfile.setImageBitmap(circularBitmap);
            hasSelectedImage = true;

            Log.d(TAG, "Image processed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            setDefaultProfileImage();
        }
    }

    private void saveKidProfile() {
        if (!validateInput()) {
            return;
        }

        String kidName = etKidName.getText().toString().trim();

        // Show loading state
        btnSaveKid.setEnabled(false);
        btnSaveKid.setText("Saving...");

        // Check if user selected an image
        if (hasSelectedImage && selectedImageBitmap != null) {
            uploadImageAndSaveProfile(kidName);
        } else {
            // No image selected, save profile without image
            saveKidToFirestore(kidName, "");
        }
    }

    private void uploadImageAndSaveProfile(String kidName) {
        // Check if user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Authentication required. Please sign in again.", Toast.LENGTH_SHORT).show();
            resetButtonState();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create unique filename with timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = "kid_profiles/" + familyId + "_" + timestamp + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        // Convert bitmap to byte array with compression
        byte[] imageData = ImageHelper.bitmapToByteArray(selectedImageBitmap);

        if (imageData != null) {
            // Validate image size (max 5MB)
            if (!ImageHelper.isValidImageSize(imageData, 5)) {
                Toast.makeText(this, "Image too large. Please select a smaller image.", Toast.LENGTH_SHORT).show();
                resetButtonState();
                return;
            }

            Log.d(TAG, "Uploading image: " + filename + ", Size: " + imageData.length + " bytes");

            // Upload image with metadata
            imageRef.putBytes(imageData)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "Image uploaded successfully");
                        // Get download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            Log.d(TAG, "Download URL: " + profileImageUrl);
                            saveKidToFirestore(kidName, profileImageUrl);
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL", e);
                            Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            resetButtonState();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload image", e);
                        String errorMessage = e.getMessage();

                        // Handle specific error cases
                        if (errorMessage != null) {
                            if (errorMessage.contains("Permission denied") || errorMessage.contains("administration only")) {
                                Toast.makeText(this, "Storage permission error. Saving profile without image.", Toast.LENGTH_LONG).show();
                                // Save without image if storage fails
                                saveKidToFirestore(kidName, "");
                                return;
                            }
                        }

                        Toast.makeText(this, "Failed to upload image. Saving profile without image.", Toast.LENGTH_SHORT).show();
                        // Save without image if upload fails
                        saveKidToFirestore(kidName, "");
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        // Show upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "Upload progress: " + progress + "%");

                        // Update button text with progress
                        runOnUiThread(() -> {
                            btnSaveKid.setText("Uploading... " + Math.round(progress) + "%");
                        });
                    });
        } else {
            Toast.makeText(this, "Error processing image. Saving profile without image.", Toast.LENGTH_SHORT).show();
            saveKidToFirestore(kidName, "");
        }
    }

    private void saveKidToFirestore(String kidName, String imageUrl) {
        Log.d(TAG, "Saving kid profile: " + kidName + " to family: " + familyId);

        // Create child user
        FirebaseHelper.createChildUser(kidName, familyId, imageUrl, new FirebaseHelper.CreateUserCallback() {
            @Override
            public void onUserCreated(String userId) {
                Log.d(TAG, "Child user created successfully: " + userId);

                runOnUiThread(() -> {
                    Toast.makeText(AddKidProfileActivity.this,
                            "Kid profile created successfully! 🎉", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to create child user: " + error);

                runOnUiThread(() -> {
                    Toast.makeText(AddKidProfileActivity.this,
                            "Failed to create profile: " + error, Toast.LENGTH_LONG).show();
                    resetButtonState();
                });
            }
        });
    }

    private boolean validateInput() {
        String kidName = etKidName.getText().toString().trim();

        if (TextUtils.isEmpty(kidName)) {
            etKidName.setError("Kid's name is required");
            etKidName.requestFocus();
            return false;
        }

        if (kidName.length() < 2) {
            etKidName.setError("Name must be at least 2 characters");
            etKidName.requestFocus();
            return false;
        }

        if (kidName.length() > 20) {
            etKidName.setError("Name must be less than 20 characters");
            etKidName.requestFocus();
            return false;
        }

        // Check for valid characters (letters, spaces, and common name characters)
        if (!kidName.matches("[a-zA-Z\\s'-]+")) {
            etKidName.setError("Name can only contain letters, spaces, hyphens, and apostrophes");
            etKidName.requestFocus();
            return false;
        }

        return true;
    }

    private void resetButtonState() {
        btnSaveKid.setEnabled(true);
        btnSaveKid.setText("Save Kid Profile");
    }
}
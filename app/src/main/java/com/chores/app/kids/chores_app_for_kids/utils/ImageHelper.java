package com.chores.app.kids.chores_app_for_kids.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int JPEG_QUALITY = 80; // JPEG compression quality (0-100)

    /**
     * Compress and resize bitmap to reduce file size
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        // Calculate new dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap; // No need to resize
        }

        float ratio = Math.min(
                (float) MAX_IMAGE_SIZE / width,
                (float) MAX_IMAGE_SIZE / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Convert bitmap to byte array with compression
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap compressedBitmap = compressBitmap(bitmap);
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
        return baos.toByteArray();
    }

    /**
     * Get bitmap from URI with proper orientation
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            // Fix orientation if needed
            return fixImageOrientation(context, uri, bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap from URI", e);
            return null;
        }
    }

    /**
     * Fix image orientation based on EXIF data
     */
    private static Bitmap fixImageOrientation(Context context, Uri uri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap; // No rotation needed
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (inputStream != null) {
                inputStream.close();
            }
            return rotatedBitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error fixing image orientation", e);
            return bitmap; // Return original bitmap if error
        }
    }

    /**
     * Create circular bitmap for profile pictures
     */
    public static Bitmap createCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Validate image file size (in bytes)
     */
    public static boolean isValidImageSize(byte[] imageData, int maxSizeInMB) {
        if (imageData == null) return false;

        int maxSizeInBytes = maxSizeInMB * 1024 * 1024; // Convert MB to bytes
        return imageData.length <= maxSizeInBytes;
    }

    /**
     * Get image file extension from URI
     */
    public static String getImageExtension(Context context, Uri uri) {
        try {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
                    return ".jpg";
                } else if (mimeType.equals("image/png")) {
                    return ".png";
                } else if (mimeType.equals("image/webp")) {
                    return ".webp";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting image extension", e);
        }
        return ".jpg"; // Default to jpg
    }
}
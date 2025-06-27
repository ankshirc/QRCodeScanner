package com.example.qrcodescanner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import android.provider.MediaStore;
import com.example.qrcodescanner.RGBLuminanceSource;
import android.graphics.Bitmap;
import android.net.Uri;


import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.IOException;


public class QRCodeScanner extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_IMAGE_PICK = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getStringExtra("action");

        if ("upload".equals(action)) {
            pickImageFromGallery();
        } else {
            // Default to scanning
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission()) {
                    startScanning();
                } else {
                    requestPermission();
                }
            } else {
                startScanning();
            }
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    private void startScanning() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Handle camera permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                startScanning();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    showMessageOKCancel("Camera permission is required to scan QR codes",
                            (dialog, which) -> requestPermission());
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Handle result from scanner
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    decodeQRCodeFromBitmap(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
                }
            }
            return; // 🚫 Prevent scanner fallback below
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            showScanResult(result.getContents());
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            finish(); // close activity if cancelled
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    decodeQRCodeFromBitmap(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void decodeQRCodeFromBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            showScanResult(result.getText());
        } catch (Exception e) {
            Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
        }
    }


    private void showScanResult(String resultText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");

        // Create a clickable TextView
        final TextView linkView = new TextView(this);
        linkView.setText(resultText);
        linkView.setAutoLinkMask(Linkify.WEB_URLS); // Auto-detect links
        linkView.setMovementMethod(LinkMovementMethod.getInstance());
        linkView.setPadding(50, 30, 50, 30); // optional padding
        linkView.setTextSize(16);

        builder.setView(linkView);

        builder.setPositiveButton("Scan Again", (dialog, which) -> startScanning());
        builder.setNegativeButton("Close", (dialog, which) -> finish());

        builder.show();
    }

}

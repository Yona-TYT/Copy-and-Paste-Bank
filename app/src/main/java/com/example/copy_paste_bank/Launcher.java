package com.example.copy_paste_bank;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import android.Manifest;

public class Launcher extends AppCompatActivity implements DefaultLifecycleObserver {

    private final ActivityResultRegistry registry;
    private final Context context;
    private final OnCapture onCapture;

    private String uniqueKey = Long.toString(System.currentTimeMillis());

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private Uri captureImageUri;

    public Launcher(ActivityResultRegistry registry, Context context, OnCapture onCapture) {
        this.registry = registry;
        this.context = context;
        this.onCapture = onCapture;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        cameraPermissionLauncher = registry.register(
                "keyCameraPermission_" + uniqueKey,
                owner,
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        try {
                            dispatchTakePictureIntent();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        openAppSettings(context);
                    }
                }
        );

        takePictureLauncher = registry.register(
                "keyCameraLauncher_" + uniqueKey,
                owner,
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (captureImageUri != null) {
                            onCapture.invoke(captureImageUri);
                        }
                    }
                }
        );

        selectPictureLauncher = registry.register(
                "keyPicker_" + uniqueKey,
                owner,
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            onCapture.invoke(uri);
                        }
                    }
                }
        );
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void dispatchSelectPictureIntent() {
        selectPictureLauncher.launch("image/*");
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = File.createTempFile("IMG_", ".jpg", context.getCacheDir());

        captureImageUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri);

        //Basic.msg(""+cameraLauncher);
        takePictureLauncher.launch(cameraIntent);
    }

    public void openAppSettings(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void launchCamera() throws IOException {
        if (isPermissionGranted(context, Manifest.permission.CAMERA)) {
            dispatchTakePictureIntent();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void launchPicker() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            dispatchSelectPictureIntent();
        }
    }

    public boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public interface OnCapture {
        void invoke(Uri uri);
    }
}
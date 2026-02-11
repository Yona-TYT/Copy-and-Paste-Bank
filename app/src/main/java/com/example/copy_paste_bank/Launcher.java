package com.example.copy_paste_bank;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.DefaultLifecycleObserver;

import androidx.lifecycle.LifecycleOwner;
import androidx.annotation.NonNull;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;

public class Launcher implements DefaultLifecycleObserver {

    private static final String TAG = "Launcher";

    private final ActivityResultRegistry registry;
    private final Context context;
    private final OnCapture onCapture;

    private String uniqueKey = Long.toString(System.currentTimeMillis());

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> multiplePickerLauncher;
    private ActivityResultLauncher<Intent> proCropLauncher;

    private Uri captureImageUri;
    private ComponentActivity  activity;

    public Launcher(ActivityResultRegistry registry, Context context, OnCapture onCapture) {
        this.registry = registry;
        this.context = context;
        this.onCapture = onCapture;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        this.activity = (ComponentActivity) owner;
        cameraPermissionLauncher = registry.register(
                "keyCameraPermission_" + uniqueKey,
                owner,
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        try {
                            dispatchTakePictureIntent();
                        } catch (IOException e) {
                            Log.e(TAG, "Error al iniciar cámara", e);
                        }
                    } else {
                        openAppSettings(context);
                    }
                }
        );

        proCropLauncher = registry.register("keyProCrop_" + uniqueKey, owner,
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri croppedUri = result.getData().getData(); // ← Este Uri SÍ tiene permiso
                        onCapture.invoke(Collections.singletonList(croppedUri));
                    } else {
                        onCapture.invoke(Collections.singletonList(captureImageUri));
                    }
                });

        proCropLauncher = registry.register("keyProCrop_" + uniqueKey, owner,
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri croppedUri = result.getData().getData();
                        onCapture.invoke(Collections.singletonList(croppedUri));
                    } else {
                        // Si el usuario pulsó "Retomar" (RESULT_CANCELED), volvemos a la cámara
                        try {
                            launchCamera(); // ← vuelve a abrir la cámara para nueva foto
                        } catch (IOException e) {
                            Log.e(TAG, "Error al relanzar cámara", e);
                            onCapture.invoke(Collections.emptyList());
                        }
                    }
                });


        // Callback del takePictureLauncher (adaptado para recibir el Uri de la foto capturada)
        takePictureLauncher = registry.register(
                "keyCameraLauncher_" + uniqueKey,
                owner,
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri photoUri = result.getData().getData(); // ← Uri de la foto capturada (con permiso)

                        // Preparamos el archivo para el recorte
                        File croppedFile = new File(activity.getCacheDir(), "cropped_final.jpg");

                        // Lanzamos el recorte
                        Intent cropIntent = ProCropActivity.buildIntent(activity, photoUri, croppedFile.getAbsolutePath());

                        if (proCropLauncher != null) {
                            proCropLauncher.launch(cropIntent);
                        } else {
                            Log.e(TAG, "proCropLauncher no inicializado");
                            onCapture.invoke(Collections.singletonList(photoUri));
                        }
                    } else {
                        onCapture.invoke(Collections.emptyList());
                    }
                }
        );

//        takePictureLauncher = registry.register(
//                "keyCameraLauncher_" + uniqueKey,
//                owner,
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    // Si la captura de la cámara fue exitosa (RESULT_OK) Y tenemos la URI donde se guardó la imagen original
//                    if (result.getResultCode() == Activity.RESULT_OK && captureImageUri != null) {
//
//                        // Preparamos la ruta de salida para la imagen recortada final
//                        File croppedFile = new File(activity.getCacheDir(), "cropped_final.jpg");
//
//                        // 1. Construimos el Intent usando el método estático corregido
//                        Intent cropIntent = ProCropActivity.buildIntent(
//                                activity,
//                                captureImageUri,
//                                croppedFile.getAbsolutePath()
//                        );
//
//                        // 2. Ejecutamos el siguiente lanzador
//                        if (proCropLauncher != null) {
//                            proCropLauncher.launch(cropIntent);
//                        } else {
//                            // Manejar error si proCropLauncher no está listo
//                            Log.e("MiApp", "proCropLauncher no inicializado al tomar foto.");
//                            onCapture.invoke(Collections.singletonList(captureImageUri)); // Fallback
//                        }
//                    } else {
//                        // Si el usuario canceló la cámara o hubo un error, se maneja aquí.
//                        onCapture.invoke(Collections.emptyList());
//                    }
//                }
//        );

        // Nuevo: Launcher para picker múltiple
        multiplePickerLauncher = registry.register(
                "keyMultiplePicker_" + uniqueKey,
                owner,
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Log.d(TAG, "Imágenes seleccionadas: procesando...");
                            List<Uri> selectedUris = new ArrayList<>();
                            ClipData clipData = result.getData().getClipData();
                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri imageUri = clipData.getItemAt(i).getUri();
                                    selectedUris.add(imageUri);
                                    Log.d(TAG, "Uri múltiple agregada: " + imageUri);
                                }
                            } else if (result.getData().getData() != null) {
                                selectedUris.add(result.getData().getData());
                                Log.d(TAG, "Uri simple agregada: " + result.getData().getData());
                            }
                            onCapture.invoke(selectedUris);
                        } else {
                            Log.d(TAG, "Selección cancelada o fallida");
                        }
                    }
                }
        );
    }

    /**
     * Configura listeners en una vista: click para picker múltiple, long click para cámara.
     * @param view La vista a adjuntar.
     * @param useMultiple Permite seleccionar multiples imagenes
     * @param useLongClick Si true, long click lanza cámara.
     */
    public void attachToViewPicker(View view, boolean useMultiple, boolean useLongClick) {
        // Long click: picker múltiple
        if (useLongClick) {
            view.setOnLongClickListener(v -> {
                launchPicker(useMultiple);
                return true;  // Consume el evento
            });
        }
        else {
            // Click simple: picker múltiple
            view.setOnClickListener(v -> launchPicker(useMultiple));
        }
    }


    public void attachToViewCam(View view, boolean useLongClick) {
        // Long click: cámara
        if (useLongClick) {
            view.setOnLongClickListener(v -> {
                try {
                    launchCamera();
                } catch (IOException e) {
                    Log.e(TAG, "Error en long click", e);
                }
                return true;  // Consume el evento
            });
        }
        else {
            // Click simple: cámara
            view.setOnClickListener(v -> {
                try {
                    launchCamera();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void dispatchTakePictureIntent() throws IOException {
        // Creamos el archivo donde la cámara personalizada guardará la foto
        File photoFile = File.createTempFile("IMG_", ".jpg", context.getCacheDir());
        captureImageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);

        // Lanzamos tu cámara personalizada
        Intent cameraIntent = new Intent(activity, CustomCameraActivity.class);
        cameraIntent.putExtra("output_path", photoFile.getAbsolutePath());
        takePictureLauncher.launch(cameraIntent);
    }

//    private void dispatchTakePictureIntent() throws IOException {
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File file = File.createTempFile("IMG_", ".jpg", context.getCacheDir());
//        captureImageUri = FileProvider.getUriForFile(
//                context,
//                context.getPackageName() + ".fileprovider",
//                file
//        );
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri);
//        takePictureLauncher.launch(cameraIntent);
//    }

    /**
     * Lanza picker para múltiples imágenes (unificado).
     */
    public void launchPicker(boolean useMultiple) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, useMultiple);  // Permite múltiples
        multiplePickerLauncher.launch(intent);
    }

    /**
     * Lanza cámara si tiene permisos.
     */
    public void launchCamera() throws IOException {
        if (isPermissionGranted(context, Manifest.permission.CAMERA)) {
            dispatchTakePictureIntent();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void openAppSettings(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Interfaz actualizada: ahora maneja lista de URIs (para múltiples o single).
     */
    public interface OnCapture {
        void invoke(List<Uri> uris);
    }
}
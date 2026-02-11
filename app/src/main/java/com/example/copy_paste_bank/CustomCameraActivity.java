package com.example.copy_paste_bank;

import android.content.Intent;
import androidx.camera.core.Camera;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class CustomCameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;

    private Camera camera; // ← Para controlar el zoom

    private String outputPath; // Ruta donde guardar la foto capturada

    private ScaleGestureDetector scaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        outputPath = getIntent().getStringExtra("output_path");
        if (outputPath == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);


        previewView = new PreviewView(this);
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        root.addView(previewView);

        // Detector de pellizco para zoom
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (camera == null) return false;

                ZoomState zoomState = camera.getCameraInfo().getZoomState().getValue();
                if (zoomState == null) return false;

                float currentRatio = zoomState.getZoomRatio();
                float scaleFactor = detector.getScaleFactor();

                float newRatio = currentRatio * scaleFactor;
                newRatio = Math.max(zoomState.getMinZoomRatio(), Math.min(newRatio, zoomState.getMaxZoomRatio()));

                camera.getCameraControl().setZoomRatio(newRatio);
                return true;
            }
        });

        // Aplicamos el detector al previewView
        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        previewView = new PreviewView(this);
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        root.addView(previewView);

        // NUEVO BOTÓN "X" – Estilo circular para armonizar con el de captura
        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeBtn.setColorFilter(Color.WHITE); // Icono blanco

        // Fondo circular oscuro semitransparente (estilo UI de cámara moderna)
        GradientDrawable closeShape = new GradientDrawable();
        closeShape.setShape(GradientDrawable.OVAL);
        closeShape.setColor(0x66000000); // Negro con 40% de transparencia
        closeBtn.setBackground(closeShape);

        // Centrar el icono dentro del círculo
        closeBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int padding = dp(12);
        closeBtn.setPadding(padding, padding, padding, padding);

        // Ajustar posición para que esté alineado verticalmente con el centro del botón grande
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        closeParams.setMargins(dp(30), 0, 0, dp(56)); // Ajustado para equilibrio visual
        root.addView(closeBtn, closeParams);

        closeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        // Botón capturar grande (estilo Telegram)
        ImageButton captureBtn = new ImageButton(this);
        captureBtn.setImageResource(android.R.drawable.ic_menu_camera);
        captureBtn.setBackgroundColor(Color.WHITE);
        captureBtn.setColorFilter(Color.BLACK);
        captureBtn.setScaleType(ImageView.ScaleType.CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(80), dp(80));
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 0, 0, dp(40));

        // Crear un fondo redondo (ovalado) programáticamente
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.WHITE); // Color de fondo
        captureBtn.setBackground(shape);

        // Para un efecto visual más profesional (Ripple), puedes añadir:
        captureBtn.setElevation(dp(4)); // Sombra ligera

        root.addView(captureBtn, params);

        captureBtn.setOnClickListener(v -> takePhoto());


        // NUEVO BOTÓN DE FLASH (a la derecha del capturar)
        ImageButton flashBtn = new ImageButton(this);
        flashBtn.setScaleType(ImageView.ScaleType.CENTER);
        FrameLayout.LayoutParams flashParams = new FrameLayout.LayoutParams(dp(56), dp(56));
        flashParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        flashParams.setMargins(0, 0, dp(20), dp(52)); // Alineado con el capturar
        root.addView(flashBtn, flashParams);

        // Fondo redondo y sombra para el botón de flash
        GradientDrawable flashShape = new GradientDrawable();
        flashShape.setShape(GradientDrawable.OVAL);
        flashShape.setColor(Color.parseColor("#CC000000")); // Fondo semi-transparente
        flashBtn.setBackground(flashShape);
        flashBtn.setElevation(dp(6));

        // Estado inicial del flash
        int[] flashMode = {ImageCapture.FLASH_MODE_OFF};
        updateFlashIcon(flashBtn, flashMode[0]);

        flashBtn.setOnClickListener(v -> {
            // Ciclo: OFF → AUTO → ON → OFF
            if (flashMode[0] == ImageCapture.FLASH_MODE_OFF) {
                flashMode[0] = ImageCapture.FLASH_MODE_AUTO;
            } else if (flashMode[0] == ImageCapture.FLASH_MODE_AUTO) {
                flashMode[0] = ImageCapture.FLASH_MODE_ON;
            } else {
                flashMode[0] = ImageCapture.FLASH_MODE_OFF;
            }

            if (imageCapture != null) {
                imageCapture.setFlashMode(flashMode[0]);
            }
            updateFlashIcon(flashBtn, flashMode[0]);
        });

        startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera(); // ← Reinicia la cámara al volver (retomar foto)
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = providerFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())  // ← CORREGIDO AQUÍ (nunca null)
                        .build();

                CameraSelector selector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                provider.unbindAll();
                Camera boundCamera = provider.bindToLifecycle(this, selector, preview, imageCapture);
                camera = boundCamera;

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        File photoFile = new File(outputPath);

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults output) {
                Uri photoUri = FileProvider.getUriForFile(CustomCameraActivity.this,
                        getPackageName() + ".fileprovider", photoFile);

                Intent result = new Intent();
                result.setData(photoUri);
                result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                setResult(RESULT_OK, result);
                finish();
            }

            @Override
            public void onError(ImageCaptureException exc) {
                exc.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void updateFlashIcon(ImageButton button, int mode) {
        int iconRes;
        int tintColor = Color.WHITE;

        switch (mode) {
            case ImageCapture.FLASH_MODE_AUTO:
                iconRes = R.drawable.ic_flash_auto;
                break;
            case ImageCapture.FLASH_MODE_ON:
                iconRes = R.drawable.ic_flash_on;
                tintColor = Color.YELLOW; // Amarillo para indicar activo
                break;
            case ImageCapture.FLASH_MODE_OFF:
            default:
                iconRes = R.drawable.ic_flash_off;
                break;
        }

        button.setImageResource(iconRes);
        button.setColorFilter(tintColor);
    }
}
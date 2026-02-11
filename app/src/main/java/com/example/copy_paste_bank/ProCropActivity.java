package com.example.copy_paste_bank;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProCropActivity extends AppCompatActivity {

    private ProCropView cropView; // Asumimos que esta clase existe
    private String outputPath;

    /**
     * Nuevo método para construir el Intent que usará ActivityResultLauncher.
     * Ya no llama a startActivityForResult() directamente.
     */
    public static Intent buildIntent(Context context, Uri imageUri, String outputPath) {
        Intent intent = new Intent(context, ProCropActivity.class);
        intent.putExtra("image", imageUri);
        intent.putExtra("output", outputPath);
        // Es una buena práctica otorgar permisos de lectura temporales al input URI también
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Soporte moderno para el botón de atrás (Android 12+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                Intent intent = new Intent(ProCropActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Uri imageUri = getIntent().getParcelableExtra("image");
        outputPath = getIntent().getStringExtra("output");

        if (imageUri == null || outputPath == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);
        setContentView(root);

        cropView = new ProCropView(this);
        root.addView(cropView);

        cropView.loadImage(imageUri);

        // 1. Botón DONE (Estilo sólido y destacado)
        ImageButton doneBtn = new ImageButton(this);
        doneBtn.setImageResource(android.R.drawable.ic_menu_save);
        doneBtn.setColorFilter(Color.BLACK); // Icono oscuro sobre fondo blanco

        GradientDrawable doneShape = new GradientDrawable();
        doneShape.setShape(GradientDrawable.OVAL);
        doneShape.setColor(Color.WHITE); // Fondo blanco sólido para destacar
        doneBtn.setBackground(doneShape);
        doneBtn.setElevation(dp(4));

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(64), dp(64));
        p.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        p.setMargins(0, 0, dp(25), dp(35)); // Ajustado para equilibrio
        root.addView(doneBtn, p);

        doneBtn.setOnClickListener(v -> {
            Bitmap cropped = cropView.getCroppedBitmap();
            if (cropped != null) {
                try {
                    File croppedFile = new File(outputPath);
                    croppedFile.getParentFile().mkdirs();
                    cropped.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(croppedFile));

                    Uri croppedUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            croppedFile
                    );

                    Intent result = new Intent();
                    result.setData(croppedUri);
                    result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    setResult(RESULT_OK, result);
                    finish();

                } catch (Exception e) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // 2. Botón RETAKE (Estilo circular traslúcido)
        ImageButton retakeBtn = new ImageButton(this);
        retakeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        retakeBtn.setColorFilter(Color.WHITE);

        GradientDrawable secondaryShape = new GradientDrawable();
        secondaryShape.setShape(GradientDrawable.OVAL);
        secondaryShape.setColor(0x66000000); // Negro traslúcido
        retakeBtn.setBackground(secondaryShape);
        retakeBtn.setPadding(dp(12), dp(12), dp(12), dp(12));

        FrameLayout.LayoutParams retakeParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        retakeParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        retakeParams.setMargins(dp(25), 0, 0, dp(43)); // Alineado con el centro del grande
        root.addView(retakeBtn, retakeParams);

        retakeBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Cancela recorte, vuelve a la cámara
            finish();
        });

        // 3. Botón ROTAR (Estilo circular traslúcido)
        ImageButton rotateBtn = new ImageButton(this);
        rotateBtn.setImageResource(android.R.drawable.ic_menu_rotate);
        rotateBtn.setColorFilter(Color.WHITE);
        rotateBtn.setBackground(secondaryShape.getConstantState().newDrawable().mutate()); // Clona el fondo
        rotateBtn.setPadding(dp(12), dp(12), dp(12), dp(12));

        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(dp(48), dp(48));
        rp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        rp.setMargins(0, 0, 0, dp(43));
        root.addView(rotateBtn, rp);

        rotateBtn.setOnClickListener(v -> cropView.rotate90());
    }

    // El método saveBitmap no se usa actualmente en doneBtn.setOnClickListener,
    // pero se puede mantener como utilidad.
    private boolean saveBitmap(Bitmap bitmap, String path) {
        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
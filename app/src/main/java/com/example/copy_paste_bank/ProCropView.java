package com.example.copy_paste_bank;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ProCropView extends FrameLayout {

    private ImageView imageView;
    private CropOverlayView overlay;
    private Bitmap originalBitmap;
    private Matrix displayMatrix = new Matrix();

    public ProCropView(Context context) {
        super(context);
        setBackgroundColor(0xFF000000);

        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        addView(imageView, -1, -1);

        overlay = new CropOverlayView(context);
        addView(overlay);
    }

    public void loadImage(Uri uri) {
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            imageView.setImageBitmap(originalBitmap);
            post(this::fitImageToView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fitImageToView() {
        if (originalBitmap == null || getWidth() == 0) return;

        float scaleX = (float) getWidth() / originalBitmap.getWidth();
        float scaleY = (float) getHeight() / originalBitmap.getHeight();
        float scale = Math.min(scaleX, scaleY);

        displayMatrix.reset();
        displayMatrix.postScale(scale, scale);
        displayMatrix.postTranslate(
                (getWidth() - originalBitmap.getWidth() * scale) / 2,
                (getHeight() - originalBitmap.getHeight() * scale) / 2
        );

        imageView.setImageMatrix(displayMatrix);

        RectF imageBounds = new RectF(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight());
        displayMatrix.mapRect(imageBounds);

        overlay.setImageBounds(imageBounds.left, imageBounds.top, imageBounds.right, imageBounds.bottom);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (originalBitmap != null) fitImageToView();
    }

    public void rotate90() {
        if (originalBitmap == null) return;
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(), rotateMatrix, true);
        originalBitmap.recycle();
        originalBitmap = rotated;
        imageView.setImageBitmap(originalBitmap);
        fitImageToView();
    }

    public Bitmap getCroppedBitmap() {
        if (originalBitmap == null) return null;

        RectF cropRect = new RectF();
        overlay.getCropRect(cropRect);

        // Invertir la matrix de visualización para mapear coordenadas de pantalla → bitmap original
        Matrix inverse = new Matrix();
        if (!displayMatrix.invert(inverse)) return null;

        inverse.mapRect(cropRect);

        int x = Math.max(0, (int) cropRect.left);
        int y = Math.max(0, (int) cropRect.top);
        int width = Math.min((int) cropRect.width(), originalBitmap.getWidth() - x);
        int height = Math.min((int) cropRect.height(), originalBitmap.getHeight() - y);

        if (width <= 0 || height <= 0) return null;

        try {
            return Bitmap.createBitmap(originalBitmap, x, y, width, height);
        } catch (Exception e) {
            return null;
        }
    }
}
package com.example.copy_paste_bank;

import android.content.Context;
import android.graphics.*;
import android.view.*;

public class CropOverlayView extends View {

    private RectF cropRect = new RectF();
    private RectF imageBounds = new RectF();

    private Paint dimPaint = new Paint();
    private Paint borderPaint = new Paint();
    private Paint cornerPaint = new Paint();

    private static final float CORNER_LENGTH = 60f;
    private static final float CORNER_THICKNESS = 12f;
    private static final float TOUCH_RADIUS = 80f; // Radio para detectar bordes/esquinas
    private static final float MIN_SIZE = 100f;    // Tamaño mínimo del cuadro

    private enum TouchType { NONE, RESIZE_CORNER, RESIZE_EDGE, MOVE }
    private TouchType activeTouch = TouchType.NONE;
    private Corner activeCorner = null; // Para redimensión de esquina
    private Edge activeEdge = null;     // Para redimensión de borde

    private enum Corner { TL, TR, BL, BR }
    private enum Edge { TOP, BOTTOM, LEFT, RIGHT }

    private float lastX, lastY;

    public CropOverlayView(Context context) {
        super(context);
        dimPaint.setColor(0xAA000000);

        borderPaint.setColor(0x80FFFFFF);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
    }

    public void setImageBounds(float left, float top, float right, float bottom) {
        imageBounds.set(left, top, right, bottom);
        cropRect.set(imageBounds);
        invalidate();
    }

    public void getCropRect(RectF outRect) {
        outRect.set(cropRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fondo oscuro fuera del recorte
        canvas.drawRect(0, 0, getWidth(), cropRect.top, dimPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, dimPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), dimPaint);

        // Borde completo
        canvas.drawRect(cropRect, borderPaint);

        // Esquinas blancas gruesas
        float l = CORNER_LENGTH;
        float t = CORNER_THICKNESS;

        canvas.drawRect(cropRect.left - t, cropRect.top - t, cropRect.left + l, cropRect.top + t, cornerPaint);
        canvas.drawRect(cropRect.left - t, cropRect.top - t, cropRect.left + t, cropRect.top + l, cornerPaint);

        canvas.drawRect(cropRect.right - l, cropRect.top - t, cropRect.right + t, cropRect.top + t, cornerPaint);
        canvas.drawRect(cropRect.right - t, cropRect.top - t, cropRect.right + t, cropRect.top + l, cornerPaint);

        canvas.drawRect(cropRect.left - t, cropRect.bottom - t, cropRect.left + l, cropRect.bottom + t, cornerPaint);
        canvas.drawRect(cropRect.left - t, cropRect.bottom - l, cropRect.left + t, cropRect.bottom + t, cornerPaint);

        canvas.drawRect(cropRect.right - l, cropRect.bottom - t, cropRect.right + t, cropRect.bottom + t, cornerPaint);
        canvas.drawRect(cropRect.right - t, cropRect.bottom - l, cropRect.right + t, cropRect.bottom + t, cornerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeTouch = getTouchType(x, y);
                if (activeTouch != TouchType.NONE) {
                    lastX = x;
                    lastY = y;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (activeTouch != TouchType.NONE) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    lastX = x;
                    lastY = y;

                    if (activeTouch == TouchType.MOVE) {
                        // Mover todo el cuadro
                        cropRect.offset(dx, dy);
                        constrainToImageBounds();
                    } else {
                        // Redimensionar
                        adjustRect(dx, dy);
                    }
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeTouch = TouchType.NONE;
                activeCorner = null;
                activeEdge = null;
                break;
        }
        return true;
    }

    private TouchType getTouchType(float x, float y) {
        // Primero comprobamos esquinas
        if (distance(x, y, cropRect.left, cropRect.top) <= TOUCH_RADIUS) {
            activeCorner = Corner.TL;
            return TouchType.RESIZE_CORNER;
        }
        if (distance(x, y, cropRect.right, cropRect.top) <= TOUCH_RADIUS) {
            activeCorner = Corner.TR;
            return TouchType.RESIZE_CORNER;
        }
        if (distance(x, y, cropRect.left, cropRect.bottom) <= TOUCH_RADIUS) {
            activeCorner = Corner.BL;
            return TouchType.RESIZE_CORNER;
        }
        if (distance(x, y, cropRect.right, cropRect.bottom) <= TOUCH_RADIUS) {
            activeCorner = Corner.BR;
            return TouchType.RESIZE_CORNER;
        }

        // Luego bordes centrales
        if (Math.abs(x - cropRect.left) <= TOUCH_RADIUS && y > cropRect.top + TOUCH_RADIUS && y < cropRect.bottom - TOUCH_RADIUS) {
            activeEdge = Edge.LEFT;
            return TouchType.RESIZE_EDGE;
        }
        if (Math.abs(x - cropRect.right) <= TOUCH_RADIUS && y > cropRect.top + TOUCH_RADIUS && y < cropRect.bottom - TOUCH_RADIUS) {
            activeEdge = Edge.RIGHT;
            return TouchType.RESIZE_EDGE;
        }
        if (Math.abs(y - cropRect.top) <= TOUCH_RADIUS && x > cropRect.left + TOUCH_RADIUS && x < cropRect.right - TOUCH_RADIUS) {
            activeEdge = Edge.TOP;
            return TouchType.RESIZE_EDGE;
        }
        if (Math.abs(y - cropRect.bottom) <= TOUCH_RADIUS && x > cropRect.left + TOUCH_RADIUS && x < cropRect.right - TOUCH_RADIUS) {
            activeEdge = Edge.BOTTOM;
            return TouchType.RESIZE_EDGE;
        }

        // Si está dentro del área y no en borde → mover
        if (cropRect.contains(x, y)) {
            return TouchType.MOVE;
        }

        return TouchType.NONE;
    }

    private void adjustRect(float dx, float dy) {
        if (activeTouch == TouchType.RESIZE_CORNER && activeCorner != null) {
            switch (activeCorner) {
                case TL:
                    cropRect.left = Math.max(imageBounds.left, Math.min(cropRect.left + dx, cropRect.right - MIN_SIZE));
                    cropRect.top = Math.max(imageBounds.top, Math.min(cropRect.top + dy, cropRect.bottom - MIN_SIZE));
                    break;
                case TR:
                    cropRect.right = Math.min(imageBounds.right, Math.max(cropRect.right + dx, cropRect.left + MIN_SIZE));
                    cropRect.top = Math.max(imageBounds.top, Math.min(cropRect.top + dy, cropRect.bottom - MIN_SIZE));
                    break;
                case BL:
                    cropRect.left = Math.max(imageBounds.left, Math.min(cropRect.left + dx, cropRect.right - MIN_SIZE));
                    cropRect.bottom = Math.min(imageBounds.bottom, Math.max(cropRect.bottom + dy, cropRect.top + MIN_SIZE));
                    break;
                case BR:
                    cropRect.right = Math.min(imageBounds.right, Math.max(cropRect.right + dx, cropRect.left + MIN_SIZE));
                    cropRect.bottom = Math.min(imageBounds.bottom, Math.max(cropRect.bottom + dy, cropRect.top + MIN_SIZE));
                    break;
            }
        } else if (activeTouch == TouchType.RESIZE_EDGE && activeEdge != null) {
            switch (activeEdge) {
                case TOP:
                    cropRect.top = Math.max(imageBounds.top, Math.min(cropRect.top + dy, cropRect.bottom - MIN_SIZE));
                    break;
                case BOTTOM:
                    cropRect.bottom = Math.min(imageBounds.bottom, Math.max(cropRect.bottom + dy, cropRect.top + MIN_SIZE));
                    break;
                case LEFT:
                    cropRect.left = Math.max(imageBounds.left, Math.min(cropRect.left + dx, cropRect.right - MIN_SIZE));
                    break;
                case RIGHT:
                    cropRect.right = Math.min(imageBounds.right, Math.max(cropRect.right + dx, cropRect.left + MIN_SIZE));
                    break;
            }
        }
    }

    private void constrainToImageBounds() {
        if (cropRect.left < imageBounds.left) cropRect.offset(imageBounds.left - cropRect.left, 0);
        if (cropRect.top < imageBounds.top) cropRect.offset(0, imageBounds.top - cropRect.top);
        if (cropRect.right > imageBounds.right) cropRect.offset(imageBounds.right - cropRect.right, 0);
        if (cropRect.bottom > imageBounds.bottom) cropRect.offset(0, imageBounds.bottom - cropRect.bottom);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
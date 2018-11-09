package com.example.mp.projectmp.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.List;

/**
 * SurfaceView to show LenxCameraPreview2 feed
 */
public class PreviewSurfaceView extends SurfaceView {

    private CameraPreview camPreview;
    private boolean listenerSet = false;
    public Paint paint;
    private DrawingView drawingView;
    private boolean drawingViewSet = false;

    private ScaleGestureDetector mScaleDetector;

    private int mPtrCount = 0;

    private float mPrimStartTouchEventX = -1;
    private float mPrimStartTouchEventY = -1;
    private float mSecStartTouchEventX = -1;
    private float mSecStartTouchEventY = -1;
    private float mPrimSecStartTouchDistance = 0;

    private int mViewScaledTouchSlop = 0;




    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    private boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY){
        float moveX = Math.abs(event.getX(ptrIndex) - originalX);
        float moveY = Math.abs(event.getY(ptrIndex) - originalY);

        if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
            return true;
        }
        return false;
    }

    private boolean isPinchGesture(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            final float distanceCurrent = distance(event, 0, 1);
            final float diffPrimX = mPrimStartTouchEventX - event.getX(0);
            final float diffPrimY = mPrimStartTouchEventY - event.getY(0);
            final float diffSecX = mSecStartTouchEventX - event.getX(1);
            final float diffSecY = mSecStartTouchEventY - event.getY(1);

            if (// if the distance between the two fingers has increased past
                // our threshold
                    Math.abs(distanceCurrent - mPrimSecStartTouchDistance) > mViewScaledTouchSlop
                            // and the fingers are moving in opposing directions
                            && (diffPrimY * diffSecY) <= 0
                            && (diffPrimX * diffSecX) <= 0) {
                // mPinchClamp = false; // don't clamp initially
                return true;
            }
        }

        return false;
    }

    private float distance(MotionEvent event, int first, int second) {
        if (event.getPointerCount() >= 2) {
            final float x = event.getX(first) - event.getX(second);
            final float y = event.getY(first) - event.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = (event.getAction() & MotionEvent.ACTION_MASK);
        if (!listenerSet) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                mPtrCount++;
                if (mPtrCount == 1 && mPrimStartTouchEventY == -1 && mPrimStartTouchEventY == -1) {
                    mPrimStartTouchEventX = event.getX(0);
                    mPrimStartTouchEventY = event.getY(0);
                    Log.d("TAG", String.format("POINTER ONE X = %.5f, Y = %.5f", mPrimStartTouchEventX, mPrimStartTouchEventY));
                    autoFocus(event);
                }
                if (mPtrCount == 2) {
                    // Starting distance between fingers
                    mSecStartTouchEventX = event.getX(1);
                    mSecStartTouchEventY = event.getY(1);
                    mPrimSecStartTouchDistance = distance(event, 0, 1);
                    Log.d("TAG", String.format("POINTER TWO X = %.5f, Y = %.5f", mSecStartTouchEventX, mSecStartTouchEventY));
                    autoFocus(event);
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mPtrCount--;
                if (mPtrCount < 2) {
                    mSecStartTouchEventX = -1;
                    mSecStartTouchEventY = -1;
                }
                if (mPtrCount < 1) {
                    mPrimStartTouchEventX = -1;
                    mPrimStartTouchEventY = -1;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                boolean isPrimMoving = isScrollGesture(event, 0, mPrimStartTouchEventX, mPrimStartTouchEventY);
                boolean isSecMoving = (mPtrCount > 1 && isScrollGesture(event, 1, mSecStartTouchEventX, mSecStartTouchEventY));

                // There is a chance that the gesture may be a scroll
                if (mPtrCount > 1 && isPinchGesture(event)) {
                    Log.d("TAG", "PINCH! OUCH!");
                    camPreview.onTouchEvent(event);

                }
                break;
        }

        return true;
    }

    private boolean autoFocus(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        Rect touchRect = new Rect(
                (int) (x - 100),
                (int) (y - 100),
                (int) (x + 100),
                (int) (y + 100));

        final Rect targetFocusRect = new Rect(
                touchRect.left * 2000 / this.getWidth() - 1000,
                touchRect.top * 2000 / this.getHeight() - 1000,
                touchRect.right * 2000 / this.getWidth() - 1000,
                touchRect.bottom * 2000 / this.getHeight() - 1000);

        camPreview.doTouchFocus(targetFocusRect);
        if (drawingViewSet) {
            drawingView.setHaveTouch(true, touchRect);
            drawingView.invalidate();

            // Remove the square after some time
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                    drawingView.invalidate();
                }
            }, 1000);
        }
        return false;

    }
    /**
     * set CameraPreview instance for touch focus.
     *
     * @param camPreview - CameraPreview
     */
    public void setListener(CameraPreview camPreview) {
        this.camPreview = camPreview;
        listenerSet = true;
    }

    /**
     * set DrawingView instance for touch focus indication.
     *
     * @param dView - DrawingView
     */
    public void setDrawingView(DrawingView dView) {
        drawingView = dView;
        drawingViewSet = true;
    }

}
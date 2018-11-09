package com.example.mp.projectmp.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.mp.projectmp.CameraActivity;
import com.example.mp.projectmp.ImagePreviewActivity;
import com.example.mp.projectmp.R;
import com.example.mp.projectmp.interfaces.CameraInterface;
import com.example.mp.projectmp.interfaces.CurrentFlashState;
import com.example.mp.projectmp.interfaces.FinishActivityInterface;
import com.example.mp.projectmp.interfaces.ProgressBarToggle;

import static android.content.Context.WINDOW_SERVICE;

public class CameraPreview
        implements
        SurfaceHolder.Callback {

    private Camera mCamera = null;
    public Camera.Parameters params;
    private SurfaceHolder sHolder;

    public List<Camera.Size> supportedSizes;

    public int isCamOpen = 0;
    public boolean isSizeSupported = false;
    private int previewWidth, previewHeight;

    private final static String TAG = "CameraPreview";

    float mDist = 0;
    Context context;

    CurrentFlashState currentFlashState;
    ProgressBarToggle pbToggle;

    boolean isPreviewRunning = false;

    FinishActivityInterface finishActivityInterface;

    CameraInterface cameraInterface;

    boolean isFrontCamera;

    public CameraPreview(Context context, int width, int height, CurrentFlashState currentFlashState, ProgressBarToggle toggle, FinishActivityInterface finishActivityInterface, CameraInterface cameraInterface) {
        Log.i("campreview", "Width = " + String.valueOf(width));
        Log.i("campreview", "Height = " + String.valueOf(height));
        previewWidth = width;
        previewHeight = height;
        this.context = context;
        this.currentFlashState = currentFlashState;
        this.pbToggle = toggle;
        this.finishActivityInterface = finishActivityInterface;
        this.cameraInterface = cameraInterface;

        pbToggle.interfacePB(View.INVISIBLE);
    }

    private int openCamera(int cameraFacingBack) {
        if (isCamOpen == 1) {
            releaseCamera();
        }

        mCamera = Camera.open(cameraFacingBack);

        if (mCamera == null) {
            return -1;
        }

        params = mCamera.getParameters();
        params.setPreviewSize(previewWidth, previewHeight);
//        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

        try {
            mCamera.setParameters(params);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
//        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
        try {

            mCamera.setPreviewDisplay(sHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            return -1;
        }
        isCamOpen = 1;
        return isCamOpen;
    }
    public int isCamOpen() {
        return isCamOpen;
    }

    public void capturePhoto(){
        mCamera.takePicture(null, null, mPicture);
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            File pictureFile = getOutputMediaFile();

            Intent intent = new Intent(context,ImagePreviewActivity.class);
//            intent.putExtra("filepath", data);
//            ImageSingletonClass singletonClass = ImageSingletonClass.getInstance();
//            singletonClass.setImage(data);
//            context.startActivity(intent);
            SaveImage saveImage = new SaveImage(pictureFile,data);
            saveImage.execute();

        }
    };

    public void changeCamera(boolean frontCamera) {
        releaseCamera();
        if(frontCamera) {
            isFrontCamera = true;
            openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setDisplayOrientation(90);
        }else{
            isFrontCamera = false;
            openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);

        }
    }

    public class SaveImage extends AsyncTask<Void, Void, Void> {
        File pictureFile;
        byte[] data;

        public SaveImage(File pictureFile, byte[] data) {
            this.pictureFile = pictureFile;
            this.data = data;
            pbToggle.interfacePB(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(Void... voids) {

            if (pictureFile == null) {
                return null;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap raw;
                if(!isFrontCamera) {
                    raw = RotateBitmap(ByteArrayToBitmap(data), 90);
                }else{
                    raw = RotateBitmap(ByteArrayToBitmap(data), -90);
                    raw = flip(raw);
                }
                raw.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                pbToggle.interfacePB(View.INVISIBLE);
            } catch (IOException e) {
                pbToggle.interfacePB(View.INVISIBLE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pbToggle.interfacePB(View.INVISIBLE);
            Intent intent = new Intent(context,ImagePreviewActivity.class);
            intent.putExtra("filepath", pictureFile.getAbsolutePath());
            context.startActivity(intent);
            finishActivityInterface.activityClose();
        }
    }

    Bitmap flip(Bitmap src)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap ByteArrayToBitmap(byte[] byteArray)
    {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap;
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ProjectMP");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("ProjectMP", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        isCamOpen = 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sHolder = holder;

        isCamOpen = openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (isPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(height, width);
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(width, height);
        }

        if(display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(height, width);
        }

        if(display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(width, height);
            mCamera.setDisplayOrientation(180);
        }

        mCamera.setParameters(parameters);
        previewCamera();

    }

    public void previewCamera() {
        try {
            mCamera.setPreviewDisplay(sHolder);
            mCamera.startPreview();
            isPreviewRunning = true;
        } catch(Exception e) {
            Log.d(TAG, "Cannot start preview", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();

    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect) {
        Log.i(TAG, "TouchFocus");
        try {
            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters para = mCamera.getParameters();
            para.setFocusAreas(focusList);
            para.setMeteringAreas(focusList);
            mCamera.setParameters(para);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }

    }

    /**
     * AutoFocus callback
     */
    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0){
                mCamera.cancelAutoFocus();
            }
        }
    };

    public boolean onTouchEvent(MotionEvent event){
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    public void changeFlash(int flashImg) {

        params = mCamera.getParameters();

        String mode;
        if(flashImg==1){
            mode = "off";
        }else if(flashImg==0){
            mode = "on";
        }else{
            mode = "auto";
        }

        params.setFlashMode(mode);

        try {
            mCamera.setParameters(params);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if(flashImg==0 || flashImg==1){
            flashImg++;
            currentFlashState.changeFlash(flashImg);
        }else{
            currentFlashState.changeFlash(0);
        }

    }
}

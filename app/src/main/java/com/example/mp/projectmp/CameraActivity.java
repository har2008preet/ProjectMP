package com.example.mp.projectmp;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.mp.projectmp.interfaces.CameraInterface;
import com.example.mp.projectmp.interfaces.CurrentFlashState;
import com.example.mp.projectmp.interfaces.FinishActivityInterface;
import com.example.mp.projectmp.interfaces.ProgressBarToggle;
import com.example.mp.projectmp.util.CameraPreview;
import com.example.mp.projectmp.util.DrawingView;
import com.example.mp.projectmp.util.PreviewSurfaceView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity implements CurrentFlashState, ProgressBarToggle, FinishActivityInterface, CameraInterface{

    private Camera mCamera;
    private CameraPreview mCameraPreview;

    @BindView(R.id.preview_surface)
    PreviewSurfaceView camView;
    @BindView(R.id.drawing_surface)
    DrawingView drawingView;
    @BindView(R.id.button_capture)
    Button captureButton;
    @BindView(R.id.flashImg)
    ImageView flashImg;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.rotate)
    ImageView rotate;

    private CameraPreview cameraPreview;

    private int previewWidth = 1280;
    private int previewHeight = 720;

    int currentFlash = 0;

    boolean frontCamera = false;

    FirebaseStorage storage;
    StorageReference imagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        storage = FirebaseStorage.getInstance("gs://projectmp-b3ba7.appspot.com");

        imagesRef = storage.getReference().child("images");

        SurfaceHolder camHolder = camView.getHolder();

        cameraPreview = new CameraPreview(getBaseContext(),previewWidth, previewHeight, this,this,this,this);

        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camView.setListener(cameraPreview);
        //cameraPreview.changeExposureComp(-currentAlphaAngle);
        camView.setDrawingView(drawingView);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreview.capturePhoto();
            }
        });

        flashImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreview.changeFlash(currentFlash);
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frontCamera = !frontCamera;
                cameraPreview.changeCamera(frontCamera);
            }
        });
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }


    @Override
    public void changeFlash(int value) {
        currentFlash = value;

        if(currentFlash==0){
            flashImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.flash_auto));
        }else if(currentFlash==1){
            flashImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.flash_on));
        }else{
            flashImg.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.flash_off));
        }
    }

    @Override
    public void interfacePB(int visiblitly) {
        progressBar.setVisibility(visiblitly);
    }

    @Override
    public void activityClose() {
        finish();
    }

    @Override
    public void cameraFace(boolean isFrontCamera) {
        frontCamera = isFrontCamera;
    }
}

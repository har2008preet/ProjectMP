package com.example.mp.projectmp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mp.projectmp.util.ImageSingletonClass;
import com.example.mp.projectmp.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImagePreviewActivity extends AppCompatActivity {

    @BindView(R.id.imagePreview)
    ImageView imagePreview;
    @BindView(R.id.imgCrop)
    ImageView imgCrop;
    @BindView(R.id.txtUpload)
    TextView txtUpload;

    private ProgressDialog dialog;

    File f;
    File tempfile;

    FirebaseStorage storage;
    StorageReference imagesRef;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);

        storage = FirebaseStorage.getInstance("gs://projectmp-b3ba7.appspot.com");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        imagesRef = storage.getReference().child("IMG_"+timeStamp+".jpg");


        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("urls");


        dialog = new ProgressDialog(this);

        String path = getIntent().getStringExtra("filepath");

        tempfile = null;

        ImageSingletonClass singletonClass = ImageSingletonClass.getInstance();
        final byte[] data = singletonClass.getImage();
        Log.d("","");


        f = new File(path);
        Picasso.get().load(f).into(imagePreview);

        imgCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File pictureFile = getOutputMediaFile();

                UCrop.of(Uri.fromFile(f), Uri.fromFile(pictureFile))
                        .withMaxResultSize(720, 1280)
                        .start(ImagePreviewActivity.this);
            }
        });

        txtUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempfile==null){
                    tempfile = f;
                }
                int size = (int) tempfile.length();
                byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(tempfile));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                UploadTask uploadTask = imagesRef.putBytes(bytes);
                dialog.setMessage("Uploading image, please wait.");
                dialog.show();
                dialog.setCancelable(false);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("","");
                        if (dialog.isShowing()) {
                            dialog.setMessage("Upload Failed");
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                }
                            }, 2000);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.d("","");
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                Log.d("","");
                                List<String> mydata= new ArrayList<>();
                                mydata.add(url);
                                databaseReference.push().setValue(url);

                                if (dialog.isShowing()) {
                                    dialog.setMessage("Upload Successful");
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            dialog.dismiss();

                                            finishAffinity();
                                            startActivity(new Intent(ImagePreviewActivity.this,MainActivity.class));
                                        }
                                    }, 2000);
                                }
                            }
                        });
                    }
                });

            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);


            tempfile = new File(resultUri.getPath());
            String path = resultUri.getPath();
            Picasso.get().load(tempfile).into(imagePreview);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File cacheDir = getApplicationContext().getCacheDir();

        File[] files = cacheDir.listFiles();

        if (files != null) {
            for (File file : files)
                file.delete();
        }
    }


}

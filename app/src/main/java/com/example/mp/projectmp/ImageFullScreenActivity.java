package com.example.mp.projectmp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ScaleGestureDetector;

import com.example.mp.projectmp.util.TouchImageView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFullScreenActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    TouchImageView imageView;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);
        ButterKnife.bind(this);

//        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        String url = getIntent().getStringExtra("url");

        Picasso.get().load(url).into(imageView);


    }
}

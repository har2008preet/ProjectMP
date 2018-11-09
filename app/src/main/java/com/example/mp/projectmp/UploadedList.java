package com.example.mp.projectmp;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.mp.projectmp.Adapter.UploadedImagesAdapter;
import com.example.mp.projectmp.Model.UploadedImageModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadedList extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    List<UploadedImageModel> list;
    UploadedImagesAdapter adapter;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_list);
        ButterKnife.bind(this);

        dialog = new ProgressDialog(this);


        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("urls");

        list = new ArrayList<>();


        getUploadedImageURLS();
    }

    private void getUploadedImageURLS() {
        dialog.setMessage("Getting images, please wait.");
        dialog.show();
        dialog.setCancelable(false);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String url = String.valueOf(singleSnapshot.getValue());
                    Log.d("URList", url);
                    UploadedImageModel model = new UploadedImageModel();
                    model.setUrl(url);
                    list.add(model);
                }

                Collections.reverse(list);
                adapter = new UploadedImagesAdapter(UploadedList.this,list);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),2);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setHasFixedSize(true);
                recyclerView.setItemViewCacheSize(20);
                recyclerView.setDrawingCacheEnabled(true);
                recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

                recyclerView.setAdapter(adapter);

                if (dialog.isShowing()) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

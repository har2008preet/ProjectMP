package com.example.mp.projectmp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mp.projectmp.ImageFullScreenActivity;
import com.example.mp.projectmp.Model.UploadedImageModel;
import com.example.mp.projectmp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadedImagesAdapter extends RecyclerView.Adapter<UploadedImagesAdapter.ViewHolder> {

    private Context context;
    private List<UploadedImageModel> uploadedImageModelList;

    public UploadedImagesAdapter(Context context, List<UploadedImageModel> uploadedImageModelList) {
        this.context = context;
        this.uploadedImageModelList = uploadedImageModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_uploaded_image, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final UploadedImageModel imageModel = uploadedImageModelList.get(i);

        Picasso.get().load(imageModel.getUrl()).fit().centerCrop().into(viewHolder.image);

        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageFullScreenActivity.class);
                intent.putExtra("url",imageModel.getUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploadedImageModelList.size();
    }

    public void addURLS(List<UploadedImageModel> list) {
        uploadedImageModelList.clear();
        uploadedImageModelList = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.parent)
        LinearLayout parent;
        @BindView(R.id.image)
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
//            parent = (LinearLayout)itemView.findViewById(R.id.parent);
//            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}

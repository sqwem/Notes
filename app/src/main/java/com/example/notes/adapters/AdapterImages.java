package com.example.notes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.ImageActivity;
import com.example.notes.R;
import com.example.notes.models.ImageModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AdapterImages extends FirestoreRecyclerAdapter<ImageModel, AdapterImages.ImageViewHolder> {

    Context context;

    public AdapterImages(@NonNull FirestoreRecyclerOptions<ImageModel> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, final int i, @NonNull final ImageModel imageModel) {
        Picasso.with(context)
                .load(imageModel.getImageUrl())
                .into(imageViewHolder.imageView);

        imageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(v.getContext(), ImageActivity.class);
                imageIntent.putExtra("imageUrl", imageModel.getImageUrl());
                v.getContext().startActivity(imageIntent);
            }
        });

        imageViewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("imagesNote")
                        .child(imageModel.getImageId());

                imageRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getSnapshots().getSnapshot(i).getReference().delete();
                            }
                        });
            }
        });

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_image_item, parent, false);
        return new AdapterImages.ImageViewHolder(view);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton deleteBtn;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageNote);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);

        }
    }
}

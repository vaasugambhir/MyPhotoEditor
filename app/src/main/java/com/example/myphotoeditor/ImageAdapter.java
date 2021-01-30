package com.example.myphotoeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(ImageView image, String path, String name, int pos);
    }

    private ArrayList<String> mFilePaths;
    private ArrayList<String> mFileNames;
    private final Context context;
    private OnImageClickListener listener;

    public ImageAdapter (Context c) {
        context = c;
    }

    public void addOnClickListener(OnImageClickListener l) {
        listener = l;
    }

    public void add (ArrayList<String> paths, ArrayList<String> names) {
        mFilePaths = new ArrayList<>();
        mFileNames = new ArrayList<>();
        mFilePaths.addAll(paths);
        mFileNames.addAll(names);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_image_holder, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        File image = new File(mFilePaths.get(position));

        ViewCompat.setTransitionName(holder.image, mFilePaths.get(position));

        holder.image.setOnClickListener(v -> {
            String path = mFilePaths.get(position);
            String name = mFileNames.get(position);
            listener.onImageClick(holder.image, path, name, position);
        });

        Glide.with(context)
                .load(image)
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return mFilePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imageView_holderImage);
        }
    }
}

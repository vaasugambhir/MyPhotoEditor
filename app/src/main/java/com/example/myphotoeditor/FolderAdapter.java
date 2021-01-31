package com.example.myphotoeditor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private final Context mContext;
    private ArrayList<String> folders;
    private final OnFolderClickListener listener;
    private ArrayList<String> paths;
    private ArrayList<Integer> count;

    public FolderAdapter(Context context, OnFolderClickListener l) {
        mContext = context;
        listener = l;
    }

    public void add(ArrayList<String> f, ArrayList<String> p, ArrayList<Integer> c) {
        folders = f;
        paths = p;
        count = c;
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.folder_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder holder, int position) {
        String path = paths.get(position);
        holder.folderName.setText(folders.get(position));
        holder.imageCount.setText(String.valueOf(count.get(position)));
        holder.myLayout.setOnClickListener(v -> listener.onFolderClick(position, holder.folderName));
        Drawable defImage = ContextCompat.getDrawable(mContext, R.drawable.ic_camera_foreground);
        if (!path.equals(""))
            Glide.with(mContext)
                    .load(path)
                    .into(holder.thumbnail);
        else
            holder.thumbnail.setImageDrawable(defImage);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName, imageCount;
        ImageView thumbnail;
        LinearLayout myLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.textView_folderName);
            thumbnail = itemView.findViewById(R.id.imageView_folderImage);
            imageCount = itemView.findViewById(R.id.textView_folderImageCount);
            myLayout = itemView.findViewById(R.id.layout_folderList);
        }
    }

    public interface OnFolderClickListener {
        void onFolderClick(int pos, TextView textView);
    }
}

package com.example.myphotoeditor;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class FontListAdapter extends RecyclerView.Adapter<FontListAdapter.ViewHolder> {

    private final String[] mFontNames;
    private final Typeface[] mFonts;
    private int mSize, mColor;
    private final OnFontClickListener listener;

    @RequiresApi(api = Build.VERSION_CODES.O)
    FontListAdapter(Resources resources, OnFontClickListener l) {
        mFontNames = Constants.mFontNames;
        mFonts = Constants.getFonts(resources);
        listener = l;
    }

    public void add(int size, int color) {
        mColor = color;
        mSize = size;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.font_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fontItemList.setText(mFontNames[position]);
        holder.fontItemList.setTypeface(mFonts[position]);
        holder.fontItemList.setTextColor(mColor);
        holder.fontItemList.setTextSize(mSize);

        holder.fontItemList.setOnClickListener(v -> listener.onFontClick(position));
    }

    @Override
    public int getItemCount() {
        return mFontNames.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fontItemList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fontItemList = itemView.findViewById(R.id.textView_fontItemList);
        }
    }

    public interface OnFontClickListener {
        void onFontClick(int position);
    }
}

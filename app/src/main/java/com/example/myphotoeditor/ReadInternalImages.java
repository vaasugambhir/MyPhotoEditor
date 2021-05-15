package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ReadInternalImages {
    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ArrayList<String> getImageList(Context context) {
        ArrayList<String> imageList = new ArrayList<>();

        Uri uri;
        Cursor cursor;
        int column_index_data;
        String absolutePathOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        String orderBy = MediaStore.Images.Media.DATE_ADDED;

        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        assert cursor != null;
        column_index_data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

        ProgressBar progressBar = ((Activity) context).findViewById(R.id.load_progress);
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            imageList.add(absolutePathOfImage);
            LoadedImages.imageCount++;
            if (LoadedImages.totalCount > 0) {
                ((Activity) context).runOnUiThread(() -> {
                    progressBar.setProgress((LoadedImages.imageCount*100)/LoadedImages.totalCount);
                    System.out.println((LoadedImages.imageCount*100)/LoadedImages.totalCount);
                });
            }
        }

        return imageList;
    }

    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Map<String, ArrayList<String>> getImageAndAlbums(Context context) {
        Map<String, ArrayList<String>> map = new TreeMap<>();
        ArrayList<String> imageList;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder;
        String absolutePathOfImage, folderName;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String orderBy = MediaStore.Images.Media.DATE_ADDED;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        assert cursor != null;
        LoadedImages.totalCount = cursor.getCount() * 2;
        ProgressBar progressBar = ((Activity) context).findViewById(R.id.load_progress);
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        column_index_data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        column_index_folder = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            folderName = cursor.getString(column_index_folder);
            if (map.containsKey(folderName)) {
                imageList = map.get(folderName);
            } else {
                imageList = new ArrayList<>();
            }
            absolutePathOfImage = cursor.getString(column_index_data);
            File file = new File(absolutePathOfImage);
            if (file.exists())
                imageList.add(absolutePathOfImage);
            map.put(folderName, imageList);

            LoadedImages.imageCount++;
            if (LoadedImages.totalCount > 0) {
                ((Activity) context).runOnUiThread(() -> {
                    progressBar.setProgress((LoadedImages.imageCount*100)/LoadedImages.totalCount);
                    System.out.println((LoadedImages.imageCount*100)/LoadedImages.totalCount);
                });
            }
        }

        return map;
    }
}
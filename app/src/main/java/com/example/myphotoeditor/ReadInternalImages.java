package com.example.myphotoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class ReadInternalImages {
    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ArrayList<String>[] getImageList(Context context) {
        ArrayList<String> imageList = new ArrayList<>();
        ArrayList<String> imageNameList = new ArrayList<>();

        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_name;
        String absolutePathOfImage, imageName;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        String orderBy = MediaStore.Images.Media.DATE_ADDED;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        assert cursor != null;
        column_index_data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        column_index_name = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            imageList.add(absolutePathOfImage);
            imageName = cursor.getString(column_index_name);
            imageNameList.add(imageName);
        }

        return (ArrayList<String>[]) new ArrayList[]{imageList, imageNameList};
    }
}
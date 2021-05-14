package com.example.myphotoeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread loadPicsThread = new Thread(() -> {
            LoadedImages.folderMap = ReadInternalImages.getImageAndAlbums(getApplicationContext());
            LoadedImages.allImages = ReadInternalImages.getImageList(getApplicationContext());
            startActivity(new Intent(getApplicationContext(), FolderActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout_special);
            finish();
        });

        loadPicsThread.start();
    }
}
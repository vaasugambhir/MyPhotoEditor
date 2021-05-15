package com.example.myphotoeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread loadPicsThread = new Thread(() -> {
            LoadedImages.folderMap = ReadInternalImages.getImageAndAlbums(SplashActivity.this);
            LoadedImages.allImages = ReadInternalImages.getImageList(SplashActivity.this);
            startActivity(new Intent(getApplicationContext(), FolderActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout_special);
            finish();
        });

        loadPicsThread.start();
    }
}